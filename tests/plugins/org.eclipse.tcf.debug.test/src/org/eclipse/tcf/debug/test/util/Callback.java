/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems and others.
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

import org.eclipse.tcf.protocol.Protocol;

/**
 * Copied and apdapted from org.eclipse.cdt.dsf.concurrent.
 * 
 * Used to monitor the result of an asynchronous request.  Because of the
 * asynchronous nature of DSF code, a very large number of methods needs to 
 * signal the result of an operation through a call-back.  This class is the base 
 * class for such call backs.  
 * <p>
 * The intended use of this class, is that a client who is calling an asynchronous 
 * method, will sub-class Callback, and implement the method {@link #handleCompleted()}, 
 * or any of the other <code>handle...</code> methods, in order to interpret the 
 * results of the request.  The object implementing the asynchronous method is required 
 * to call the {@link #done()} method on the request monitor object that it received 
 * as an argument.  
 * </p>
 * <p>
 * The error the returned by #getError() can be used to 
 * determine the success or failure of the asynchronous operation.  By convention
 * the error value returned by asynchronous method should be interpreted as follows:
 * <ul>
 * <li><code>null</code> - Result is a success.  In DataCallback, getData() should
 *     return a value.</li>
 * <li>non-<code>null</code> - An error condition that should probably be reported 
 *     to the user.</li>
 * <li><code>CancellationException</code> - The request was canceled, and the 
 *     asynchronous method was not completed.</li>
 * </ul>  
 * </p>
 * <p>
 * The Callback constructor accepts an optional "parent" request monitor.  If a 
 * parent monitor is specified, it will automatically be invoked by this monitor when 
 * the request is completed.  The parent option is useful when implementing a method 
 * which is asynchronous (and accepts a request monitor as an argument) and which itself 
 * calls another asynchronous method to complete its operation.  For example, in the 
 * request monitor implementation below, the implementation only needs to override 
 * <code>handleSuccess()</code>, because the base implementation will handle notifying the 
 * parent <code>rm</code> in case the <code>getIngredients()</code> call fails. 
 * <pre>
 *     public void createCupCakes(final DataCallback<CupCake[]> rm) {
 *         getIngredients(new DataCallback<Ingredients>(fExecutor, rm) {
 *                 public void handleSuccess() {
 *                     rm.setData( new CupCake(getData().getFlour(), getData().getSugar(), 
 *                                             getData().getBakingPowder()));
 *                     rm.done();  
 *                 }
 *             });
 *     }
 * </pre>
 * </p>
 * 
 * @since 1.0
 */
public class Callback {
    
    /**
     * Interface used by Callback to notify when a given request monitor
     * is canceled.  
     * 
     * @see Callback
     */
    public static interface ICanceledListener {
        
        /**
         * Called when the given request monitor is canceled.
         */
        public void requestCanceled(Callback rm);
    }

    /**
     * The request monitor which was used to call into the method that created this 
     * monitor.  
     */
    private final Callback fParentCallback;

    private List<ICanceledListener> fCancelListeners;
    
    /**
     * Status 
     */
    private Throwable fError = null;
    private boolean fCanceled = false;
    private boolean fDone = false;

    private final ICanceledListener fCanceledListener;
    
	/**
	 * This field is never read by any code; its purpose is strictly to assist
	 * developers debug DPF code. Developer can select this field in the
	 * Variables view and see a monitor backtrace in the details pane. See
	 * {@link DsfExecutable#DEBUG_MONITORS}.
	 * 
	 * <p>
	 * This field is set only when tracing is enabled.
	 */
	@SuppressWarnings("unused")
	private String fMonitorBacktrace;

    public Callback() {
        this(null);
    }
	
