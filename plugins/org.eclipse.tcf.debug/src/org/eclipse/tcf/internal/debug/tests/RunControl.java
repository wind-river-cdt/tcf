/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.debug.tests;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.IDiagnostics;
import org.eclipse.tcf.services.IRunControl;
import org.eclipse.tcf.services.IRunControl.RunControlContext;

class RunControl {

    private final TCFTestSuite test_suite;
    private final IChannel channel;
    private int channel_id;
    private final IRunControl rc_service;
    private final HashSet<String> suspended_ctx_ids = new HashSet<String>();
    private final HashSet<IToken> get_state_cmds = new HashSet<IToken>();
    private final HashMap<String,IToken> resume_cmds = new HashMap<String,IToken>();
    private final HashSet<String> pending_resume_ids = new HashSet<String>();
    private final HashMap<String,IRunControl.RunControlContext> ctx_map = new HashMap<String,IRunControl.RunControlContext>();
    private final Random rnd = new Random();

    private boolean enable_trace;

    private boolean sync_pending;

    private final IRunControl.RunControlListener listener = new IRunControl.RunControlListener() {

        public void contextAdded(RunControlContext[] contexts) {
            for (IRunControl.RunControlContext ctx : contexts) {
                ctx_map.put(ctx.getID(), ctx);
            }
        }

        public void contextChanged(RunControlContext[] contexts) {
            for (IRunControl.RunControlContext ctx : contexts) {
                ctx_map.put(ctx.getID(), ctx);
            }
        }

        public void contextRemoved(String[] context_ids) {
            for (String id : context_ids) {
                ctx_map.remove(id);
                test_suite.getCanceledTests().remove(id);
                suspended_ctx_ids.remove(id);
            }
        }

        public void contextSuspended(final String id, String pc, String reason, Map<String,Object> params) {
            if (enable_trace) System.out.println("" + channel_id + " suspended " + id);
            suspended_ctx_ids.add(id);
            Protocol.invokeLater(new Runnable() {
                public void run() {
                    resume(id, IRunControl.RM_RESUME);
                }
            });
        }

        public void contextResumed(String id) {
            if (enable_trace) System.out.println("" + channel_id + " resumed " + id);
            suspended_ctx_ids.remove(id);
            pending_resume_ids.remove(id);
        }

        public void containerSuspended(String context, String pc, String reason, Map<String, Object> params, String[] suspended_ids) {
            if (enable_trace) {
                StringBuffer bf = new StringBuffer();
                for (String id : suspended_ids) {
                    if (bf.length() > 0) bf.append(',');
                    bf.append(id);
                }
                System.out.println("" + channel_id + " suspended " + bf);
            }
            for (String id : suspended_ids) {
                suspended_ctx_ids.add(id);
                resume(id, IRunControl.RM_RESUME);
            }
        }

        public void containerResumed(String[] context_ids) {
            if (enable_trace) {
                StringBuffer bf = new StringBuffer();
                for (String id : context_ids) {
                    if (bf.length() > 0) bf.append(',');
                    bf.append(id);
                }
                System.out.println("" + channel_id + " resumed " + bf);
            }
            for (String id : context_ids) {
                suspended_ctx_ids.remove(id);
                pending_resume_ids.remove(id);
            }
        }

        public void contextException(String context, String msg) {
        }
    };

    RunControl(TCFTestSuite test_suite, IChannel channel, int channel_id) {
        this.test_suite = test_suite;
        this.channel = channel;
        this.channel_id = channel_id;
        rc_service = channel.getRemoteService(IRunControl.class);
        if (rc_service != null) {
            rc_service.addListener(listener);
            getState();
            startTimer();
        }
        enable_trace = System.getProperty("org.eclipse.tcf.debug.tracing.tests.runcontrol") != null;
    }

