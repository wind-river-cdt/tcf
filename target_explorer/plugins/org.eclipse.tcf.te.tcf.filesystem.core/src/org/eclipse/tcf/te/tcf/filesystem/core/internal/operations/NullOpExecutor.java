/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.core.internal.operations;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.tcf.filesystem.core.activator.CorePlugin;
import org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IOperation;

/**
 * An operation executor that executes an operation directly and silently. 
 */
public class NullOpExecutor implements IOpExecutor {
	// The callback being invoked after execution.
	ICallback callback;
	
	/**
	 * Empty argument constructor
	 */
	public NullOpExecutor() {
		this(null);
	}
	
	/**
	 * Create an instance with a callback.
	 * 
	 * @param callback The callback to be invoked after execution.
	 */
	public NullOpExecutor(ICallback callback) {
		this.callback = callback;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.IOpExecutor#execute(org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IOperation)
	 */
	@Override
	public IStatus execute(IOperation operation) {
		IProgressMonitor monitor = new NullProgressMonitor();
		IStatus status;
		try {
			monitor.setTaskName(operation.getName());
			monitor.beginTask(operation.getName(), operation.getTotalWork());
			operation.run(monitor);
			status = Status.OK_STATUS;
		}
		catch (InvocationTargetException e) {
			Throwable throwable = e.getTargetException();
			status = new Status(IStatus.ERROR, CorePlugin.getUniqueIdentifier(), throwable.getMessage(), throwable);
		}
		catch (InterruptedException e) {
			status = Status.CANCEL_STATUS;
		}
		finally {
			monitor.done();
		}
		if(callback != null) callback.done(operation, status);
		return status;
	}
}
