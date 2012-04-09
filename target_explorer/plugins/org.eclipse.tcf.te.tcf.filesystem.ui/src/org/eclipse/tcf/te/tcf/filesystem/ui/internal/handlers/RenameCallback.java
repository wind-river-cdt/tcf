/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.handlers;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.te.runtime.callback.Callback;
import org.eclipse.tcf.te.tcf.filesystem.ui.nls.Messages;
import org.eclipse.ui.PlatformUI;

public class RenameCallback extends Callback implements Runnable {
	private String message;
	@Override
    protected void internalDone(Object caller, IStatus status) {
		if (!status.isOK()) {
			message = status.getMessage();
			PlatformUI.getWorkbench().getDisplay().asyncExec(this);
		}
	}
	@Override
    public void run() {
		Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		MessageDialog.openError(parent, Messages.FSRename_RenameFileFolderTitle, message);
    }
}