    private void getState() {
        get_state_cmds.add(rc_service.getChildren(null, new IRunControl.DoneGetChildren() {
            public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
                get_state_cmds.remove(token);
                if (error != null) {
                    exit(error);
                }
                else {
                    for (final String id : context_ids) {
                        get_state_cmds.add(rc_service.getChildren(id, this));
                        get_state_cmds.add(rc_service.getContext(id, new IRunControl.DoneGetContext() {
                            public void doneGetContext(IToken token, Exception error, RunControlContext context) {
                                get_state_cmds.remove(token);
                                if (error != null) {
                                    exit(error);
                                }
                                else {
                                    ctx_map.put(id, context);
                                    if (context.hasState()) {
                                        get_state_cmds.add(context.getState(new IRunControl.DoneGetState() {
                                            public void doneGetState(IToken token, Exception error, boolean suspended,
                                                    String pc, String reason, Map<String, Object> params) {
                                                get_state_cmds.remove(token);
                                                if (error != null) {
                                                    if (ctx_map.get(id) != null) exit(new Exception(
                                                            "Cannot get context state", error));
                                                }
                                                else {
                                                    if (suspended) suspended_ctx_ids.add(id);
                                                    getStateDone();
                                                }
                                            }
                                        }));
                                    }
                                    getStateDone();
                                }
                            }
                        }));
                    }
                    getStateDone();
                }
            }
        }));
    }

    private void getStateDone() {
        if (get_state_cmds.size() > 0) return;
        if (channel.getState() != IChannel.STATE_OPEN) return;
        if (suspended_ctx_ids.size() > 0) {
            String[] arr = suspended_ctx_ids.toArray(new String[suspended_ctx_ids.size()]);
            resume(arr[rnd.nextInt(arr.length)], IRunControl.RM_RESUME);
        }
    }

    private void startTimer() {
        Protocol.invokeLater(50, new Runnable() {
            public void run() {
                if (channel.getState() != IChannel.STATE_OPEN) return;
                if (test_suite.cancel) return;
                Protocol.invokeLater(50, this);
                Set<String> s = test_suite.getCanceledTests().keySet();
                if (s.size() > 0 || suspended_ctx_ids.size() > 0) {
                    Set<String> ids = new HashSet<String>(s);
                    ids.addAll(suspended_ctx_ids);
                    String[] arr = ids.toArray(new String[ids.size()]);
                    resume(arr[rnd.nextInt(arr.length)], IRunControl.RM_RESUME);
                }
            }
        });
    }

    private void exit(Throwable error) {
        Collection<ITCFTest> c = test_suite.getActiveTests();
        ITCFTest[] arr = c.toArray(new ITCFTest[c.size()]);
        for (ITCFTest t : arr) test_suite.done(t, error);
    }

    IRunControl.RunControlContext getContext(String id) {
        return ctx_map.get(id);
    }

    boolean canResume(String id) {
        if (sync_pending) return false;
        if (get_state_cmds.size() > 0) return false;
        if (resume_cmds.get(id) != null) return false;
        if (test_suite.getCanceledTests().get(id) == null && !suspended_ctx_ids.contains(id)) return false;
        IRunControl.RunControlContext ctx = ctx_map.get(id);
        if (ctx == null) return false;
        String grp = ctx.getRCGroup();
        if (grp != null) {
            for (String s : resume_cmds.keySet()) {
                IRunControl.RunControlContext c = ctx_map.get(s);
                if (c == null) return false;
                if (grp.equals(c.getRCGroup())) return false;
            }
        }
        return true;
    }

    void resume(final String id, final int mode) {
        if (!test_suite.canResume(id)) return;
        assert !sync_pending;
        sync_pending = true;
        Protocol.sync(new Runnable() {
            public void run() {
                sync_pending = false;
                if (test_suite.canResume(id)) {
                    assert resume_cmds.get(id) == null;
                    final String test_id = test_suite.getCanceledTests().get(id);
                    if (test_id != null) {
                        boolean ok = false;
                        if (enable_trace) System.out.println("" + channel_id + " cancel " + id);
                        if (rnd.nextInt(4) == 0) {
                            IRunControl.RunControlContext ctx = ctx_map.get(test_id);
                            if (ctx != null && ctx.canTerminate()) {
                                resume_cmds.put(id, ctx.terminate(new IRunControl.DoneCommand() {
                                    public void doneCommand(IToken token, Exception error) {
                                        assert resume_cmds.get(id) == token;
                                        resume_cmds.remove(id);
                                        if (enable_trace) System.out.println("" + channel_id + " done cancel " + error);
                                        if (error != null && ctx_map.get(test_id) != null) exit(error);
                                    }
                                }));
                                ok = true;
                            }
                        }
                        if (!ok && rnd.nextInt(4) == 0) {
                            IRunControl.RunControlContext ctx = ctx_map.get(test_id);
                            if (ctx != null && ctx.canDetach()) {
                                resume_cmds.put(id, ctx.detach(new IRunControl.DoneCommand() {
                                    public void doneCommand(IToken token, Exception error) {
                                        assert resume_cmds.get(id) == token;
                                        resume_cmds.remove(id);
                                        if (enable_trace) System.out.println("" + channel_id + " done detach " + error);
                                        if (error != null && ctx_map.get(test_id) != null) exit(error);
                                    }
                                }));
                                ok = true;
                            }
                        }
                        if (!ok) {
                            IDiagnostics diag = channel.getRemoteService(IDiagnostics.class);
                            resume_cmds.put(id, diag.cancelTest(test_id, new IDiagnostics.DoneCancelTest() {
                                public void doneCancelTest(IToken token, Throwable error) {
                                    assert resume_cmds.get(id) == token;
                                    resume_cmds.remove(id);
                                    if (enable_trace) System.out.println("" + channel_id + " done cancel " + error);
                                    if (error != null && ctx_map.get(test_id) != null) exit(error);
                                }
                            }));
                        }
                    }
                    else {
                        IRunControl.RunControlContext ctx = ctx_map.get(id);
                        if (ctx != null) {
                            pending_resume_ids.add(id);
                            if (enable_trace) System.out.println("" + channel_id + " resume " + mode + " " + id);
                            resume_cmds.put(id, ctx.resume(mode, 1, new IRunControl.DoneCommand() {
                                public void doneCommand(IToken token, Exception error) {
                                    assert resume_cmds.get(id) == token;
                                    resume_cmds.remove(id);
                                    if (enable_trace) System.out.println("" + channel_id + " done resume " + error);
                                    if (error != null) {
                                        pending_resume_ids.remove(id);
                                        if (suspended_ctx_ids.contains(id)) exit(error);
                                    }
                                    else if (pending_resume_ids.contains(id)) {
                                        exit(new Exception("Missing contextResumed event"));
                                    }
                                }
                            }));
                        }
                    }
                }
            }
        });
    }

    void cancel(String test_id) {
        for (IRunControl.RunControlContext ctx : ctx_map.values()) {
            if (ctx.hasState()) {
                if (test_id.equals(ctx.getID()) ||
                        test_id.equals(ctx.getParentID()) ||
                        test_id.equals(ctx.getCreatorID())) {
                    String thread_id = ctx.getID();
                    test_suite.getCanceledTests().put(thread_id, test_id);
                    resume(thread_id, 0);
                }
            }
        }
    }
}
