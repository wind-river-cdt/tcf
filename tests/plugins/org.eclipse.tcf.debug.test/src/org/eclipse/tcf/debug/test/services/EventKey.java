package org.eclipse.tcf.debug.test.services;


abstract class EventKey<V> extends Key<V> {
    private Object fClientKey;
    
    public EventKey(Class<V> eventClazz, Object clientKey) {
        super(eventClazz);
        fClientKey = clientKey;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj) && obj instanceof EventKey<?>) {
            return ((EventKey<?>)obj).fClientKey.equals(fClientKey);
        } 
        return false;        
    }
    
    @Override
    public int hashCode() {
        return super.hashCode() + fClientKey.hashCode();
    } 
}