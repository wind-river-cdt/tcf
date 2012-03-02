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
 * A Runnable that kills a Process after a given timeout.
 * <p>
 * To be used inside a Thread:
 * <pre>     ProcessKiller killer = new ProcessKiller(myProcess,1000);
 *     new Thread(killer).start();
 *     ...
 *     if( killer.isKilled() ) ...
 * </pre>
 */
public class ProcessKiller implements Runnable {
	// Reference to the process handle
	final Process process;
	// The timeout to wait for the process to finish
	final long timeout;
	// Flag set once the process finished
	private boolean finished;
	// Flag set once the process got killed
	boolean killed;

	/**
	 * Constructor.
	 *
	 * @param process The process to monitor. Must not be <code>null</code>.
	 * @param timeout The timeout in milliseconds to wait for the process to finish,
	 * 				  or <code>-1</code> to kill the process immediately.
	 */
	public ProcessKiller(Process process, long timeout) {
		super();

		Assert.isNotNull(process);
		this.process = process;
		this.timeout = timeout;

		this.killed = false;
		this.finished = false;
	}

	@Override
    public void run() {
		try {
			if (timeout >= 0) Thread.sleep(timeout);
			try {
				process.exitValue();
				setFinished(true);
			} catch(IllegalThreadStateException dummy) {
				process.destroy();
				setKilled(true);
			}
		} catch(InterruptedException e) {
			/* ignored on purpose */
		}
	}

	/**
	 * Sets the process killed state.
	 *
	 * @param killed <code>True</code> if the process got killed, <code>false</code> otherwise.
	 */
	protected synchronized void setKilled(boolean killed) {
		this.killed = killed;
	}

	/**
	 * Returns if the process got killed.
	 *
	 * @return <code>True</code> if the process got killed, <code>false</code> otherwise.
	 */
	public synchronized boolean isKilled() {
		return killed;
	}

	/**
	 * Sets the process finished state.
	 *
	 * @param finished <code>True</code> if the process is finished, <code>false</code> otherwise.
	 */
	protected synchronized void setFinished(boolean finished) {
		this.finished = finished;
	}

	/**
	 * Returns if the process has been finished.
	 *
	 * @return <code>True</code> if the process is finished, <code>false</code> otherwise.
	 */
	public synchronized boolean isFinished() {
		return finished;
	}
}
