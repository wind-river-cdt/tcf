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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.utils.StatusHelper;

/**
 * Basic process launcher implementation.
 * <p>
 * The associated callback is called after <code>launch()</code> finishes. The throwable which will
 * be associated to the callback at the time of invocation is either any exception thrown by the
 * <code>launch()</code> method or if no exception has been thrown, the associated error if any
 * (call <code>setError(Throwable)</code> to associated an error).
 * <p>
 * In case a <code>IProgressMonitor</code> is given, a new <code>SubProgressMonitor</code> will be
 * opened for the launching stage. The <code>SubProgressMonitor</code> will use the given number of
 * ticks from the parent progress monitor.
 */
public abstract class ProcessLauncher implements Runnable {
	private ICallback callback;
	private IProgressMonitor progress;
	private Throwable error;

	/**
	 * Constructor.
	 *
	 * @param progress The progress monitor to provide progress feedback to the user or <code>null</code>.
	 * @param callback The callback to invoke if the launch finished or <code>null</code>.
	 * @param ticksToUse The number of ticks to use for the sub progress monitor.
	 */
	public ProcessLauncher(IProgressMonitor progress, ICallback callback, int ticksToUse) {
		this.callback = callback;
		if (progress != null) {
			this.progress = new SubProgressMonitor(progress, ticksToUse);
		}
		else {
			this.progress = new NullProgressMonitor();
		}
		setError(null);
	}

	/**
	 * Returns the associated error. This error will be passed to the callback if the launch has
	 * finished.
	 */
	public Throwable getError() {
		return error;
	}

	/**
	 * Associated the given error to pass to the callback if the launch finishes.
	 *
	 * @param error The error to pass or <code>null</code>.
	 */
	public void setError(Throwable error) {
		this.error = error;
	}

	/**
	 * Starts the task with the given name. The task will consume the given number of ticks till
	 * finished.
	 *
	 * @param taskName The task name for set for the task.
	 * @param ticks The number of ticks till the task finishes.
	 *
	 * @throws OperationCanceledException If the progress monitor has been canceled in the meantime.
	 */
	protected void progressBeginTask(String taskName, int ticks) throws OperationCanceledException {
		progressCancelCheck();
		progress.beginTask(taskName, ticks);
	}

	/**
	 * Check if the progress monitor has been canceled already. If yes, the method will throw an
	 * <code>OperationCanceledExceptions</code>
	 *
	 * @throws OperationCanceledException If the progress monitor has been canceled in the meantime.
	 */
	protected void progressCancelCheck() throws OperationCanceledException {
		if (progress.isCanceled()) {
			throw new OperationCanceledException();
		}
	}

	/**
	 * Move the given amount of ticks forward in progress.
	 *
	 * @param ticks The amount of ticks to set as worked.
	 * @throws OperationCanceledException If the progress monitor has been canceled in the meantime.
	 */
	protected void progressWorked(int ticks) throws OperationCanceledException {
		progressCancelCheck();
		progress.worked(ticks);
	}

	/**
	 * Set the given sub task name to the progress monitor.
	 *
	 * @param subTask The sub task name to set.
	 * @throws OperationCanceledException If the progress monitor has been canceled in the meantime.
	 */
	protected void progressSubtask(String subTask) throws OperationCanceledException {
		progressCancelCheck();
		progress.subTask(subTask);
	}

	/**
	 * Set the progress monitor done.
	 */
	protected void progressDone() {
		progress.done();
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public final void run() {
		Throwable error = null;
		try {
			launch();
		}
		catch (Throwable throwable) {
			error = throwable;
		}
		finally {
			if (error == null) {
				error = getError();
			}
		}

		// invoke the callback if any is associated.
		if (callback != null) {
			callback.done(ProcessLauncher.this, StatusHelper.getStatus(error));
		}

		// Re-throw any Error except assertion errors ! NEVER REMOVE THIS !
		if ((error instanceof Error) && !(error instanceof AssertionError)) {
			throw (Error) error;
		}
	}

	/**
	 * Override to execute the launch.
	 *
	 * @throws Throwable In case something went wrong, the method may throw the problem wrapped
	 *             within a <code>Throwable</code>.
	 */
	public abstract void launch() throws Throwable;
}
