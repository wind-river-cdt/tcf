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
import org.eclipse.tcf.services.IBreakpoints;

/**
 * 
 */
public class BreakpointsCM extends AbstractCacheManager implements IBreakpoints.BreakpointsListener {

    private IBreakpoints fService;
    
    public BreakpointsCM(IBreakpoints service) {
        fService = service;
        fService.addListener(this);
    }

    @Override
    public void dispose() {
        fService.removeListener(this);
        // TODO Auto-generated method stub
        super.dispose();
    }

    private abstract static class DoneCommandCache extends TokenCache<Object> implements IBreakpoints.DoneCommand {
        public void doneCommand(IToken token, Exception error) {
            set(token, null, error);
        }
    }

    public ICache<Object> set(final Map<String,Object>[] properties, Object clientKey) {
        class MyCache extends DoneCommandCache {
            @Override
            protected IToken retrieveToken() {
                return fService.set(properties, this);
            }
        }
        return mapCache( new CommandKey<MyCache>(MyCache.class, clientKey) {
                @Override MyCache createCache() { return new MyCache(); }
            });                
    }

    public ICache<Object> add(final Map<String,Object> properties, Object clientKey) {
        class MyCache extends DoneCommandCache {
            @Override
            protected IToken retrieveToken() {
                return fService.add(properties, this);
            }
        }
        return mapCache( new CommandKey<MyCache>(MyCache.class, clientKey) {
                @Override MyCache createCache() { return new MyCache(); }
            });                
    }

    public ICache<Object> change(final Map<String,Object> properties, Object clientKey) {
        class MyCache extends DoneCommandCache {
            @Override
            protected IToken retrieveToken() {
                return fService.change(properties, this);
            }
        }
        return mapCache( new CommandKey<MyCache>(MyCache.class, clientKey) {
                @Override MyCache createCache() { return new MyCache(); }
            });                
    }
    
    public ICache<Object> enable(final String[] ids, Object clientKey) {
        class MyCache extends DoneCommandCache {
            @Override
            protected IToken retrieveToken() {
                return fService.enable(ids, this);
            }
        }
        return mapCache( new CommandKey<MyCache>(MyCache.class, clientKey) {
                @Override MyCache createCache() { return new MyCache(); }
            });                
    }

    public ICache<Object> disable(final String[] ids, Object clientKey) {
        class MyCache extends DoneCommandCache {
            @Override
            protected IToken retrieveToken() {
                return fService.disable(ids, this);
            }
        }
        return mapCache( new CommandKey<MyCache>(MyCache.class, clientKey) {
                @Override MyCache createCache() { return new MyCache(); }
            });                
    }

    public ICache<Object> remove(final String[] ids, Object clientKey) {
        class MyCache extends DoneCommandCache {
            @Override
            protected IToken retrieveToken() {
                return fService.remove(ids, this);
            }
        }
        return mapCache( new CommandKey<MyCache>(MyCache.class, clientKey) {
                @Override MyCache createCache() { return new MyCache(); }
            });                
    }
    
    private class IDsCache extends TokenCache<String[]> implements IBreakpoints.DoneGetIDs {            
        @Override
        protected IToken retrieveToken() {
            return fService.getIDs(this);
        } 
        
        public void doneGetIDs(IToken token, Exception error, String[] ids) {
            set(token, ids, error);
        }
        
        public void resetIDs() {
            // TODO: handle add/remove ids
            if (isValid()) reset();
        }
    }
    
    private class IDsCacheKey extends Key<IDsCache> {
        public IDsCacheKey() { 
            super(IDsCache.class);
        }
        
        @Override IDsCache createCache() { return new IDsCache(); }
    }
    
    public ICache<String[]> getIDs() {
        return mapCache( new IDsCacheKey() );
    }

    private class PropertiesCache extends TokenCache<Map<String,Object>> implements IBreakpoints.DoneGetProperties {
        String fId;
        
        public PropertiesCache(String id) {
            fId = id;
        }
        
        @Override
        protected IToken retrieveToken() {
            return fService.getProperties(fId, this);
        }
        
        public void doneGetProperties(IToken token, Exception error, Map<String, Object> properties) {
            set(token, properties, error);
        }
        
        public void setProperties(Map<String, Object> properties) {
            set(null, properties, null);
        }
        
        public void resetProperties() {
            if (isValid()) reset();
        }
    }
    
    private class PropertiesCacheKey extends IdKey<PropertiesCache> {
        public PropertiesCacheKey(String id) {
            super(PropertiesCache.class, id);
        }
        
        @Override PropertiesCache createCache() { return new PropertiesCache(fId); }
    };     

    public ICache<Map<String,Object>> getProperties(String id) {
        return mapCache( new PropertiesCacheKey(id) );
    }
    
    private class StatusCache extends TokenCache<Map<String,Object>> implements IBreakpoints.DoneGetStatus {
        String fId;
        
        public StatusCache(String id) {
            fId = id;
        }
        
        @Override
        protected IToken retrieveToken() {
            return fService.getStatus(fId, this);
        }
        
        public void doneGetStatus(IToken token, Exception error, Map<String, Object> status) {
            set(token, status, error);
        }
        
        public void setStatus(Map<String, Object> status) {
            set(null, status, null);
        }
    }
    
    private class StatusCacheKey extends IdKey<StatusCache> {
        public StatusCacheKey(String id) {
            super(StatusCache.class, id);
        }
        @Override StatusCache createCache() { return new StatusCache(fId); }
    }  
    
