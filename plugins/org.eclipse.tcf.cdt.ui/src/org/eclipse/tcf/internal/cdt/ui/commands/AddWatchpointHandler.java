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

package org.eclipse.tcf.internal.cdt.ui.commands;

import org.eclipse.cdt.debug.ui.breakpoints.IToggleBreakpointsTargetCExtension;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class AddWatchpointHandler extends AbstractHandler {
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchPart part = HandlerUtil.getActivePart(event);
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        IToggleBreakpointsTargetManager toggleMgr = DebugUITools.getToggleBreakpointsTargetManager();
        IToggleBreakpointsTarget target = toggleMgr.getToggleBreakpointsTarget(part, selection);
        if (target != null && target instanceof IToggleBreakpointsTargetCExtension) {
            try {
                ((IToggleBreakpointsTargetCExtension)target).createWatchpointsInteractive(part, selection);
            } catch (CoreException e) {
                ErrorDialog.openError(part.getSite().getShell(), "Error", "Unable to create new watchpoing", e.getStatus());
                DebugPlugin.log(e.getStatus());
            }
        } else {
            ErrorDialog.openError(part.getSite().getShell(), "Error", "Unable to create new watchpoing.  Watchpoints are not supported by the active breakpoint type.", null);
        }
        return null;
    }
}
