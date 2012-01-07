package org.eclipse.tcf.debug.test.services;


abstract class IdEventKey<V> extends IdKey<V> {
    private Object fClientKey;
    
    public IdEventKey(Class<V> eventClazz, String id, Object clientKey) {
        super(eventClazz, id);
        fClientKey = clientKey;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj) && obj instanceof IdEventKey<?>) {
            return ((IdEventKey<?>)obj).fClientKey.equals(fClientKey);
        } 
        return false;        
    }
    
    @Override
    public int hashCode() {
        return super.hashCode() + fClientKey.hashCode();
    } 
}