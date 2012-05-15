/*******************************************************************************
 * Copyright (c) 2009, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.debug.ui.commands;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.internal.debug.ui.model.TCFModel;
import org.eclipse.tcf.internal.debug.ui.model.TCFNode;
import org.eclipse.tcf.internal.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tcf.internal.debug.ui.model.TCFNodeExpression;
import org.eclipse.tcf.internal.debug.ui.model.TCFNodeLaunch;
import org.eclipse.tcf.internal.debug.ui.model.TCFNodeModule;
import org.eclipse.tcf.internal.debug.ui.model.TCFNodeStackFrame;

public class SignalsCommand extends AbstractActionDelegate {

    private static boolean isValidNode(TCFNode n) {
        if (n instanceof TCFNodeLaunch) return true;
        if (n instanceof TCFNodeExecContext) return true;
        if (n instanceof TCFNodeStackFrame) return true;
        if (n instanceof TCFNodeExpression) return true;
        if (n instanceof TCFNodeModule) return true;
        return false;
    }

    protected void selectionChanged() {
        TCFNode n = getSelectedNode();
        getAction().setEnabled(isValidNode(n));
    }

    protected void run() {
        TCFNode n = getSelectedNode();
        if (isValidNode(n)) {
            Shell shell = getWindow().getShell();
            try {
                new SignalsDialog(shell, n).open();
            }
            catch (Throwable x) {
                MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                mb.setText("Cannot open Signals dialog");
                mb.setMessage(TCFModel.getErrorMessage(x, true));
                mb.open();
            }
        }
    }
}
