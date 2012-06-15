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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import org.eclipse.tcf.debug.test.services.ResetMap.IResettable;
import org.eclipse.tcf.debug.test.util.CallbackCache;
import org.eclipse.tcf.debug.test.util.DataCallback;
import org.eclipse.tcf.debug.test.util.ICache;
import org.eclipse.tcf.debug.test.util.TokenCache;
import org.eclipse.tcf.debug.test.util.Transaction;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IRunControl;
import org.eclipse.tcf.services.IRunControl.DoneCommand;
import org.eclipse.tcf.services.IRunControl.RunControlContext;
import org.eclipse.tcf.services.IRunControl.RunControlListener;

/**
 * 
 */
public class RunControlCM extends AbstractCacheManager implements RunControlListener {

    private final IRunControl fService;
    private final ResetMap fStateResetMap = new ResetMap();
    private final ResetMap fChildrenResetMap = new ResetMap();
    private final List<RunControlListener> fListeners = new ArrayList<RunControlListener>();
    
    public RunControlCM(IRunControl service) {
        fService = service;
        fService.addListener(this);
    }
    
    public void dispose() {
        fService.removeListener(this);
        super.dispose();
    }

    public void addListener(RunControlListener listener) {
        fListeners.add(listener);
    }
    
    public void removeListener(RunControlListener listener) {
        fListeners.remove(listener);
    }
    
    public IRunControl getService() { 
        return fService;
    }
    
    public ICache<RunControlContext> getContext(final String id) {
        class MyCache extends RunControlTokenCache<RunControlContext> implements IRunControl.DoneGetContext {
            @Override
            protected String getId() {
                return id;
            }
            @Override
            protected IToken retrieveToken() {
                return fService.getContext(id, this);
            }
            public void doneGetContext(IToken token, Exception error, RunControlContext context) {
                set(token, context, error);
            }
            
        };
        
        return mapCache(new IdKey<MyCache>(MyCache.class, id) {
                @Override MyCache createCache() { return new MyCache(); }
            });
    }
    
    private abstract class RunControlTokenCache<V> extends TokenCache<V> {
        abstract protected String getId();
        
        protected void set(IToken token, V data, Throwable error) {
            fStateResetMap.addValid(getId(), this);
            super.set(token, data, error);
        }
    }
    
    private class ChildrenCache extends TokenCache<String[]> implements IRunControl.DoneGetChildren {
        private final String fId;
        public ChildrenCache(String id) {
            fId = id;
        }
        
