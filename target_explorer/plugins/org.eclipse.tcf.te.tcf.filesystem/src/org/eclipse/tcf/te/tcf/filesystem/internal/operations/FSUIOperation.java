/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.operations;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.dialogs.TimeTriggeredProgressMonitorDialog;
import org.eclipse.tcf.te.tcf.filesystem.nls.Messages;
import org.eclipse.ui.PlatformUI;

/**
 * The operation that is executed in an interactive progress dialog.
 */
public class FSUIOperation extends FSOperation {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSOperation#doit()
	 */
	@Override
	public IStatus doit() {
		Assert.isNotNull(Display.getCurrent());
		Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		TimeTriggeredProgressMonitorDialog dialog = new TimeTriggeredProgressMonitorDialog(parent, 250);
		dialog.setCancelable(true);
		try {
			dialog.run(true, true, this);
		}
		catch (InvocationTargetException e) {
			// Display the error during copy.
			Throwable throwable = e.getTargetException() != null ? e.getTargetException() : e;
			MessageDialog.openError(parent, Messages.FSCopy_CopyFileFolderTitle, throwable.getLocalizedMessage());
			return new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), throwable.getLocalizedMessage(), throwable);
		}
		catch (InterruptedException e) {
			// It is canceled.
		}
		return Status.OK_STATUS;
	}
}
