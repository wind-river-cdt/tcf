/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.test.util;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.tcf.protocol.Protocol;

/**
 * @since 2.2
 */
public abstract class Transaction<V>  implements Future<V>, Runnable {

	/**
	 * The exception we throw when the client transaction logic asks us to
	 * validate a cache object that is stale (or has never obtained a value from
	 * the source)
	 */
    public static final InvalidCacheException INVALID_CACHE_EXCEPTION = new InvalidCacheException();
    
	/** The request object we've been given to set the transaction results in */
    private DataCallback<V> fRm;
    
    private Query<V> fQuery;
    
    public static class InvalidCacheException extends Exception {
        private static final long serialVersionUID = 1L;
    }

	/**
	 * Kicks off the transaction. We'll either complete the request monitor
	 * immediately if all the data points the transaction needs are cached and
	 * valid, or we'll end up asynchronously completing the monitor if and when
	 * either (a) all the data points are available and up-to-date, or (b)
	 * obtaining them from the source encountered an error. Note that there is
	 * potential in (b) for us to never complete the monitor. If one or more
	 * data points are perpetually becoming stale, then we'll indefinitely wait
	 * for them to stabilize. The caller should cancel its request monitor in
	 * order to get us to stop waiting.
	 * 
	 * @param rm Request completion monitor.
	 */
    public void request(DataCallback<V> rm) {
        if (fRm != null) {
            assert fRm.isCanceled();
            fRm.done();
        }
        fRm = rm;
        assert fRm != null;
        run();
    }

    protected void preProcess() {}
    
    protected void postProcess(boolean done, V data, Throwable error) {}
    
    protected boolean processUnchecked() {
        try {
            // Execute the transaction logic
            V data = process();
            
            // No exception means all cache objects used by the transaction
            // were valid and up to date. Complete the request
            setData(data);
            return true;
        }
        catch (InvalidCacheException e) {
            // At least one of the cache objects was stale/unset. Keep the
            // request monitor in the incomplete state, thus leaving our client
            // "waiting" (asynchronously). We'll get called again once the cache
            // objects are updated, thus re-starting the whole transaction
            // attempt.
            return false;
        }
        catch (Throwable e) {
            // At least one of the cache objects encountered a failure obtaining
            // the data from the source. Complete the request.
            setError(e);
            return true;
        }
    }
    
	/**
	 * The transaction logic--code that tries to synchronously make use of,
	 * usually, multiple data points that are normally obtained asynchronously.
	 * Each data point is represented by a cache object. The transaction logic
	 * must check the validity of each cache object just prior to using it
	 * (calling its getData()). It should do that check by calling one of our
	 * validate() methods. Those methods will throw InvalidCacheException if the
	 * cached data is invalid (stale, e.g.,) or CoreException if an error was
	 * encountered the last time it got data form the source. The exception will
	 * abort the transaction, but in the case of InvalidCacheException, we
	 * schedule an asynchronous call that will re-invoke the transaction
	 * logic once the cache object has been updated from the source.
	 * 
	 * @return the cached data if it's valid, otherwise an exception is thrown
     * @throws Transaction.InvalidCacheException Exception indicating that a 
     * cache is not valid and transaction will need to be rescheduled.
     * @throws CoreException Exception indicating that one of the caches is 
     * in error state and transaction cannot be processed.
	 */
    protected V process() throws InvalidCacheException, ExecutionException {
        return null;
    }

    /**
     * Can be called only while in process().
     * @param data
     */
    protected void setData(V data) {
        assert Protocol.isDispatchThread();
        fRm.setData(data);
    }
    
    /**
     * Can be called only while in process().
     * @param data
     */
    protected void setError(Throwable error) {
        assert Protocol.isDispatchThread();
        fRm.setError(error);
    }
    
	/**
	 * Method which invokes the transaction logic and handles any exception that
	 * may result. If that logic encounters a stale/unset cache object, then we
	 * simply do nothing. This method can be invoked by transaction logic when 
	 * caches have become valid, thus unblocking transaction processing.  
	 */
    public void run() {
        // If execute is called after transaction completes (as a result of a 
        // cancelled request completing for example), ignore it. 
        if (fRm == null) {
            return;
        }
        
        if (fRm.isCanceled()) {
            fRm.done();
            fRm = null;
            return;
        }

        preProcess();
        if (processUnchecked()) {
            postProcess(true, fRm.getData(), fRm.getError());
            fRm.done();
            fRm = null;
        } else {
            postProcess(false, null, null);
        }
    }

	/**
	 * Clients must call one of the validate methods prior to using (calling
	 * getData()) on data cache object.  
	 * 
	 * @param cache
	 *            the object being validated
	 * @throws InvalidCacheException
	 *             if the data is stale/unset
	 * @throws ExecutionException
	 *             if an error was encountered getting the data from the source
	 */
    public <T> T validate(ICache<T> cache) throws InvalidCacheException, ExecutionException {
        if (cache.isValid()) {
            if (cache.getError() != null) {
                throw new ExecutionException(cache.getError());
            }
            return cache.getData();
        } else {
			// Throw the invalid cache exception, but first ask the cache to
			// update itself from its source, and schedule a re-attempt of the
			// transaction logic to occur when the stale/unset cache has been
			// updated
            cache.wait(new Callback(fRm) {
                @Override
                protected void handleCompleted() {
                    run();
                }
            });
            throw INVALID_CACHE_EXCEPTION;
        }
    }

