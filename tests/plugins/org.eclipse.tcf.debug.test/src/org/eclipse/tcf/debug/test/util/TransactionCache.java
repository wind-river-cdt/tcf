/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems and others.
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
import java.util.concurrent.ExecutionException;


/**
 * 
 */
public abstract class TransactionCache<V> extends Transaction<V> implements ICache<V> {

    private List<ICache<?>> fDependsOn;
    private List<Callback> fDependsOnCallbacks;
    
    private CallbackCache<V> fCache = new CallbackCache<V>() {
        @Override
        protected void retrieve(DataCallback<V> rm) {
            request(rm);
        }
    };

    public V getData() {
        return fCache.getData();
    }

    public Throwable getError() {
        return fCache.getError();
    }

    public void wait(Callback rm) {
        fCache.wait(rm);
    }

    public boolean isValid() {
        return fCache.isValid();
    }
    
    @Override
    protected void preProcess() {
        super.preProcess();
        
        if (fDependsOnCallbacks != null) {
            for (Callback cb : fDependsOnCallbacks) {
                cb.cancel();
            }
            fDependsOnCallbacks = null;
        }
        fDependsOn = new ArrayList<ICache<?>>(4);
    }
    
    protected void postProcess(boolean done, V data, Throwable error) {
        if (done) {
            fDependsOnCallbacks = new ArrayList<Callback>(fDependsOn.size());
            for (ICache<?> cache : fDependsOn) {
                assert cache.isValid();
                cache.wait(new Callback() {
                    @Override
                    protected void handleCompleted() {
                        if (!isCancelled()) {
                            fCache.reset();
                            for (Callback cb : fDependsOnCallbacks) {
                                if (cb == this) continue;
                                cb.cancel();
                            }
                        }
                    }
                });
            }
        } else {
            fDependsOn = null;
        }
        super.postProcess(done, data, error);
    }
    
    /**
     * Can be called while in {@link #process()}
     * @param cache
     */
    public void addDependsOn(ICache<?> cache) {
        fDependsOn.add(cache);
    }
    
    public <T> T validate(ICache<T> cache) throws InvalidCacheException, ExecutionException {
        if (cache.isValid()) {
            addDependsOn(cache);
        }
        return super.validate(cache);
    }
    
    @Override
    public void validate(@SuppressWarnings("rawtypes") Iterable caches) throws InvalidCacheException, ExecutionException {
        for (Object cacheObj : caches) {
            ICache<?> cache = (ICache<?>)cacheObj;
            if (cache.isValid()) {
                addDependsOn(cache);
            }
        }
        super.validate(caches);
    }
    
    @Override
    public boolean validateUnchecked(ICache<?> cache) {
        if (cache.isValid()) {
            addDependsOn(cache);
        }
        return super.validateUnchecked(cache);
    }
    
    @Override
    public boolean validateUnchecked(@SuppressWarnings("rawtypes") Iterable caches) {
        for (Object cacheObj : caches) {
            ICache<?> cache = (ICache<?>)cacheObj;
            if (cache.isValid()) {
                addDependsOn(cache);
            }
        }
        return super.validateUnchecked(caches);
    }
}
