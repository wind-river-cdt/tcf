/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.debug.ui.commands;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IResumeHandler;
import org.eclipse.tcf.internal.debug.actions.TCFAction;
import org.eclipse.tcf.internal.debug.model.TCFContextState;
import org.eclipse.tcf.internal.debug.ui.Activator;
import org.eclipse.tcf.internal.debug.ui.model.TCFModel;
import org.eclipse.tcf.internal.debug.ui.model.TCFNode;
import org.eclipse.tcf.internal.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IErrorReport;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IRunControl;
import org.eclipse.tcf.util.TCFDataCache;


public class ResumeCommand extends StepCommand implements IResumeHandler {

    public ResumeCommand(TCFModel model) {
        super(model);
    }

    @Override
    protected boolean canExecute(IRunControl.RunControlContext ctx) {
        return ctx.canResume(IRunControl.RM_RESUME);
    }

    @Override
    protected void execute(final IDebugCommandRequest monitor,
            final IRunControl.RunControlContext ctx, boolean src_step, final Runnable done) {
        new TCFAction(model.getLaunch(), ctx.getID()) {
            boolean cmd_sent;
            public void run() {
                if (cmd_sent) {
                    TCFNode node = model.getNode(ctx_id);
                    if (node instanceof TCFNodeExecContext) {
                        TCFDataCache<TCFContextState> state = ((TCFNodeExecContext)node).getState();
                        if (!state.validate(this)) return;
                    }
                    done();
                    return;
                }
                cmd_sent = true;
                ctx.resume(IRunControl.RM_RESUME, 1, new IRunControl.DoneCommand() {
                    public void doneCommand(IToken token, Exception error) {
                        if (error != null && model.getChannel().getState() == IChannel.STATE_OPEN) {
                            if (error instanceof IErrorReport) {
                                IErrorReport r = (IErrorReport)error;
                                if (r.getErrorCode() == IErrorReport.TCF_ERROR_ALREADY_RUNNING) {
                                    run();
                                    return;
                                }
                            }
                            launch.removeContextActions(getContextID());
                            monitor.setStatus(new Status(IStatus.ERROR,
                                    Activator.PLUGIN_ID, IStatus.OK, "Cannot resume: " + error.getLocalizedMessage(), error));
                        }
                        run();
                    }
                });
            }
            public void done() {
                super.done();
                done.run();
            }
        };
    }
}
