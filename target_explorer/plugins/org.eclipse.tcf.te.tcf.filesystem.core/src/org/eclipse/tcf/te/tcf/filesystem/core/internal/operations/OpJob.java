/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
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
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.tcf.te.tcf.filesystem.core.activator.CorePlugin;
import org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IOperation;

/**
 * The job that wraps an IOperation object.
 */
public class OpJob extends Job {
	// The operation object.
	private IOperation operation;

	/**
	 * Create a job with the specified name and the runnable object.
	 * 
	 * @param name The job's name.
	 * @param operation The runnable object.
	 */
	public OpJob(String name, IOperation operation) {
		super(name);
		this.operation = operation;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			operation.run(monitor);
		}
		catch (InvocationTargetException e) {
			return new Status(IStatus.ERROR, CorePlugin.getUniqueIdentifier(), e.getLocalizedMessage(), e);
		}
		catch (InterruptedException e) {
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}
}
