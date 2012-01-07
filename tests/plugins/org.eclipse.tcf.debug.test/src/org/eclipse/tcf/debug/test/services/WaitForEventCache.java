package org.eclipse.tcf.debug.test.services;

import org.eclipse.tcf.debug.test.util.AbstractCache;

class WaitForEventCache<V> extends AbstractCache<V> implements IWaitForEventCache<V> {
    @Override
    protected void retrieve() { } // no-op - called by listener
    @Override
    protected void canceled() { } // no-op - no command sent
    
    public void eventReceived(V data) {
        if (!isValid()) {
            set(data, null, true); // notify listeners
        }
    }
}