	/**
	 * Constructor with an optional parent monitor.
	 * 
	 * @param executor
	 *            This executor will be used to invoke the runnable that will
	 *            allow processing the completion code of this request monitor.
	 *            I.e., the runnable will call {@link #handleCompleted()}.
	 * @param parentCallback
	 *            An optional parent request monitor. By default, our completion
	 *            handlers invoke the parent monitor's <code>done</code> method,
	 *            thus allowing monitors to be daisy chained. If this request is
	 *            unsuccessful, its status is set into the parent monitor.
	 *            Parameter may be null.
	 */
    public Callback(Callback parentCallback) {
        fParentCallback = parentCallback;
        
        // If the parent rm is not null, add ourselves as a listener so that 
        // this request monitor will automatically be canceled when the parent
        // is canceled.
        if (fParentCallback != null) {
            fCanceledListener = new ICanceledListener() {
                public void requestCanceled(Callback rm) {
                    cancel();
                }
            };

            fParentCallback.addCancelListener(fCanceledListener);
        } else {
            fCanceledListener = null;
        }
    }
    
    /** 
     * Sets the status of the result of the request.  If status is OK, this 
     * method does not need to be called. 
     */
    public synchronized void setError(Throwable error) { 
        fError = error; 
    }
    
    /** Returns the status of the completed method. */
    public synchronized Throwable getError() {
        if (isCanceled()) {
            return new CancellationException();
        }
        return fError; 
    }

	/**
	 * Sets this request monitor as canceled and calls the cancel listeners if
	 * any.
	 * <p>
	 * Note: Calling cancel() does not automatically complete the
	 * Callback. The asynchronous call still has to call done().
	 * </p>
	 * <p>
	 * Note: logically a request should only be canceled by the client that
	 * issued the request in the first place. After a request is canceled, the
	 * method that is fulfilling the request may call
	 * {@link #setError(Throwable)} with <code>CancelledException</code>
	 * to indicate that it recognized that the given request was canceled and it
	 * did not perform the given operation.
	 * </p>
	 * <p>
	 * Canceling a monitor effectively cancels all descendant monitors, by
	 * virtue of the default implementation of {@link #isCanceled()}, which
	 * checks not only its own state but that of its parent. However, only the
	 * cancel listeners of the monitor directly canceled will be called.
	 * </p>
	 */
    public void cancel() {
        ICanceledListener[] listeners = null;
        synchronized (this) {
            // Check to make sure the request monitor wasn't previously canceled.
            if (!fCanceled) {
                fCanceled = true;
                if (fCancelListeners != null) {
                    listeners = fCancelListeners.toArray(new ICanceledListener[fCancelListeners.size()]);
                }
            }
        }

        // Call the listeners outside of a synchronized section to reduce the 
        // risk of deadlocks.
        if (listeners != null) {
            for (ICanceledListener listener : listeners) {
                listener.requestCanceled(this);
            }
        }
    }
    
    /**
     * Returns whether the request was canceled.  Even if the request is
     * canceled by the client, the implementor handling the request should 
     * still call {@link #done()} in order to complete handling 
     * of the request monitor. 
     * 
     * <p>
     * A request monitor is considered canceled if either it or its parent was canceled.
     * </p> 
     */
    public boolean isCanceled() {
        boolean canceled = false;
        
        // Avoid holding onto this lock while calling parent RM, which may 
        // acquire other locks (bug 329488).
        synchronized(this) {
            canceled = fCanceled;
        }
        return canceled || (fParentCallback != null && fParentCallback.isCanceled());
    }
    
    /**
     * Adds the given listener to list of listeners that are notified when this 
     * request monitor is directly canceled.
     */
    public synchronized void addCancelListener(ICanceledListener listener) {
        if (fCancelListeners == null) {
            fCancelListeners = new ArrayList<ICanceledListener>(1);
        }
        fCancelListeners.add(listener);
    }

    /**
     * Removes the given listener from the list of listeners that are notified 
     * when this request monitor is directly canceled.
     */
    public synchronized void removeCancelListener(ICanceledListener listener) {
        if (fCancelListeners != null) {
            fCancelListeners.remove(listener);
        }
    }

    public void done(Throwable error) {
        setError(error);
        done();
    }
    
