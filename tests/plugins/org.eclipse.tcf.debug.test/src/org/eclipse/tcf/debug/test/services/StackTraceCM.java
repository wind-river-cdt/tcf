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
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IRunControl;
import org.eclipse.tcf.services.IRunControl.RunControlContext;
import org.eclipse.tcf.services.IStackTrace;
import org.eclipse.tcf.services.IStackTrace.StackTraceContext;

/**
 * 
 */
public class StackTraceCM extends AbstractCacheManager implements IRunControl.RunControlListener  {
    private IStackTrace fService;
    private IRunControl fRunControl;
    private final ResetMap fRunControlStateResetMap = new ResetMap();
    
    public StackTraceCM(IStackTrace service, IRunControl runControl) {
        fService = service;
        fRunControl = runControl;
        fRunControl.addListener(this);
    }

    @Override
    public void dispose() {
        fRunControl.removeListener(this);
        super.dispose();
    }

    public ICache<String[]> getChildren(final String id) {
        class MyCache extends TokenCache<String[]> implements IStackTrace.DoneGetChildren {
            @Override
            protected IToken retrieveToken() {
                return fService.getChildren(id, this);
            }
            
            public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
                fRunControlStateResetMap.addValid(id, this);
                set(token, context_ids, error);
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
                        if (!fIsValid) {
                            fRunControlStateResetMap.addValid(parentId, MyCache.this);
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
        class MyCache extends TokenCache<StackTraceContext[]> implements IStackTrace.DoneGetContext {
            @Override
            protected IToken retrieveToken() {
                fRunControlStateResetMap.addPending(this);
                return fService.getContext(ids, this);
            }
            
            public void doneGetContext(IToken token, Exception error, StackTraceContext[] contexts) {
                fRunControlStateResetMap.removePending(this);
                fRunControlStateResetMap.addValid(contexts[0].getParentID(), this);
                set(token, contexts, error);
            }
        }

        return mapCache(new IdKey<MyCache>(MyCache.class, Arrays.toString(ids)) {
            @Override MyCache createCache() { return new MyCache(); }        
        });
    }

    
    
    public void contextAdded(RunControlContext[] contexts) {
    }

    public void contextChanged(RunControlContext[] contexts) {
        for (RunControlContext context : contexts) {
            fRunControlStateResetMap.reset(context.getID());
        }
    }

    public void contextRemoved(String[] context_ids) {
        for (String id : context_ids) {
            fRunControlStateResetMap.reset(id);
        }
    }

    public void contextSuspended(String context, String pc, String reason, Map<String, Object> params) {
        fRunControlStateResetMap.reset(context);
    }

    public void contextResumed(String context) {
        fRunControlStateResetMap.reset(context);
    }

    public void containerSuspended(String context, String pc, String reason, Map<String, Object> params,
        String[] suspended_ids) 
    {
        for (String id : suspended_ids) {
            fRunControlStateResetMap.reset(id);
        }
    }

    public void containerResumed(String[] context_ids) {
        for (String id : context_ids) {
            fRunControlStateResetMap.reset(id);
        }
    }

    public void contextException(String context, String msg) {
        fRunControlStateResetMap.reset(context);
    }
}
