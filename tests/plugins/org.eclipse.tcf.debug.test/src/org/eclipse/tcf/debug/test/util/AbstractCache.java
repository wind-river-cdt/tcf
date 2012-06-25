/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.test.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.tcf.debug.test.services.ResetMap.IResettable;
import org.eclipse.tcf.protocol.Protocol;

/**
 * A base implementation of a general purpose cache. Sub classes must implement
 * {@link #retrieve(DataRequestMonitor)} to fetch data from the data source.
 * Sub-classes are also responsible for calling {@link #set(Object, IStatus)}
 * and {@link #reset()} to manage the state of the cache in response to events
 * from the data source.
 * <p>
 * This cache requires an executor to use. The executor is used to synchronize
 * access to the cache state and data.
 * </p>
 * @since 2.2
 */
public abstract class AbstractCache<V> implements ICache<V>, IResettable {

    private static final Throwable INVALID_STATUS = new Throwable("Cache invalid"); //$NON-NLS-1$

    private class RequestCanceledListener implements Callback.ICanceledListener {
        public void requestCanceled(final Callback canceledRm) {
            invokeInDispatchThread(new Runnable() {
                public void run() {
                    handleCanceledRm(canceledRm);
                }
            });
        }
    };

    private RequestCanceledListener fRequestCanceledListener = new RequestCanceledListener();

    private boolean fValid;

    private V fData;
    private Throwable fError = INVALID_STATUS;

    private Object fWaitingList;

	/**
	 * Sub-classes should override this method to retrieve the cache data from
	 * its source. The implementation should call {@link #set(Object, IStatus)}
	 * to store the newly retrieved data when it arrives (or an error, if one
	 * occurred retrieving the data)
	 *
	 * @param rm
	 *            Request monitor for completion of data retrieval.
	 */
    abstract protected void retrieve();

    protected void invokeInDispatchThread(Runnable runnable) {
        if (Protocol.isDispatchThread()) {
            runnable.run();
        } else {
            Protocol.invokeLater(runnable);
        }
    }

    /**
     * Called to cancel a retrieve request.  This method is called when
     * clients of the cache no longer need data that was requested. <br>
     * Sub-classes should cancel and clean up requests to the asynchronous
     * data source.
     *
     * <p>
     * Note: Called while holding a lock to "this".  No new request will start until
     * this call returns.
     * </p>
     */
    abstract protected void canceled();

    public boolean isValid() {
        return fValid;
    }

    public V getData() {
        if (!isValid()) {
            throw new IllegalStateException("Cache is not valid.  Cache data can be read only when cache is valid."); //$NON-NLS-1$
        }
        return fData;
    }

    public Throwable getError() {
        if (!isValid()) {
            throw new IllegalStateException("Cache is not valid.  Cache status can be read only when cache is valid."); //$NON-NLS-1$
        }
        return fError;
    }

    public void wait(Callback rm) {
        assert Protocol.isDispatchThread();

        boolean first = false;
        synchronized (this) {
            if (fWaitingList == null) {
                first = true;
                fWaitingList = rm;
            } else if (fWaitingList instanceof Callback[]) {
                Callback[] waitingList = (Callback[])fWaitingList;
                int waitingListLength = waitingList.length;
                int i;
                for (i = 0; i < waitingListLength; i++) {
                    if (waitingList[i] == null) {
                        waitingList[i] = rm;
                        break;
                    }
                }
                if (i == waitingListLength) {
                    Callback[] newWaitingList = new Callback[waitingListLength + 1];
                    System.arraycopy(waitingList, 0, newWaitingList, 0, waitingListLength);
                    newWaitingList[waitingListLength] = rm;
                    fWaitingList = newWaitingList;
                }
            } else {
                Callback[] newWaitingList = new Callback[2];
                newWaitingList[0] = (Callback)fWaitingList;
                newWaitingList[1] = rm;
                fWaitingList = newWaitingList;
            }
        }
        rm.addCancelListener(fRequestCanceledListener);
        if (first && !isValid()) {
            retrieve();
        }
    }

    private void completeWaitingRms() {
        Object waiting = null;
        synchronized(this) {
            waiting = fWaitingList;
            fWaitingList = null;
        }
        if (waiting != null) {
            if (waiting instanceof Callback) {
                completeWaitingRm((Callback)waiting);
            } else if (waiting instanceof Callback[]) {
                Callback[] waitingList = (Callback[])waiting;
                for (int i = 0; i < waitingList.length; i++) {
                    if (waitingList[i] != null) {
                        completeWaitingRm(waitingList[i]);
                    }
                }
            }
            waiting = null;
        }
    }

