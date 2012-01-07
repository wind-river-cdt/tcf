/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.test.services;

import java.util.Map;

import org.eclipse.tcf.debug.test.util.ICache;
import org.eclipse.tcf.debug.test.util.TokenCache;
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

    private class ChildrenCache extends TokenCache<String[]> implements IStackTrace.DoneGetChildren {
        private final String fId;
        public ChildrenCache(String id) {
            fId = id;
        }
        
        @Override
        protected IToken retrieveToken() {
            return fService.getChildren(fId, this);
        }
        public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
            set(token, context_ids, error);
        }
        public void resetChildren() {
            if (isValid()) reset();
        }
    };

    private class ChildrenCacheKey extends IdKey<ChildrenCache> {
        public ChildrenCacheKey(String id) {
            super(ChildrenCache.class, id);
        }
        @Override ChildrenCache createCache() { return new ChildrenCache(fId); }        
    }
    
    public ICache<String[]> getChildren(String id) {
        return mapCache(new ChildrenCacheKey(id));
    }

    class ContextCache extends TokenCache<StackTraceContext> implements IStackTrace.DoneGetContext {
        private final String fId;
        
        public ContextCache(String id) {
            fId = id;
        }
        @Override
        protected IToken retrieveToken() {
            return fService.getContext(new String[] { fId }, this);
        }
        public void doneGetContext(IToken token, Exception error, StackTraceContext[] contexts) {
            StackTraceContext context = contexts != null && contexts.length > 0 ? contexts[0] : null;
            set(token, context, error);
        }
        public void resetContext() {
            if (isValid()) reset();
        }
    }
    
    private class ContextCacheKey extends IdKey<ContextCache> {
        public ContextCacheKey(String id) {
            super(ContextCache.class, id);
        }
        @Override ContextCache createCache() { return new ContextCache(fId); }        
    }
    
    public ICache<StackTraceContext>[] getContext(final String[] ids) {
        @SuppressWarnings("unchecked")
        ICache<StackTraceContext>[] caches = (ICache<StackTraceContext>[])new ICache[ids.length];
        for (int i = 0; i < ids.length; i++) {
            caches[i] = mapCache(new ContextCacheKey(ids[i]));
        }
        return caches;
    }

    public void contextAdded(RunControlContext[] contexts) {
        // TODO Auto-generated method stub
        
    }

    public void contextChanged(RunControlContext[] contexts) {
        for (RunControlContext context : contexts) {
            resetRunControlContext(context.getID());
        }
    }

    public void contextRemoved(String[] context_ids) {
        for (String id : context_ids) {
            resetRunControlContext(id);
        }
    }

    public void contextSuspended(String context, String pc, String reason, Map<String, Object> params) {
        resetRunControlContext(context);
    }

    public void contextResumed(String context) {
        resetRunControlContext(context);
    }

    public void containerSuspended(String context, String pc, String reason, Map<String, Object> params,
        String[] suspended_ids) 
    {
        for (String id : suspended_ids) {
            resetRunControlContext(id);
        }
    }

    public void containerResumed(String[] context_ids) {
        for (String id : context_ids) {
            resetRunControlContext(id);
        }
    }

    public void contextException(String context, String msg) {
        resetRunControlContext(context);
    }

    private void resetRunControlContext(String id) {
        ChildrenCache childrenCache = getCache(new ChildrenCacheKey(id));
        if (childrenCache != null && childrenCache.isValid() && childrenCache.getData() != null) {
            String[] frameIds = childrenCache.getData();
            for (String frameId : frameIds) {
                ContextCache contextCache = getCache(new ContextCacheKey(frameId));
                if (contextCache != null) {
                    contextCache.resetContext();
                }
            }
            childrenCache.resetChildren();
        }
    }
}
