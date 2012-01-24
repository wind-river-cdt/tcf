/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.test;

import java.util.Collections;
import java.util.Map;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * 
 */
public class BreakpointsListener implements IBreakpointsListener {

    public enum EventType { ADDED, REMOVED, CHANGED };
    
    public interface EventTester {
        boolean checkEvent(EventType type, IBreakpoint bp, Map<String, Object> deltaAttributes); 
    }

    private static final EventTester ANY_EVENT_TESTER = new EventTester() {
        public boolean checkEvent(EventType type, IBreakpoint bp, Map<String,Object> deltaAttributes) {
            return true;
        }
    };

    private EventTester fTester = ANY_EVENT_TESTER;
    private boolean fWaiting = false;

    
    public BreakpointsListener() {
        DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
    }
    
    public void dispose() {
        DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
    }
    
    public void startWaiting() {
        fWaiting = true;
    }
    public void setTester(EventTester tester) { 
        fTester = tester;
        startWaiting();
    }
    
    public void breakpointsAdded(IBreakpoint[] bps) {
        Map<String,Object> emptyAttrs = Collections.emptyMap();
        for (IBreakpoint bp : bps) {
            if (fTester.checkEvent(EventType.ADDED, bp, emptyAttrs)) {
                synchronized(this) {
                    fWaiting = false;
                    notifyAll();
                }
                return;
            }
        }
    }

    public void breakpointsRemoved(IBreakpoint[] bps, IMarkerDelta[] deltas) {
        for (int i =0; i < bps.length; i++) {
        	Map<String,Object> attributes = Collections.emptyMap();
        	if (deltas[i] != null) {
        		attributes = deltas[i].getAttributes();
        	}        	
            if (fTester.checkEvent(EventType.REMOVED, bps[i], attributes)) {
                synchronized(this) {
                    fWaiting = false;
                    notifyAll();
                }
                return;
            }
        }
    }

    public void breakpointsChanged(IBreakpoint[] bps, IMarkerDelta[] deltas) {
        for (int i =0; i < bps.length; i++) {
        	Map<String,Object> attributes = Collections.emptyMap();
        	if (deltas[i] != null) {
        		attributes = deltas[i].getAttributes();
        	}        	        	
            if (fTester.checkEvent(EventType.CHANGED, bps[i], attributes)) {
                synchronized(this) {
                    fWaiting = false;
                    notifyAll();
                }
                return;
            }
        }
    }

    
    public void waitForEvent() throws InterruptedException {
        synchronized(this) {
            while(fWaiting) {
                wait();
            }
        }
    }
}
