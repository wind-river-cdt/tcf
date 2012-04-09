/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.operations;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IOperation;
import org.eclipse.tcf.te.tcf.filesystem.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.ui.dialogs.TimeTriggeredProgressMonitorDialog;
import org.eclipse.ui.PlatformUI;

/**
 * The operation that is executed in an interactive progress dialog.
 */
public class UiExecutor implements IOpExecutor {
	// The callback
	protected ICallback callback;
	// The operation's name to be displayed.
	private String opName;
	
	/**
	 * Create a UI executor with the specified name.
	 * 
	 * @param opName The operation's name.
	 */
	public UiExecutor(String opName) {
		this(opName, null);
	}
	
	/**
	 * Create a UI executor with the specified name and
	 * a callback that will be invoked after execution.
	 * 
	 * @param opName The operation's name.
	 * @param callback The callback to be invoked after execution.
	 */
	public UiExecutor(String opName, ICallback callback) {
		this.opName = opName;
		this.callback = callback;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.ui.internal.operations.IOpExecutor#execute(org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IOperation)
	 */
	@Override
    public IStatus execute(final IOperation operation) {
		Assert.isNotNull(Display.getCurrent());
		Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		TimeTriggeredProgressMonitorDialog dialog = new TimeTriggeredProgressMonitorDialog(parent, 250);
		final IRunnableWithProgress runnable = new IRunnableWithProgress() {
			@Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				operation.run(monitor);
            }};
		dialog.setCancelable(true);
		IStatus status = null;
		try {
			dialog.run(true, true, runnable);
			status = Status.OK_STATUS;
		}
		catch (InvocationTargetException e) {
			// Display the error during copy.
			Throwable throwable = e.getTargetException() != null ? e.getTargetException() : e;
			MessageDialog.openError(parent, opName, throwable.getLocalizedMessage());
			status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), throwable.getLocalizedMessage(), throwable);
		}
		catch (InterruptedException e) {
			// It is canceled.
			status = Status.OK_STATUS;
		}
		if(callback != null) {
			callback.done(this, status);
		}
		return status;
	}
}
