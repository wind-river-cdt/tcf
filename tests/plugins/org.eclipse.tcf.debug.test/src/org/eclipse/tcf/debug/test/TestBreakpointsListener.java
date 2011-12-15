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
package org.eclipse.tcf.debug.test;

import java.util.Collection;
import java.util.Map;

import org.eclipse.tcf.services.IBreakpoints;

/**
 * Listener for breakpoint service events.
 */
public class TestBreakpointsListener implements IBreakpoints.BreakpointsListener{

    final private IBreakpoints fBreakpoints;
    private Throwable fError;
    private String bp_id;
    private String process_id;
    private boolean test_done = false;
    
    public TestBreakpointsListener(IBreakpoints bp) {
        fBreakpoints = bp;
        fBreakpoints.addListener(this);
    }    
    
    public void dispose() {
        test_done = true;
        fBreakpoints.removeListener(this);
    }

    public void setBreakpointId(String bpId) {
        bp_id = bpId;
    }
    
    public void setProcess(String processId) {
        process_id = processId;
    }
    
    public void checkError() throws Exception {
        if (fError != null) {
            throw new Exception(fError);
        }
    }
    
    public void exit(Throwable error) {
        fError = error;
    }
    
    public void breakpointStatusChanged(String id, Map<String, Object> status) {
        if (id.equals(bp_id) && process_id != null && !test_done) {
            String s = (String)status.get(IBreakpoints.STATUS_ERROR);
            if (s != null) exit(new Exception("Invalid BP status: " + s));
            Collection<Map<String,Object>> list = (Collection<Map<String,Object>>)status.get(IBreakpoints.STATUS_INSTANCES);
            if (list == null) return;
            String err = null;
            for (Map<String,Object> map : list) {
                String ctx = (String)map.get(IBreakpoints.INSTANCE_CONTEXT);
                if (process_id.equals(ctx) && map.get(IBreakpoints.INSTANCE_ERROR) != null)
                    err = (String)map.get(IBreakpoints.INSTANCE_ERROR);
            }
            if (err != null) exit(new Exception("Invalid BP status: " + err));
        }
    }

    public void contextAdded(Map<String, Object>[] bps) {
    }

    public void contextChanged(Map<String, Object>[] bps) {
    }

    public void contextRemoved(String[] ids) {
    }
    
    
}
