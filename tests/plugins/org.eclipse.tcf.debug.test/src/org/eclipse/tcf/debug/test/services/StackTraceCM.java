/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.test.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.tcf.debug.test.services.ResetMap.IResettable;
import org.eclipse.tcf.debug.test.util.DataCallback;
import org.eclipse.tcf.debug.test.util.ICache;
import org.eclipse.tcf.debug.test.util.RangeCache;
import org.eclipse.tcf.debug.test.util.TokenCache;
import org.eclipse.tcf.debug.test.util.Transaction;
import org.eclipse.tcf.debug.test.util.TransactionCache;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IMemory;
import org.eclipse.tcf.services.IMemory.MemoryContext;
import org.eclipse.tcf.services.IMemoryMap;
import org.eclipse.tcf.services.IRunControl;
import org.eclipse.tcf.services.IRunControl.RunControlContext;
import org.eclipse.tcf.services.IStackTrace;
import org.eclipse.tcf.services.IStackTrace.StackTraceContext;

/**
 * 
 */
public class StackTraceCM extends AbstractCacheManager {
    private IStackTrace fService;
    private RunControlCM fRunControlCM;
    private IMemory fMemory;
    private IMemoryMap fMemoryMap;
    private final ResetMap fRunControlStateResetMap = new ResetMap();
    private final ResetMap fMemoryResetMap = new ResetMap();
    
    /**
     * Listener public for testing purposes only.
     */
    public final IRunControl.RunControlListener fRunControlListener = new IRunControl.RunControlListener() {
        @Override
        public void contextAdded(RunControlContext[] contexts) {
        }

        @Override
        public void contextChanged(RunControlContext[] contexts) {
            for (RunControlContext context : contexts) fRunControlStateResetMap.reset(context.getID());
        }

        @Override
        public void contextRemoved(String[] context_ids) {
            for (String id : context_ids) fRunControlStateResetMap.reset(id);
        }

        @Override
        public void contextSuspended(String context, String pc, String reason, Map<String, Object> params) {
            fRunControlStateResetMap.reset(context);
        }

        @Override
        public void contextResumed(String context) {
            fRunControlStateResetMap.reset(context);
        }

        @Override
        public void containerSuspended(String context, String pc, String reason, Map<String, Object> params,
            String[] suspended_ids) 
        {
            for (String id : suspended_ids) fRunControlStateResetMap.reset(id);
        }

        @Override
        public void containerResumed(String[] context_ids) {
            for (String id : context_ids) fRunControlStateResetMap.reset(id);
        }

        @Override
        public void contextException(String context, String msg) {
            fRunControlStateResetMap.reset(context);
        }
    };
    
    public final IMemory.MemoryListener fMemoryListener = new IMemory.MemoryListener() {
        
        @Override
        public void contextAdded(MemoryContext[] contexts) {
        }
        
        @Override
        public void contextChanged(MemoryContext[] contexts) {
            for (MemoryContext context : contexts) fMemoryResetMap.reset(context.getID());
        }
        
        public void contextRemoved(String[] context_ids) {
            for (String context_id : context_ids) fMemoryResetMap.reset(context_id);
            
        };
        
        @Override
        public void memoryChanged(String context_id, Number[] addr, long[] size) {
            fMemoryResetMap.reset(context_id);
        }
        
    };

    public final IMemoryMap.MemoryMapListener fMemoryMapListener = new IMemoryMap.MemoryMapListener() {
        @Override
        public void changed(String context_id) {
            // Memory Map changed
            fMemoryResetMap.reset(context_id);
        }
    };
    
    public StackTraceCM(IStackTrace service, RunControlCM runControlCM, IMemory memory, IMemoryMap memoryMap) {
        fService = service;
        fRunControlCM = runControlCM;
        fRunControlCM.getService().addListener(fRunControlListener);
        fMemory = memory;
        fMemory.addListener(fMemoryListener);
        fMemoryMap = memoryMap;
        fMemoryMap.addListener(fMemoryMapListener);
    }

