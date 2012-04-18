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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IOperation;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.JobExecutor;

/**
 * The interface is to defined an operation executor, which executes
 * the given operation providing the context including the progress monitor.
 * 
 * @see JobExecutor
 * @see UiExecutor
 */
public interface IOpExecutor {
	/**
	 * Execute the specified operation providing an execution
	 * context.
	 * 
	 * @param operation The operation to be executed.
	 * @return a result status of the execution.
	 */
	public IStatus execute(IOperation operation);
}
