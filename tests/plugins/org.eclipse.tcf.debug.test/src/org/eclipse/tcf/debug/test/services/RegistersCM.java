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

import java.util.Map;

import org.eclipse.tcf.debug.test.util.ICache;
import org.eclipse.tcf.debug.test.util.TokenCache;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IRegisters;
import org.eclipse.tcf.services.IRegisters.RegistersContext;
import org.eclipse.tcf.services.IRunControl;
import org.eclipse.tcf.services.IRunControl.RunControlContext;

/**
 * 
 */
public class RegistersCM extends AbstractCacheManager implements IRunControl.RunControlListener, IRegisters.RegistersListener  {
    private IRegisters fService;
    private IRunControl fRunControl;
    private final ResetMap fRunControlStateResetMap = new ResetMap();
    private final ResetMap fRegistersMap = new ResetMap();
    
    public RegistersCM(IRegisters service, IRunControl runControl) {
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
        class MyCache extends TokenCache<String[]> implements IRegisters.DoneGetChildren {
            @Override
            protected IToken retrieveToken() {
                return fService.getChildren(id, this);
            }
            
            public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
                fRegistersMap.addValid(id, this);
                set(token, context_ids, error);
            }
        };

        return mapCache(new IdKey<MyCache>(MyCache.class, id) {
            @Override MyCache createCache() { return new MyCache(); }
        });
    }

    public ICache<RegistersContext> getContext(final String id) {
        class MyCache extends TokenCache<RegistersContext> implements IRegisters.DoneGetContext {
            @Override
            protected IToken retrieveToken() {
                return fService.getContext(id, this);
            }
            
            public void doneGetContext(IToken token, Exception error, RegistersContext context) {
                fRegistersMap.addValid(id, this);
                set(token, context, error);
            }
        };

        return mapCache(new IdKey<MyCache>(MyCache.class, id) {
            @Override MyCache createCache() { return new MyCache(); }
        });        
    }
    
    public ICache<byte[]> getContextValue(final RegistersContext context) {
        class MyCache extends TokenCache<byte[]> implements IRegisters.DoneGet {
            @Override
            protected IToken retrieveToken() {
                return context.get(this);
            }
            
            public void doneGet(IToken token, Exception error, byte[] value) {
                fRegistersMap.addValid(context.getID(), this);
                set(token, value, error);
            }
        };

        return mapCache(new IdKey<MyCache>(MyCache.class, context.getID()) {
            @Override MyCache createCache() { return new MyCache(); }
        });                
    }
    
    protected abstract static class ContextCommandKey<V> extends IdKey<V> {
        Object fClientKey;
        
        ContextCommandKey(Class<V> cacheClass, String id, Object clientKey) {
            super(cacheClass, id);
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

    
    public ICache<Object> setContextValue(final RegistersContext context, Object clientKey, final byte[] value) {
        class MyCache extends TokenCache<Object> implements IRegisters.DoneSet {
            @Override
            protected IToken retrieveToken() {
                return context.set(value, this);
            }
            
            public void doneSet(IToken token, Exception error) {
                fRegistersMap.addValid(context.getID(), this);
                set(token, null, error);
            }
        };

        return mapCache(new ContextCommandKey<MyCache>(MyCache.class, context.getID(), clientKey) {
            @Override MyCache createCache() { return new MyCache(); }
        });        
    }
    
    public void contextChanged() {
        fRegistersMap.resetAll();     
    }
    
    public void registerChanged(String id) {
        fRegistersMap.reset(id);
    }

    public void contextAdded(RunControlContext[] contexts) {
    }

    public void contextRemoved(String[] context_ids) {
        for (String id : context_ids) {
            fRunControlStateResetMap.reset(id);
        }
    }

    public void contextChanged(RunControlContext[] contexts) {
        for (RunControlContext context : contexts) {
            fRunControlStateResetMap.reset(context.getID());
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