    /**
     * See {@link #validate(ICache)}. This variant simply validates
     * multiple cache objects.
     */
    public void  validate(ICache<?> ... caches) throws InvalidCacheException, ExecutionException {
        validate(Arrays.asList(caches));
    }

    /**
     * See {@link #validate(ICache)}. This variant validates
     * multiple cache objects.
     */
    public void validate(@SuppressWarnings("rawtypes") Iterable caches) throws InvalidCacheException, ExecutionException {
        // Check if any of the caches have errors:
        boolean allValid = true;
        
        for (Object cacheObj : caches) {
            ICache<?> cache = (ICache<?>)cacheObj;
            if (cache.isValid()) {
                if (cache.getError() != null) {
                    throw new ExecutionException(cache.getError());
                }
            } else {
                allValid = false;
            }
        }
        if (!allValid) {
            // Throw the invalid cache exception, but first schedule a
            // re-attempt of the transaction logic, to occur when the
            // stale/unset cache objects have been updated
            AggregateCallback countringRm = new AggregateCallback(fRm) {
                @Override
                protected void handleCompleted() {
                    run();
                }
            };
            int count = 0;
            for (Object cacheObj : caches) {
                ICache<?> cache = (ICache<?>)cacheObj;
                if (!cache.isValid()) {
                    cache.wait(countringRm);
                    count++;
                }
            }
            countringRm.setDoneCount(count);
            throw INVALID_CACHE_EXCEPTION;
        }        
    }

    /**
     * See {@link #validate(ICache)}.  This variant does not throw exceptions, 
     * instead it returns <code>false</code> if the cache is not valid.  If the 
     * given cache is valid, and this method returns <code>true</code>, clients 
     * must still check if the cache contains an error before retrieving its 
     * data through {@link ICache#getData()}.
     * 
     * @param cache the object being validated
     * @return returns <code>false</code> if the cache is not yet valid and 
     * transaction processing should be interrupted.
     */
    public boolean validateUnchecked(ICache<?> cache) {
        if (cache.isValid()) {
            return true;
        } else {
            // Just sk the cache to update itself from its source, and schedule a 
            // re-attempt of the transaction logic to occur when the stale/unset 
            // cache has been updated
            cache.wait(new Callback(fRm) {
                @Override
                protected void handleCompleted() {
                    run();
                }
            });
            return false;
        }        
    }
    
    /**
     * See {@link #validate(ICache)}. This variant validates
     * multiple cache objects.
     */
    public boolean validateUnchecked(ICache<?> ... caches) {
        return validateUnchecked(Arrays.asList(caches));
    }

    
    /**
     * See {@link #validate(ICache)}. This variant validates
     * multiple cache objects.
     */
    public boolean validateUnchecked(@SuppressWarnings("rawtypes") Iterable caches) {
        // Check if all caches are valid
        boolean allValid = true;
        
        for (Object cacheObj : caches) {
            ICache<?> cache = (ICache<?>)cacheObj;
            if (!cache.isValid()) {
                allValid = false;
            }
        }
        if (allValid) {
            return true;
        }
        
        // Just schedule a re-attempt of the transaction logic, to occur 
        // when the stale/unset cache objects have been updated
        AggregateCallback countringRm = new AggregateCallback(fRm) {
            @Override
            protected void handleCompleted() {
                run();
            }
        };
        int count = 0;
        for (Object cacheObj : caches) {
            ICache<?> cache = (ICache<?>)cacheObj;
            if (!cache.isValid()) {
                cache.wait(countringRm);
                count++;
            }
        }
        countringRm.setDoneCount(count);
        return false;
    }

    private synchronized Query<V> getQuery(boolean create) {
        if (fQuery == null && create) {
            fQuery = new Query<V>() {
                @Override
                protected void execute(DataCallback<V> callback) {
                    request(callback);
                }
            };
            fQuery.invoke();
        }
        
        return fQuery;
    }
    
    public boolean cancel(boolean mayInterruptIfRunning) {
        Query<V> query = getQuery(false);
        if (query != null) {
            return query.cancel(mayInterruptIfRunning);
        }
        return false;
    }

    public boolean isCancelled() {
        Query<V> query = getQuery(false);
        if (query != null) {
            return query.isCancelled();
        }
        return false;
    }

    public boolean isDone() {
        Query<V> query = getQuery(false);
        if (query != null) {
            return query.isDone();
        }
        return false;
    }

    public V get() throws InterruptedException, ExecutionException {
        return getQuery(true).get();
    }

    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return getQuery(true).get(timeout, unit);
    }

    
}
