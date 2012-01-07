package org.eclipse.tcf.debug.test.services;


abstract class Key<V> {
    private Class<V> fCacheClass;
    
    public Key(Class<V> cacheClass) {
        fCacheClass = cacheClass;
    }
    
    abstract V createCache();

    public Class<V> getCacheClass() {
        return fCacheClass;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Key) {
            return ((Key<?>)obj).fCacheClass.equals(fCacheClass);
        } 
        return false;        
    }
    
    @Override
    public int hashCode() {
        return fCacheClass.hashCode();
    }
}