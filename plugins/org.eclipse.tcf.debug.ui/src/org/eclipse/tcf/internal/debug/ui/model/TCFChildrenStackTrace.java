/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.debug.ui.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.tcf.internal.debug.model.TCFContextState;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IStackTrace;
import org.eclipse.tcf.util.TCFDataCache;


public class TCFChildrenStackTrace extends TCFChildren {

    private final TCFNodeExecContext node;

    private String top_frame_id;
    private int limit_factor = 1;

    TCFChildrenStackTrace(TCFNodeExecContext node) {
        super(node, 16);
        this.node = node;
    }

    void onSourceMappingChange() {
        for (TCFNode n : getNodes()) ((TCFNodeStackFrame)n).onSourceMappingChange();
    }

    void onExpressionAddedOrRemoved() {
        for (TCFNode n : getNodes()) ((TCFNodeStackFrame)n).onExpressionAddedOrRemoved();
    }

    void onSuspended(boolean func_call) {
        limit_factor = 1;
        for (TCFNode n : getNodes()) ((TCFNodeStackFrame)n).onSuspended(func_call);
        reset();
    }

    void onRegistersChanged() {
        for (TCFNode n : getNodes()) ((TCFNodeStackFrame)n).onRegistersChanged();
        reset();
    }

    void onMemoryMapChanged() {
        for (TCFNode n : getNodes()) ((TCFNodeStackFrame)n).onMemoryMapChanged();
        reset();
    }

    void onMemoryChanged() {
        for (TCFNode n : getNodes()) ((TCFNodeStackFrame)n).onMemoryChanged();
        reset();
    }

    void onRegisterValueChanged() {
        for (TCFNode n : getNodes()) ((TCFNodeStackFrame)n).onRegisterValueChanged();
        reset();
    }

    void onPreferencesChanged() {
        reset();
    }

    void riseTraceLimit() {
        limit_factor++;
        reset();
    }

    void postAllChangedDelta() {
        for (TCFNode n : getNodes()) ((TCFNodeStackFrame)n).postAllChangedDelta();
    }

    Boolean checkHasChildren(Runnable done) {
        TCFDataCache<TCFContextState> state = node.getState();
        if (!state.validate(done)) return null;
        if (node.isNotActive()) return false;
        if (state.getError() != null) return false;
        TCFContextState state_data = state.getData();
        if (state_data == null || !state_data.is_suspended) return false;
        return true;
    }

    public TCFNodeStackFrame getTopFrame() {
        assert isValid();
        return (TCFNodeStackFrame)node.model.getNode(top_frame_id);
    }

    @Override
    public void set(IToken token, Throwable error, Map<String,TCFNode> data) {
        for (TCFNode n : getNodes()) {
            if (data == null || data.get(n.id) == null) ((TCFNodeStackFrame)n).setFrameNo(-1);
        }
        super.set(token, error, data);
    }

    private void addEmulatedTopFrame(HashMap<String,TCFNode> data) {
        top_frame_id = node.id + "-TF";
        TCFNodeStackFrame n = (TCFNodeStackFrame)node.model.getNode(top_frame_id);
        if (n == null) n = new TCFNodeStackFrame(node, top_frame_id, true);
        n.setFrameNo(0);
        n.setTraceLimit(false);
        data.put(n.id, n);
    }

    @Override
    protected boolean startDataRetrieval() {
        Boolean has_children = checkHasChildren(this);
        if (has_children == null) return false;
        final HashMap<String,TCFNode> data = new HashMap<String,TCFNode>();
        if (!has_children) {
            top_frame_id = null;
            set(null, node.getState().getError(), data);
            return true;
        }
        IStackTrace st = node.model.getLaunch().getService(IStackTrace.class);
        if (st == null) {
            addEmulatedTopFrame(data);
            set(null, null, data);
            return true;
        }
        assert command == null;
        command = st.getChildren(node.id, new IStackTrace.DoneGetChildren() {
            public void doneGetChildren(IToken token, Exception error, String[] contexts) {
                if (command == token) {
                    if (error == null && contexts != null) {
                        int limit_value = 0;
                        boolean limit_enabled = node.model.getStackFramesLimitEnabled();
                        if (limit_enabled) {
                            limit_value = node.model.getStackFramesLimitValue() * limit_factor;
                            if (limit_value <= 0) limit_value = limit_factor;
                        }
                        int cnt = contexts.length;
                        for (String id : contexts) {
                            cnt--;
                            if (!limit_enabled || cnt <= limit_value) {
                                TCFNodeStackFrame n = (TCFNodeStackFrame)node.model.getNode(id);
                                if (n == null) n = new TCFNodeStackFrame(node, id, false);
                                assert n.parent == node;
                                n.setFrameNo(cnt);
                                n.setTraceLimit(limit_enabled && cnt == limit_value);
                                data.put(id, n);
                                if (cnt == 0) top_frame_id = id;
                            }
                        }
                    }
                    if (data.size() == 0) addEmulatedTopFrame(data);
                    set(token, error, data);
                }
            }
        });
        return false;
    }
}
