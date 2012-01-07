package org.eclipse.tcf.debug.test.services;


abstract class IdKey<V> extends Key<V> {
    String fId;
    
    public IdKey(Class<V> clazz, String id) {
        super(clazz);
        fId = id;
    }
    
    public String getId() {
        return fId;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj) && obj instanceof IdKey<?>) {
            return ((IdKey<?>)obj).fId.equals(fId);
        } 
        return false;        
    }
    
    @Override
    public int hashCode() {
        return super.hashCode() + fId.hashCode();
    }
}