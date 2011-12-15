/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.debug.internal.ui.actions.RunContextualLaunchAction;
import org.eclipse.tcf.debug.test.util.DataCallback;
import org.eclipse.tcf.protocol.IErrorReport;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IRunControl;
import org.eclipse.tcf.services.IRunControl.RunControlContext;

/**
 * Run control service listener.  Allows client to wait for events with a 
 * callback.
 */
public class TestRunControlListener implements IRunControl.RunControlListener {

    private IRunControl fRunControl;
    private Throwable fError;
    
    private final HashMap<String, IRunControl.RunControlContext> ctx_map = new HashMap<String,IRunControl.RunControlContext>();
    private final HashMap<String, String> fSuspendedPCs = new HashMap<String, String>();
    private Map<String, List<DataCallback<String>>> waiting_suspend = new HashMap<String, List<DataCallback<String>>>();
    private String process_id;
    private boolean test_done;
    private String test_ctx_id;

    
    public TestRunControlListener(IRunControl rc) {
        fRunControl = rc;
        fRunControl.addListener(this);
    }
    
    public void dispose() throws Exception {
        test_done = true;
        fRunControl.removeListener(this);
    }

    public void checkError() throws Exception {
        if (fError != null) {
            throw new Exception(fError);
        }
    }
    
    public void waitForSuspend(RunControlContext context, DataCallback<String> cb) {
        final String contextId = context.getID();
        if (fSuspendedPCs.containsKey(contextId)) {
            String pc = fSuspendedPCs.get(contextId);
            if (pc != null) {
                cb.setData(pc);
                cb.done();
                return;
            } else {
                addWaitingForSuspend(contextId, cb);
            }
        } else {
            getContextState(context, cb);
        }
    }
    
    private void getContextState(RunControlContext context, final DataCallback<String> cb) {
        final String contextId = context.getID();
        context.getState(new IRunControl.DoneGetState() {
            public void doneGetState(IToken token, Exception error,
                    boolean suspended, String pc, String reason,
                    Map<String,Object> params) {
                if (error != null) {
                    exit(error);
                }
                else if (suspended) {
                    fSuspendedPCs.put(contextId, pc);
                    cb.setData(pc);
                    cb.done();
                }
                else {
                    fSuspendedPCs.put(contextId, null);
                    if (cb != null) {
                        addWaitingForSuspend(contextId, cb);
                    }
                }
            }
        });
    }

    public void addWaitingForSuspend(String contextId, DataCallback<String> cb) {
        List<DataCallback<String>> waitingList = waiting_suspend.get(contextId);
        if (waitingList == null) {
            waitingList = new ArrayList<DataCallback<String>>(1);
            waiting_suspend.put(contextId, waitingList);
        }
        waitingList.add(cb);
    }
    
    private void exit(Throwable e) {
        fError = e;
    }
    
    public void contextAdded(RunControlContext[] contexts) {
        for (IRunControl.RunControlContext ctx : contexts) {
            if (ctx_map.get(ctx.getID()) != null) exit(new Error("Invalid 'contextAdded' event"));
            ctx_map.put(ctx.getID(), ctx);
        }
    }

    public void contextChanged(RunControlContext[] contexts) {
        for (IRunControl.RunControlContext ctx : contexts) {
            if (ctx_map.get(ctx.getID()) == null) return;
            ctx_map.put(ctx.getID(), ctx);
        }
    }
    
    public void waitSuspended(DataCallback<String> cb) {
        
    }

    public void contextRemoved(String[] context_ids) {
        for (String id : context_ids) {
            ctx_map.remove(id);
            if (id.equals(test_ctx_id)) {
                if (test_done) {
//                    bp.set(null, new IBreakpoints.DoneCommand() {
//                        public void doneCommand(IToken token, Exception error) {
//                            exit(error);
//                        }
//                    });
                }
                else {
                    exit(new Exception("Test process exited too soon"));
                }
                return;
            }
        }
    }

    public void contextSuspended(String contextId, String pc, String reason, Map<String, Object> params) {
        if (pc == null || "".equals(pc)) {
            RunControlContext context = ctx_map.get(contextId);
            if (context != null) {
                getContextState(context, null);
            }
            return;
        }
        
        fSuspendedPCs.put(contextId, pc);
        List<DataCallback<String>> waitingList = waiting_suspend.remove(contextId);
        if (waitingList != null) {
            for (DataCallback<String> cb : waitingList) {
                cb.setData(pc);
                cb.done();
            }
        }
        if (test_done) {
            IRunControl.RunControlContext ctx = ctx_map.get(contextId);
            if (ctx != null && process_id != null && process_id.equals(ctx.getParentID())) {
                ctx.resume(IRunControl.RM_RESUME, 1, new IRunControl.DoneCommand() {
                    public void doneCommand(IToken token, Exception error) {
                        if (error instanceof IErrorReport) {
                            int code = ((IErrorReport)error).getErrorCode();
                            if (code == IErrorReport.TCF_ERROR_ALREADY_RUNNING) return;
                            if (code == IErrorReport.TCF_ERROR_INV_CONTEXT) return;
                        }
                        if (error != null) exit(error);
                    }
                });
            }
        }
    }

    public void contextResumed(String context) {
        fSuspendedPCs.put(context, null);
    }

    public void containerSuspended(String context, String pc, String reason, Map<String, Object> params,
        String[] suspended_ids) 
    {
        for (String id : suspended_ids) {
            assert id != null;
            contextSuspended(id, id.equals(context) ? pc : null, null, null);
        }
    }

    public void containerResumed(String[] resumed_ids) {
        for (String id : resumed_ids) {
            assert id != null;
            contextResumed(id);
        }
    }

    public void contextException(String context, String msg) {
        exit(new Exception("Context exception: " + msg));
    }

    
}