    public ICache<Map<String,Object>> getStatus(String id) {
        return mapCache( new StatusCacheKey(id) );      
    }

    public ICache<Map<String,Object>> getCapabilities(final String id) {
        class MyCache extends TokenCache<Map<String,Object>> implements IBreakpoints.DoneGetCapabilities {            
            @Override
            protected IToken retrieveToken() {
                return fService.getCapabilities(id, this);
            }
            public void doneGetCapabilities(IToken token, Exception error, Map<String, Object> capabilities) {
                set(token, capabilities, error);
            }            
        }
        return mapCache( new IdKey<MyCache>(MyCache.class, id) {
                @Override MyCache createCache() { return new MyCache(); }
            });                        
    }
    
    private class StatusChangedCache extends WaitForEventCache<Map<String, Object>> {}

    public ICache<Map<String, Object>> waitStatusChanged(String id, Object clientKey) {
        return mapCache(new IdEventKey<StatusChangedCache>(StatusChangedCache.class, id, clientKey) {
                @Override
                StatusChangedCache createCache() {
                    return new StatusChangedCache();
                }
            });
    }
        
    public void breakpointStatusChanged(String id, final Map<String, Object> status) {
        StatusCache statusCache = getCache(new StatusCacheKey(id));    
        if (statusCache != null) {
            statusCache.setStatus(status);
        }        
        
        // TODO: avoid iterating over all entries, use separate list for events.
        for (Map.Entry<Key<?>, Object> entry: fMap.entrySet()) {
            if (entry.getKey() instanceof IdEventKey) {
                IdEventKey<?> eventKey = (IdEventKey<?>)entry.getKey();
                if ( StatusChangedCache.class.equals( eventKey.getCacheClass() ) &&
                     eventKey.fId.equals(id) ) 
                {
                    ((StatusChangedCache)entry.getValue()).eventReceived(status);
                }
            }
        }
    }

    private void setBreakpointsProperties(Map<String, Object>[] bps) {
        for (Map<String, Object> bp : bps) {
            Object id = (String)bp.get(IBreakpoints.PROP_ID);
            if (id instanceof String) {
                PropertiesCache cache = mapCache(new  PropertiesCacheKey((String)id));
                cache.setProperties(bp);
            }
        }
    }
    
    private class ContextAddedCache extends WaitForEventCache<Map<String, Object>[]> {}

    public ICache<Map<String, Object>[]> waitContextAdded(Object clientKey) {
        return mapCache(new EventKey<ContextAddedCache>(ContextAddedCache.class, clientKey) {
                @Override
                ContextAddedCache createCache() {
                    return new ContextAddedCache();
                }
            });
    }

    public void contextAdded(Map<String, Object>[] bps) {
        IDsCache idsCache = getCache(new IDsCacheKey());    
        if (idsCache != null && idsCache.isValid()) {
            idsCache.resetIDs();
        }        
        
        setBreakpointsProperties(bps);
        
        // TODO: avoid iterating over all entries, use separate list for events.
        for (Map.Entry<Key<?>, Object> entry: fMap.entrySet()) {
            if (entry.getKey() instanceof EventKey) {
                EventKey<?> eventKey = (EventKey<?>)entry.getKey();
                if ( ContextAddedCache.class.equals( eventKey.getCacheClass() ) ) {
                    ((ContextAddedCache)entry.getValue()).eventReceived(bps);
                }
            }
        }
    }

    private class ContextChangedCache extends WaitForEventCache<Map<String, Object>[]> {}

    public ICache<Map<String, Object>[]> waitContextChanged(Object clientKey) {
        return mapCache(new EventKey<ContextChangedCache>(ContextChangedCache.class, clientKey) {
                @Override
                ContextChangedCache createCache() {
                    return new ContextChangedCache();
                }
            });
    }

    public void contextChanged(Map<String, Object>[] bps) {
        setBreakpointsProperties(bps);
        
        // TODO: avoid iterating over all entries, use separate list for events.
        for (Map.Entry<Key<?>, Object> entry: fMap.entrySet()) {
            if (entry.getKey() instanceof EventKey) {
                EventKey<?> eventKey = (EventKey<?>)entry.getKey();
                if ( ContextChangedCache.class.equals( eventKey.getCacheClass() ) ) {
                    ((ContextChangedCache)entry.getValue()).eventReceived(bps);
                }
            }
        }
    }
    
    private class ContextRemovedCache extends WaitForEventCache<String[]> {}

    public ICache<String[]> waitContextRemoved(Object clientKey) {
        return mapCache(new EventKey<ContextRemovedCache>(ContextRemovedCache.class, clientKey) {
                @Override
                ContextRemovedCache createCache() {
                    return new ContextRemovedCache();
                }
            });
    }

    public void contextRemoved(String[] ids) {
        IDsCache idsCache = getCache(new IDsCacheKey());    
        if (idsCache != null) {
            idsCache.resetIDs();
        }
        
        for (String id : ids) {
            PropertiesCache cache = mapCache(new  PropertiesCacheKey(id));
            cache.resetProperties();
        }        

        // TODO: avoid iterating over all entries, use separate list for events.
        for (Map.Entry<Key<?>, Object> entry: fMap.entrySet()) {
            if (entry.getKey() instanceof EventKey) {
                EventKey<?> eventKey = (EventKey<?>)entry.getKey();
                if ( ContextRemovedCache.class.equals( eventKey.getCacheClass() ) ) {
                    ((ContextRemovedCache)entry.getValue()).eventReceived(ids);
                }
            }
        }
    }
    
    
}
