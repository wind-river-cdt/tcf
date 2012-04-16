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
import org.eclipse.tcf.te.tcf.filesystem.core.activator.CorePlugin;
import org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IOperation;

/**
 * An operation executor that executes an operation directly and silently. 
 */
public class NullOpExecutor implements IOpExecutor {
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.IOpExecutor#execute(org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IOperation)
	 */
	@Override
	public IStatus execute(IOperation operation) {
		IProgressMonitor monitor = new NullProgressMonitor();
		try {
			monitor.setTaskName(operation.getName());
			monitor.beginTask(operation.getName(), operation.getTotalWork());
			operation.run(monitor);
			return Status.OK_STATUS;
		}
		catch (InvocationTargetException e) {
			return new Status(IStatus.ERROR, CorePlugin.getUniqueIdentifier(), e.getLocalizedMessage(), e);
		}
		catch (InterruptedException e) {
			return Status.CANCEL_STATUS;
		}
		finally {
			monitor.done();
		}
	}
}
