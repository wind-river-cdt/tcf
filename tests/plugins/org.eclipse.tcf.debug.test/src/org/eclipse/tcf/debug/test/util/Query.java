/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.test.util;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.tcf.protocol.Protocol;


/**
 * Copied and adapted from org.eclipse.cdt.dsf.concurrent.
 * 
 * A convenience class that allows a client to retrieve data from services 
 * synchronously from a non-dispatch thread.  This class is different from
 * a Callable<V> in that it allows the implementation code to calculate
 * the result in several dispatches, rather than requiring it to return the 
 * data at end of Callable#call method.
 * <p>
 * Usage:<br/>
 * <pre>
 *     class DataQuery extends Query<Data> {
 *         protected void execute(DataCallback<Data> callback) {
 *             callback.setData(fSlowService.getData());
 *             callback.done();
 *         }
 *     }
 *     
 *     DsfExecutor executor = getExecutor();
 *     DataQuery query = new DataQuery();
 *     executor.submit(query);
 *     
 *     try {
 *         Data data = query.get();
 *     }
 *     
 * </pre>
 * <p> 
 * @see java.util.concurrent.Callable
 * 
 */
abstract public class Query<V> implements Future<V> 
{
    private class QueryCallback extends DataCallback<V> {

        boolean fExecuted = false;
        
        boolean fCompleted = false;
        
        private QueryCallback() { 
            super(null);
        }
        
        @Override
        public synchronized void handleCompleted() {
            fCompleted = true;
            notifyAll();
        }
    
        public synchronized boolean isCompleted() {
            return fCompleted;
        }
        
        public synchronized boolean setExecuted() {
            if (fExecuted || isCanceled()) {
                // already executed or canceled
                return false;
            } 
            fExecuted = true;
            return true;
        }
    };
    
    private final QueryCallback fCallback = new QueryCallback();
    
    /** 
     * The no-argument constructor 
     */
    public Query() {}

    public V get() throws InterruptedException, ExecutionException {
        invoke();
        Throwable error;
        V data;
        synchronized (fCallback) {
            while (!isDone()) {
                fCallback.wait();
            }
            error = fCallback.getError();
            data = fCallback.getData();
        }
        
        if (error instanceof CancellationException) {
            throw new CancellationException();
        } else if (error != null) {
            throw new ExecutionException(error);
        }
        return data;
    }

    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        invoke();

        long timeLeft = unit.toMillis(timeout);
        long timeoutTime = System.currentTimeMillis() + unit.toMillis(timeout);

        Throwable error;
        V data;
        synchronized (fCallback) {
            while (!isDone()) {
                if (timeLeft <= 0) {
                    throw new TimeoutException();
                }
                fCallback.wait(timeLeft);
                timeLeft = timeoutTime - System.currentTimeMillis();
            }
            error = fCallback.getError();
            data = fCallback.getData();
        }
        
        if (error instanceof CancellationException) {
            throw new CancellationException();
        } else if (error != null) {
            throw new ExecutionException(error);
        }
        return data;
    }

    /**
     * Don't try to interrupt the DSF executor thread, just ignore the request 
     * if set.
     */
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean completed = false;
        synchronized (fCallback) {
            completed = fCallback.isCompleted();
            if (!completed) {
                fCallback.cancel();
                fCallback.notifyAll();
            }
        }
        return !completed; 
    }

    public boolean isCancelled() { return fCallback.isCanceled(); }

    public boolean isDone() {
        synchronized (fCallback) {
            // If future is canceled, return right away.
            return fCallback.isCompleted() || fCallback.isCanceled();
        }
    }

    abstract protected void execute(DataCallback<V> callback);
    
    public void invoke() {
        Protocol.invokeLater(new Runnable() {
            public void run() {
                if (fCallback.setExecuted()) {
                    execute(fCallback);
                }
            }
        });
    }
}