        @Override
        protected IToken retrieveToken() {
            return fService.getChildren(fId, this);
        }
        public void doneGetChildren(IToken token, Exception error, String[] child_ids) {
            fChildrenResetMap.addValid(fId, child_ids, this);
            set(token, child_ids, error);
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

    public static class ContextState {
        public final boolean suspended;
        public final String pc;
        public final String reason;
        public final Map<String, Object> params;
        ContextState(boolean suspended, String pc, String reason, Map<String, Object> params) {
            this.suspended = suspended;
            this.pc = pc;
            this.reason = reason;
            this.params = params;
        }
        
    }

    private class ContextStateCache extends CallbackCache<ContextState> implements IResettable {
        
        private class InnerContextStateCache extends TokenCache<ContextState> implements IRunControl.DoneGetState {
            private final RunControlContext fContext;
            
            public InnerContextStateCache(RunControlContext context) {
                fContext = context;
            }
            
            public void doneGetState(IToken token, Exception error, boolean suspended, String pc, String reason,
                Map<String, Object> params) {
                set(token, new ContextState(suspended, pc, reason, params), error);
            }
            
            @Override
            protected IToken retrieveToken() {
                return fContext.getState(this);
            }
        } 
        
        private final String fId;
        private InnerContextStateCache fInnerCache;
        public ContextStateCache(String id) {
            fId = id;
        }
        
        @Override
        protected void retrieve(DataCallback<ContextState> rm) {
            new Transaction<ContextState>() {
                @Override
                protected ContextState process() throws InvalidCacheException, ExecutionException 
                {
                    RunControlContext context = validate( getContext(fId) );
                    if (fInnerCache == null || !fInnerCache.fContext.equals(context)) {
                        fInnerCache = new InnerContextStateCache(context);
                    }
                    return validate(fInnerCache);
                }
            }.request(rm);
        }
        
        @Override
        protected void handleCompleted(ContextState data, Throwable error, boolean canceled) {
            if (canceled) return;
            fStateResetMap.addValid(fId, this);
            set(data, error, true);
        }
        
        public void setState(ContextState state, Throwable error) {
            fStateResetMap.addValid(fId, this);
            set(state, error, true);
        }
        
        public void reset() {
            super.reset();
            if (fInnerCache != null) {
                fInnerCache.reset();
            }
        }
        
    }
    
    private class ContextStateKey extends IdKey<ContextStateCache> {
        public ContextStateKey(String id) {
            super(ContextStateCache.class, id);
        }
        @Override 
        ContextStateCache createCache() { 
            return new ContextStateCache(getId()); 
        }
    }
    
    public ICache<ContextState> getState(String id) {
        return mapCache(new ContextStateKey(id));
    }

    protected abstract static class ContextCommandKey<V> extends IdKey<V> {
        Object fClientKey;
        
        ContextCommandKey(Class<V> cacheClass, String contextId, Object clientKey) {
            super(cacheClass, contextId);
            fClientKey = clientKey;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (super.equals(obj) && obj instanceof ContextCommandKey<?>) {
                return ((ContextCommandKey<?>)obj).fClientKey.equals(fClientKey);
            } 
            return false;        
        }
        
        @Override
        public int hashCode() {
            return super.hashCode() + fClientKey.hashCode();
        }
    }

    private abstract static class DoneCommandCache extends TokenCache<Object> implements DoneCommand {
        public void doneCommand(IToken token, Exception error) {
            set(token, null, error);
        }
    }
       
    public ICache<Object> suspend(final RunControlContext context, Object clientKey) {
        class MyCache extends DoneCommandCache {
            @Override
            protected IToken retrieveToken() {
                return context.suspend(this);
            }
        }
        return mapCache( new ContextCommandKey<MyCache>(MyCache.class, context.getID(), clientKey) {
                @Override MyCache createCache() { return new MyCache(); }
            });
    }

    public ICache<Object> resume(final RunControlContext context, Object clientKey, final int mode, 
        final int count) 
    {
        class MyCache extends DoneCommandCache {
            @Override
            protected IToken retrieveToken() {
                return context.resume(mode, count, this);
            }
        }
        return mapCache( new ContextCommandKey<MyCache>(MyCache.class, context.getID(), clientKey) {
                @Override MyCache createCache() { return new MyCache(); }
            });        
    }

    public ICache<Object> resume(final RunControlContext context, Object clientKey, final int mode, 
        final int count, final Map<String,Object> params) 
    {
        class MyCache extends DoneCommandCache {
            @Override
            protected IToken retrieveToken() {
                return context.resume(mode, count, params, this);
            }
        }
        return mapCache( new ContextCommandKey<MyCache>(MyCache.class, context.getID(), clientKey) {
                @Override MyCache createCache() { return new MyCache(); }
            });        
    }

    public ICache<Object> terminate(final RunControlContext context, Object clientKey) {
        class MyCache extends DoneCommandCache {
            @Override
            protected IToken retrieveToken() {
                return context.terminate(this);
            }
        }
        return mapCache( new ContextCommandKey<MyCache>(MyCache.class, context.getID(), clientKey) {
                @Override MyCache createCache() { return new MyCache(); }
            });        
        
    }    
    
    private class WaitForContainerResumedCache extends WaitForEventCache<String[]> {}

    public IWaitForEventCache<String[]> waitForContainerResumed(String id, Object clientKey) {
        return mapCache(new IdEventKey<WaitForContainerResumedCache>(WaitForContainerResumedCache.class, id, clientKey) {
                @Override
                WaitForContainerResumedCache createCache() {
                    return new WaitForContainerResumedCache();
                }
            });
    }

    public void containerResumed(String[] context_ids) {
        for (RunControlListener listener : fListeners) {
            listener.containerResumed(context_ids);
        }
        
        for (Map.Entry<Key<?>, Object> entry: fMap.entrySet()) {
            if (entry.getKey() instanceof IdEventKey) {
                IdEventKey<?> eventKey = (IdEventKey<?>)entry.getKey();
                if ( WaitForContainerResumedCache.class.equals(eventKey.getCacheClass()) &&
                     contains(context_ids, eventKey.fId) ) 
                {
                    ((WaitForContainerResumedCache)entry.getValue()).eventReceived(context_ids);
                }
            }
        }
        for (String id : context_ids) {
            doContextResumed(id);
        }
    }

    private class WaitForContainerSuspendedCache extends WaitForEventCache<String[]> {}

    public IWaitForEventCache<String[]> waitForContainerSuspended(String id, Object clientKey) {
        return mapCache(new IdEventKey<WaitForContainerSuspendedCache>(WaitForContainerSuspendedCache.class, id, clientKey) {
                @Override
                WaitForContainerSuspendedCache createCache() {
                    return new WaitForContainerSuspendedCache();
                }
            });
    }

    public void containerSuspended(String context, String pc, String reason, Map<String, Object> params,
        String[] suspended_ids) 
    {
        // Call client listeners first
        for (RunControlListener listener : fListeners) {
            listener.containerSuspended(context, pc, reason, params, suspended_ids);
        }

        for (Map.Entry<Key<?>, Object> entry: fMap.entrySet()) {
            if (entry.getKey() instanceof IdEventKey) {
                IdEventKey<?> eventKey = (IdEventKey<?>)entry.getKey();
                if ( WaitForContainerSuspendedCache.class.equals( eventKey.getCacheClass() ) &&
                     eventKey.fId.equals(context) ) 
                {
                    ((WaitForContainerSuspendedCache)entry.getValue()).eventReceived(suspended_ids);
                }
            }
        }
        
        ContextState state = pc == null ? null : new ContextState(true, pc, reason, params); 
        doContextSuspended(context, state);
        
        for (String id : suspended_ids) {
            if (!id.equals(context)) {
                doContextSuspended(id, null);
            }
        }
    }
    
    private class WaitForContextSuspendedCache extends WaitForEventCache<Object> {}

    public IWaitForEventCache<Object> waitForContextSuspended(String id, Object clientKey) {
        return mapCache(new IdEventKey<WaitForContextSuspendedCache>(WaitForContextSuspendedCache.class, id, clientKey) {
                @Override
                WaitForContextSuspendedCache createCache() {
                    return new WaitForContextSuspendedCache();
                }
            });
    }

    public void contextSuspended(String id, String pc, String reason, Map<String, Object> params) {
        // Call client listeners first
        for (RunControlListener listener : fListeners) {
            listener.contextSuspended(id, pc, reason, params);
        }
        
        ContextState state = pc == null ? null : new ContextState(true, pc, reason, params); 
        doContextSuspended(id, state); 
    }
    
    public void doContextSuspended(String id, ContextState state) {
        fStateResetMap.reset(id);
        ContextStateCache stateCache = getCache(new ContextStateKey(id));    
        if (stateCache != null) {
            if (state != null) {
                stateCache.setState(state, null);
            } 
        }

        // TODO: avoid iterating over all entries, use separate list for events.
        for (Map.Entry<Key<?>, Object> entry: fMap.entrySet()) {
            if (entry.getKey() instanceof IdEventKey) {
                IdEventKey<?> eventKey = (IdEventKey<?>)entry.getKey();
                if ( WaitForContextSuspendedCache.class.equals( eventKey.getCacheClass() ) &&
                     eventKey.fId.equals(id) ) 
                {
                    ((WaitForContextSuspendedCache)entry.getValue()).eventReceived(null);
                }
            }
        }
    }

    private class WaitForContextResumedCache extends WaitForEventCache<Object> {}
    
    public IWaitForEventCache<Object> waitForContextResumed(String id, Object clientKey) {
        return mapCache(new IdEventKey<WaitForContextResumedCache>(WaitForContextResumedCache.class, id, clientKey) {
                @Override
                WaitForContextResumedCache createCache() {
                    return new WaitForContextResumedCache();
                }
            });
    }
    
    private static final ContextState RESUMED_STATE = new ContextState(false, null, null, null);
    
    public void contextResumed(String id) {
        for (RunControlListener listener : fListeners) {
            listener.contextResumed(id);
        }
        doContextResumed(id);
    }
    
    private void doContextResumed(String id) {
        for (RunControlListener listener : fListeners) {
            listener.contextResumed(id);
        }        fStateResetMap.reset(id);
        
        ContextStateCache stateCache = getCache(new ContextStateKey(id));    
        if (stateCache != null) {
            stateCache.setState(RESUMED_STATE, null);
        }        
        
        // TODO: avoid iterating over all entries, use separate list for events.
        for (Map.Entry<Key<?>, Object> entry: fMap.entrySet()) {
            if (entry.getKey() instanceof IdEventKey) {
                IdEventKey<?> eventKey = (IdEventKey<?>)entry.getKey();
                if ( WaitForContextResumedCache.class.equals( eventKey.getCacheClass() ) &&
                     eventKey.fId.equals(id) ) 
                {
                    ((WaitForContextResumedCache)entry.getValue()).eventReceived(null);
                }
            }
        }
    }

    private class WaitForContextExceptionCache extends WaitForEventCache<String> {}

    public IWaitForEventCache<String> waitForContextException(String id, Object clientKey) {
        return mapCache(new IdEventKey<WaitForContextExceptionCache>(WaitForContextExceptionCache.class, id, clientKey) {
                @Override
                WaitForContextExceptionCache createCache() {
                    return new WaitForContextExceptionCache();
                }
            });
    }
        
    public void contextException(String id, String msg) {
        fStateResetMap.reset(id);
        
        // TODO: avoid iterating over all entries, use separate list for events.
        for (Map.Entry<Key<?>, Object> entry: fMap.entrySet()) {
            if (entry.getKey() instanceof IdEventKey) {
                IdEventKey<?> eventKey = (IdEventKey<?>)entry.getKey();
                if ( WaitForContextExceptionCache.class.equals( eventKey.getCacheClass() ) &&
                     eventKey.fId.equals(id) ) 
                {
                    ((WaitForContextExceptionCache)entry.getValue()).eventReceived(msg);
                }
            }
        }
    }

    private abstract class ContextEventKey<V> extends IdKey<V> {
        private Object fClientKey;
        
        public ContextEventKey(Class<V> eventClazz, String id, Object clientKey) {
            super(eventClazz, id);
            fClientKey = clientKey;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (super.equals(obj) && obj instanceof ContextEventKey<?>) {
                return ((ContextEventKey<?>)obj).fClientKey.equals(fClientKey);
            } 
            return false;        
        }
        
        @Override
        public int hashCode() {
            return super.hashCode() + fClientKey.hashCode();
        } 
    }

    private class WaitForContextAddedCache extends WaitForEventCache<RunControlContext[]> {}

    public IWaitForEventCache<RunControlContext[]> waitForContextAdded(String parentId, Object clientKey) {
        return mapCache(new ContextEventKey<WaitForContextAddedCache>(WaitForContextAddedCache.class, parentId, clientKey) {
                @Override
                WaitForContextAddedCache createCache() {
                    return new WaitForContextAddedCache();
                }
            });
    }

    public void contextAdded(RunControlContext[] contexts) {
        for (RunControlListener listener : fListeners) {
            listener.contextAdded(contexts);
        }

        for (RunControlContext context : contexts) {
            fStateResetMap.reset(context.getID());
        }
        
        Set<String> parents = new TreeSet<String>();
        for (RunControlContext context : contexts) {
            if (context.getParentID() != null) {
                parents.add(context.getParentID());
            }
        }
        for (String parent : parents) {
            fChildrenResetMap.reset(parent, false, false);
        }
        
        for (Map.Entry<Key<?>, Object> entry: fMap.entrySet()) {
            if (entry.getKey() instanceof ContextEventKey) {
                ContextEventKey<?> eventKey = (ContextEventKey<?>)entry.getKey();
                if ( WaitForContextAddedCache.class.equals( eventKey.getCacheClass()) &&
                     parents.contains(eventKey.getId()) )
                {
                    ((WaitForContextAddedCache)entry.getValue()).eventReceived(contexts);
                }
            }
        }
    }

    private class WaitForContextChangedCache extends WaitForEventCache<RunControlContext[]> {}

    public IWaitForEventCache<RunControlContext[]> waitForContextChanged(String id, Object clientKey) {
        return mapCache(new ContextEventKey<WaitForContextChangedCache>(WaitForContextChangedCache.class, id, clientKey) {
                @Override
                WaitForContextChangedCache createCache() {
                    return new WaitForContextChangedCache();
                }
            });
    }

    public void contextChanged(RunControlContext[] contexts) {
        for (RunControlListener listener : fListeners) {
            listener.contextChanged(contexts);
        }

        for (RunControlContext context : contexts) {
            fStateResetMap.reset(context.getID());
            fChildrenResetMap.reset(context.getID(), true, false);
        }
        
        for (Map.Entry<Key<?>, Object> entry: fMap.entrySet()) {
            if (entry.getKey() instanceof ContextEventKey) {
                ContextEventKey<?> eventKey = (ContextEventKey<?>)entry.getKey();
                if ( WaitForContextChangedCache.class.equals( eventKey.getCacheClass()) && 
                     contains(contexts, eventKey.getId()) ) 
                {
                    ((WaitForContextChangedCache)entry.getValue()).eventReceived(null);
                }
            }
        }
    }

    private class WaitForContextRemovedCache extends WaitForEventCache<String[]> {}

    public IWaitForEventCache<String[]> waitForContextRemoved(String id, Object clientKey) {
        return mapCache(new IdEventKey<WaitForContextRemovedCache>(WaitForContextRemovedCache.class, id, clientKey) {
                @Override
                WaitForContextRemovedCache createCache() {
                    return new WaitForContextRemovedCache();
                }
            });
    }

    public void contextRemoved(String[] context_ids) {
        // Call client listeners first
        for (RunControlListener listener : fListeners) {
            listener.contextRemoved(context_ids);
        }

        for (String context_id : context_ids) {
            fChildrenResetMap.reset(context_id, false, true);
            fStateResetMap.reset(context_id);
        }
        
        for (Map.Entry<Key<?>, Object> entry: fMap.entrySet()) {
            if (entry.getKey() instanceof IdEventKey) {
                IdEventKey<?> eventKey = (IdEventKey<?>)entry.getKey();
                if ( WaitForContextRemovedCache.class.equals( eventKey.getCacheClass()) && 
                     contains(context_ids, eventKey.getId()) ) 
                {
                    ((WaitForContextRemovedCache)entry.getValue()).eventReceived(context_ids);
                }
            }
        }
    }

}
