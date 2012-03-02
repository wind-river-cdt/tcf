/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.processes;

import org.eclipse.core.runtime.Assert;

/**
 * A simple process waiter class. The process waiter keeps "running" till the observed process
 * terminates or the process waiter is interrupted from external.
 */
public class ProcessWaiter extends Thread {
	// Reference to the process handle
	private Process process;
	// Flag set once the process finished
	private boolean finished;
	// The exit code of the process
	private int exitCode;

	/**
	 * Constructor.
	 *
	 * @param process The process to monitor. Must not be <code>null</code>.
	 */
	public ProcessWaiter(final Process process) {
		super();

		Assert.isNotNull(process);
		this.process = process;
		this.finished = false;
		this.exitCode = -1;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		try {
			exitCode = process.waitFor();
		}
		catch (InterruptedException e) {
			/* ignored on purpose */
		}
		finished = true;
	}

	/**
	 * Returns if or if not the monitored process finished yet.
	 *
	 * @return <code>true</code> if the process finished yet, <code>false</code> otherwise
	 */
	public final boolean isFinished() {
		return finished;
	}

	/**
	 * Returns the process exit code the waiter had been monitored.
	 *
	 * @return The process exit code or <code>-1</code>.
	 */
	public final int getExitCode() {
		return exitCode;
	}
}