    @Override
    public void dispose() {
        fRunControlCM.getService().removeListener(fRunControlListener);
        fMemory.removeListener(fMemoryListener);
        fMemoryMap.removeListener(fMemoryMapListener);
        super.dispose();
    }

    public ICache<String[]> getChildren(final String id) {
        class MyCache extends TransactionCache<String[]> {

            class InnerCache extends TokenCache<String[]> implements IStackTrace.DoneGetChildren {
                @Override
                protected IToken retrieveToken() {
                    return fService.getChildren(id, this);
                }
                
                @Override
                public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
                    set(token, context_ids, error);
                }
            };
            
            private final InnerCache fInner = new InnerCache();
            
            @Override
            protected String[] process() throws InvalidCacheException, ExecutionException {
                RunControlContext rcContext = validate(fRunControlCM.getContext(id));
                validate(fInner);
                fRunControlStateResetMap.addValid(id, fInner);
                fMemoryResetMap.addValid(rcContext.getProcessID(), fInner);
                return fInner.getData();
            }
        };

        return mapCache(new IdKey<MyCache>(MyCache.class, id) {
            @Override MyCache createCache() { return new MyCache(); }
        });
    }

    public RangeCache<StackTraceContext> getContextRange(final String parentId) {
        
        class MyCache extends RangeCache<StackTraceContext> implements IResettable {
            boolean fIsValid = false;
            @Override
            protected void retrieve(final long offset, final int count, DataCallback<List<StackTraceContext>> rm) {
                new Transaction<List<StackTraceContext>>() {
                    @Override
                    protected List<StackTraceContext> process() throws InvalidCacheException, ExecutionException {
                        String[] ids = validate(getChildren(parentId));
                        int adjustedCount = Math.min(count, ids.length + (int)offset);
                        String[] subIds = new String[adjustedCount]; 
                        System.arraycopy(ids, (int)offset, subIds, 0, adjustedCount);
                        StackTraceContext[] contexts = validate(getContexts(subIds));
                        RunControlContext rcContext = validate(fRunControlCM.getContext(parentId));
                        if (!fIsValid) {
                            fRunControlStateResetMap.addValid(parentId, MyCache.this);
                            fMemoryResetMap.addValid(rcContext.getProcessID(), MyCache.this);
                        }
                        return Arrays.asList(contexts);
                        
                    }
                }.request(rm);
            }
            
            public void reset() {
                fIsValid = false;
                @SuppressWarnings("unchecked")
                List<StackTraceContext> emptyData = (List<StackTraceContext>)Collections.EMPTY_LIST;
                set(0, 0, emptyData, new Throwable("Cache invalid") );
            }
            
        };

        return mapCache(new IdKey<MyCache>(MyCache.class, parentId) {
            @Override MyCache createCache() { return new MyCache(); }        
        });
    }
    
    public ICache<StackTraceContext[]> getContexts(final String[] ids) {
        assert ids.length != 0;
        
        class MyCache extends TransactionCache<StackTraceContext[]> {
            class InnerCache extends TokenCache<StackTraceContext[]> implements IStackTrace.DoneGetContext {
                @Override
                protected IToken retrieveToken() {
                    return fService.getContext(ids, this);
                }
                
                public void doneGetContext(IToken token, Exception error, StackTraceContext[] contexts) {
                    set(token, contexts, error);
                }
            }
            
            private InnerCache fInner = new InnerCache();
            
            @Override
            protected StackTraceContext[] process() throws InvalidCacheException, ExecutionException {
                StackTraceContext[] contexts = validate(fInner);
                String threadId = contexts[0].getParentID();
                RunControlContext threadContext = validate(fRunControlCM.getContext(threadId));
                fRunControlStateResetMap.addValid(threadId, fInner);
                fMemoryResetMap.addValid(threadContext.getProcessID(), fInner);
                return contexts;
            }
        }
        
        return mapCache(new IdKey<MyCache>(MyCache.class, Arrays.toString(ids)) {
            @Override MyCache createCache() { return new MyCache(); }        
        });
    }
    
    
}