    private void completeWaitingRm(Callback rm) {
        if (!isValid() && fError == null) {
            rm.setError(INVALID_STATUS);
        } else {
            rm.setError(fError);
        }

        rm.removeCancelListener(fRequestCanceledListener);
        rm.done();
    }

    private void handleCanceledRm(final Callback rm) {

        boolean found = false;
        boolean waiting = false;
        synchronized (this) {
            if (rm.equals(fWaitingList)) {
                found = true;
                waiting = false;
                fWaitingList = null;
            } else if(fWaitingList instanceof Callback[]) {
                Callback[] waitingList = (Callback[])fWaitingList;
                for (int i = 0; i < waitingList.length; i++) {
                    if (!found && rm.equals(waitingList[i])) {
                        waitingList[i] = null;
                        found = true;
                    }
                    waiting = waiting || waitingList[i] != null;
                }
            }
            if (found && !waiting) {
                canceled();
            }
        }

        // If we have no clients waiting anymore, cancel the request
        if (found) {
            // We no longer need to listen to cancellations.
            rm.removeCancelListener(fRequestCanceledListener);
            rm.setError(new CancellationException());
            rm.done();
        }

    }

    /**
     * Returns true if there are no clients waiting for this cache or if the
     * clients that are waiting, have already canceled their requests.
     * <p>
     * Note: Calling this method may cause the client request monitors that were
     * canceled to be completed with a cancel status.  If all the client request
     * monitors were canceled, this method will also cause the {@link #canceled()}
     * method to be called.  Both of these side effects will only happen
     * asynchronously after <code>isCanceled()</code> returns.
     * </p>
     *
     * @return <code>true</code> if all clients waiting on this cache have been
     * canceled, or if there are no clients waiting at all.
     */
    protected boolean isCanceled() {
        Object waitingList = null;
        synchronized (this) {
            if (fWaitingList instanceof Callback[]) {
                waitingList = new Callback[((Callback[])fWaitingList).length];
                System.arraycopy(fWaitingList, 0, waitingList, 0, ((Callback[]) fWaitingList).length);
            } else {
                waitingList = fWaitingList;
            }
        }
        boolean canceled;
        List<Callback> canceledRms = null;
        if (waitingList instanceof Callback) {
            if ( ((Callback)waitingList).isCanceled() ) {
                canceledRms = new ArrayList<Callback>(1);
                canceledRms.add((Callback)waitingList);
                canceled = true;
            } else {
                canceled = false;
            }
        } else if(waitingList instanceof Callback[]) {
            canceled = true;
            Callback[] waitingListArray = (Callback[])waitingList;
            for (int i = 0; i < waitingListArray.length; i++) {
                if (waitingListArray[i] != null) {
                    if (waitingListArray[i].isCanceled()) {
                        if (canceledRms == null) {
                            canceledRms = new ArrayList<Callback>(1);
                        }
                        canceledRms.add( waitingListArray[i] );
                    } else {
                        canceled = false;
                    }
                }
            }
        } else {
            assert waitingList == null;
            canceled = true;
        }

        if (canceledRms != null) {
            final List<Callback> _canceledRms = canceledRms;
            Protocol.invokeLater(new Runnable() {
                public void run() {
                    for (Callback canceledRm : _canceledRms) {
                        handleCanceledRm(canceledRm);
                    }
                }
            });
        }

        return canceled;
    }

	/**
	 * Resets the cache, setting the data to null and the status to
	 * INVALID_STATUS. When in the invalid state, neither the data nor the
	 * status can be queried.
	 */
    public void reset() {
        set(null, INVALID_STATUS, false);
    }

	/**
	 * Sets data and error values into the cache, and optionally puts in valid
	 * state. Note that data may be null and status may be an error status.
	 * 'Valid' simply means that our data is not stale. In other words, if the
	 * request to the source encounters an error, the cache object becomes valid
	 * all the same. The status indicates what error was encountered.
	 *
	 * <p>
	 * This method is called internally, typically in response to having
	 * obtained the result from the asynchronous request to the source. The
	 * data/status will remain valid until the cache object receives an event
	 * notification from the source indicating otherwise.
	 *
	 * @param data
	 *            The data that should be returned to any clients waiting for
	 *            cache data and for clients requesting data until the cache is
	 *            invalidated.
	 * @param error The status that should be returned to any clients waiting for
	 *         cache data and for clients requesting data until the cache is
	 *         invalidated
	 * @param valid Whether the cache should bet set in valid state.  If false,
	 *            any callback waiting for data are completed but the cache
	 *            is moved back to invalid state.
	 *
	 * @see #reset
	 */
    public void set(V data, Throwable error, boolean valid) {
        assert Protocol.isDispatchThread();

        fData = data;
        fError = error;
        fValid = valid;

        completeWaitingRms();
    }

}
