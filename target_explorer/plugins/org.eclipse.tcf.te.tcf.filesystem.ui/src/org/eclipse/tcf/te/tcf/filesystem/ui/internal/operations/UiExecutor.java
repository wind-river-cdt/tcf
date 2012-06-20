/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
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
import org.eclipse.tcf.te.tcf.filesystem.core.internal.exceptions.TCFException;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.IOpExecutor;
import org.eclipse.tcf.te.tcf.filesystem.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.ui.dialogs.TimeTriggeredProgressMonitorDialog;
import org.eclipse.ui.PlatformUI;

/**
 * The operation that is executed in an interactive progress dialog.
 */
public class UiExecutor implements IOpExecutor {
	// The callback
	protected ICallback callback;
	
	/**
	 * Create a UI executor with no callback.
	 */
	public UiExecutor() {
		this(null);
	}
	
	/**
	 * Create a UI executor with a callback that will be 
	 * invoked after execution.
	 * 
	 * @param callback The callback to be invoked after execution.
	 */
	public UiExecutor(ICallback callback) {
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
				try {
					monitor.setTaskName(operation.getName());
					monitor.beginTask(operation.getName(), operation.getTotalWork());
					operation.run(monitor);
				}
				finally {
					monitor.done();
				}
			}};
		dialog.setCancelable(true);
		IStatus status = null;
		try {
			dialog.run(true, true, runnable);
			status = Status.OK_STATUS;
		}
		catch (InvocationTargetException e) {
			// Display the error during copy.
			Throwable throwable = e.getTargetException();
			if(throwable instanceof TCFException) {
				int severity = ((TCFException)throwable).getSeverity();
				status = new Status(severity, UIPlugin.getUniqueIdentifier(), throwable.getMessage(), throwable);
			}
			else {
				status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), throwable.getMessage(), throwable);
			}
			MessageDialog.openError(parent, operation.getName(), throwable.getMessage());
		}
		catch (InterruptedException e) {
			// It is canceled.
			status = Status.OK_STATUS;
		}
		if (callback != null) callback.done(operation, status);
		return status;
	}
}
