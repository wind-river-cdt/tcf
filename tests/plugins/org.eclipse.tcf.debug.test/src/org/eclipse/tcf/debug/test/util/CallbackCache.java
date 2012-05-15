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

/**
 * A general purpose cache, which caches the result of a single request.
 * Sub classes need to implement {@link #retrieve(DataRequestMonitor)} to fetch
 * data from the data source.  Clients are responsible for calling
 * {@link #disable()} and {@link #reset()} to manage the state of the cache in
 * response to events from the data source.
 * <p>
 * This cache requires an executor to use.  The executor is used to synchronize
 * access to the cache state and data.
 * </p>
 * @since 2.2
 */
public abstract class CallbackCache<V> extends AbstractCache<V> {

    protected DataCallback<V> fRm;

    @Override
    protected final void retrieve() {
        // Make sure to cancel the previous rm.  This may lead to the rm being
        // canceled twice, but that's not harmful.
        if (fRm != null) {
            fRm.cancel();
        }

        fRm = new DataCallback<V>(null) {
            @Override
            protected void handleCompleted() {
                if (this == fRm) {
                    fRm = null;
                    CallbackCache.this.handleCompleted(getData(), getError(), isCanceled());
                }
            }

            @Override
            public boolean isCanceled() {
                return super.isCanceled() || CallbackCache.this.isCanceled();
            }
        };
        retrieve(fRm);
    }

    /**
     * Sub-classes should override this method to retrieve the cache data
     * from its source.
     *
     * @param rm Request monitor for completion of data retrieval.
     */
    protected abstract void retrieve(DataCallback<V> rm);

    protected void handleCompleted(V data, Throwable error, boolean canceled) {
        // If the requestor canceled the request, then leave the
        // cache as is, regardless of how the retrieval completes.
        // We want the cache to stay in the invalid state so that
        // it remains functional. The request may have completed
        // successfully, but using it may produce inconsistent
        // results.
        if (!canceled) {
            set(data, error, true);
        }
    }

    @Override
    protected synchronized void canceled() {
        if (fRm != null) {
            fRm.cancel();
        }
    }

    @Override
    public void set(V data, Throwable error, boolean valid) {
        if (fRm != null) {
            fRm.cancel();
            fRm = null;
        }
        super.set(data, error, valid);
    }

    @Override
    public void reset() {
        if (fRm != null) {
            fRm.cancel();
            fRm = null;
        }
        super.reset();
    }
}