    /**
     * Marks this request as completed.  Once this method is called, the
     * monitor submits a runnable to the DSF Executor to call the 
     * <code>handle...</code> methods.  
     * <p>
     * Note: This method should be called once and only once, for every request 
     * issued.  Even if the request was canceled.
     * </p>  
     */
    public void done() {
        synchronized(this) {
            assert Protocol.getEventQueue().isDispatchThread();
            
            if (fDone) {
                throw new IllegalStateException("Callback: " + this + ", done() method called more than once");  //$NON-NLS-1$//$NON-NLS-2$
            }
            fDone = true;
            
            // This Callback is done, it can no longer be canceled.
            // We must clear the list of cancelListeners because it causes a
            // circular reference between parent and child Callback, which
            // causes a memory leak.
            fCancelListeners = null;
            
            if (fParentCallback != null) {
                fParentCallback.removeCancelListener(fCanceledListener);
            }
        }
        
        handleCompleted();
    }

    @Override
    public String toString() {
        return "Callback (" + super.toString() + "): " + getError(); //$NON-NLS-1$ //$NON-NLS-2$
    }

	/**
	 * Checks whether the given request monitor completed with success or
	 * failure result. If the request monitor was canceled it is considered a
	 * failure, regardless of the status. If the status has a severity higher
	 * than INFO (i.e., WARNING, ERROR or CANCEL), it is considered a failure.
	 */
    public synchronized boolean isSuccess() {
        return !isCanceled() && getError() == null ; 
    }

	/**
	 * First tier handler for the completion of the request. By default, the
	 * {@link #done()} method drives this method on the executor specified at
	 * construction time. By default, this handler merely calls a more
	 * specialized handler, which in turn may call an even more specialized
	 * handler, and so on, thus giving a subclass the ability to
	 * compartmentalize its completion logic by overriding specific handlers.
	 * All handlers are named <code>handleXxxxx</code>. More specifically, the
	 * base implementation calls {@link #handleSuccess()} if the request
	 * succeeded, and calls {@link #handleFailure()} otherwise. <br>
	 * 
	 * The complete hierarchy of handlers is as follows: <br>
	 * <pre>
	 * + handleCompleted 
	 *   - handleSuccess 
	 *   + handleFailure 
	 *     - handleCancel
	 *     + handleErrororWarning 
	 *       - handleError 
	 *       - handleWarning
	 * </pre>
	 * 
	 * <p>
	 * Note: Sub-classes may override this method.
	 */
    protected void handleCompleted() {
        if (isSuccess()) {
            handleSuccess();
        } else {
            handleFailure();
        } 
    }
    
    /**
     * Default handler for a successful the completion of a request.  If this 
     * monitor has a parent monitor that was configured by the constructor, that 
     * parent monitor is notified.  Otherwise this method does nothing. 
     * {@link #handleFailure()} or cancel otherwise.
     * <br>
     * Note: Sub-classes may override this method.
     */
    protected void handleSuccess() {
        if (fParentCallback != null) {
            fParentCallback.done();
        }
    }
    
    /**
     * The default implementation of a cancellation or an error result of a 
     * request.  The implementation delegates to {@link #handleCancel()} and
     * {@link #handleErrorOrWarning()} as needed.
     * <br>
     * Note: Sub-classes may override this method.
     */
    protected void handleFailure() {
        assert !isSuccess();
        
        if (isCanceled()) {
            handleCancel();
        } else {
            handleError();
        } 
    }
    
    /**
     * The default implementation of an error result of a request.  If this 
     * monitor has a parent monitor that was configured by the constructor, that 
     * parent monitor is configured with a new status containing this error.
     * Otherwise the error is logged.  
     * <br>
     * Note: Sub-classes may override this method.
     */    
    protected void handleError() {
        if (fParentCallback != null) {
            fParentCallback.setError(getError());
            fParentCallback.done();
        } else {
            Protocol.log("Unhandled error in callback " + toString(), getError());
        }
    }
    

	/**
	 * Default completion handler for a canceled request. If this monitor was
	 * constructed with a parent monitor, the status is propagated up to it.
	 * Otherwise this method does nothing. <br>
	 * Note: Sub-classes may override this method.
	 */
    protected void handleCancel() {
        if (fParentCallback != null) {
            if (getError() instanceof CancellationException && !fParentCallback.isCanceled()) {
                Protocol.log("Sub-request " + toString() + " was canceled and not handled.'", getError());
            } else {
                fParentCallback.setError(getError());
            }
            fParentCallback.done();
        }
    }
}
