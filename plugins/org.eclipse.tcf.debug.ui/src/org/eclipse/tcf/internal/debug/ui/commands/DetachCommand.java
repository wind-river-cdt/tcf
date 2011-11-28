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
package org.eclipse.tcf.internal.debug.ui.commands;

import org.eclipse.tcf.internal.debug.ui.model.TCFNode;
import org.eclipse.tcf.internal.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IRunControl;
import org.eclipse.tcf.util.TCFDataCache;
import org.eclipse.tcf.util.TCFTask;

public class DetachCommand extends AbstractActionDelegate {

    private static boolean run(TCFNode[] nodes, final boolean dry_run) {
        if (nodes == null || nodes.length == 0) return false;
        for (TCFNode n : nodes) {
            boolean ok = false;
            while (!ok && n != null) {
                if (n instanceof TCFNodeExecContext) {
                    final TCFNodeExecContext exe = (TCFNodeExecContext)n;
                    ok = new TCFTask<Boolean>(n.getChannel()) {
                        public void run() {
                            TCFDataCache<IRunControl.RunControlContext> ctx_cache = exe.getRunContext();
                            if (!ctx_cache.validate(this)) return;
                            IRunControl.RunControlContext ctx_data = ctx_cache.getData();
                            if (ctx_data != null && ctx_data.canDetach()) {
                                if (dry_run) {
                                    done(true);
                                }
                                else {
                                    ctx_data.detach(new IRunControl.DoneCommand() {
                                        public void doneCommand(IToken token, Exception error) {
                                            if (error != null) {
                                                error(error);
                                            }
                                            else {
                                                exe.getModel().getLaunch().onDetach(exe.getID());
                                                done(true);
                                            }
                                        }
                                    });
                                }
                            }
                            else {
                                done(false);
                            }
                        }
                    }.getE();
                }
                n = n.getParent();
            }
            if (!ok) return false;
        }
        return true;
    }

    @Override
    protected void selectionChanged() {
        getAction().setEnabled(run(getSelectedNodes(), true));
    }

    @Override
    protected void run() {
        TCFNode[] nodes = getSelectedNodes();
        if (!run(nodes, true)) return;
        run(nodes, false);
    }
}
