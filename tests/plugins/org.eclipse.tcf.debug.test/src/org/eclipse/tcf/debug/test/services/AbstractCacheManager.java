package org.eclipse.tcf.debug.test.services;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.tcf.debug.test.util.ICache;

public class AbstractCacheManager {

    protected Map<Key<?>, ICache<?>> fMap = new LinkedHashMap<Key<?>, ICache<?>>();

    public AbstractCacheManager() {
        super();
    }

    public void dispose() {
    }

    protected <V> V getCache(Key<V> key) {
        @SuppressWarnings("unchecked")
        V cache = (V)fMap.get(key);
        return cache;
    }
    
    protected <V> V mapCache(Key<V> key) {
        @SuppressWarnings("unchecked")
        V cache = (V)fMap.get(key);
        if (cache != null) return cache;
        cache = key.createCache();
        fMap.put(key, (ICache<?>)cache);
        return cache;
    }

    protected boolean contains(Object[] elements, Object toFind) {
        for (int i = 0; i < elements.length; i++) {
            if (toFind.equals(elements[i])) {
                return true;
            }
        }
        return false;
    }

}