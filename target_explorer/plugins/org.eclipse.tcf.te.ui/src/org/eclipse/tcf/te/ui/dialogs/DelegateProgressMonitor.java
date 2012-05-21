/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.dialogs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * A progress monitor delegate that wraps the parent monitor
 * and delegates to a progress monitor part.
 */
class DelegateProgressMonitor extends ProgressMonitorWrapper {
	// The progress monitor part to delegate the monitor call.
	ProgressMonitorPart fPmPart;
	
	/**
	 * Create an instance with the parent monitor and a progress
	 * monitor part.
	 * 
	 * @param parent a parent monitor to wrap
	 * @param mpart a progress monitor part to delegate to.
	 */
	public DelegateProgressMonitor(IProgressMonitor parent, ProgressMonitorPart mpart) {
		super(parent);
		fPmPart = mpart;
		safeRun(new Runnable() {
			@Override
			public void run() {
				if (!fPmPart.isDisposed()) {
					fPmPart.attachToCancelComponent(null);
					fPmPart.setCanceled(false);
					fPmPart.setVisible(true);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.ProgressMonitorWrapper#beginTask(java.lang.String, int)
	 */
	@Override
	public void beginTask(final String name, final int totalWork) {
		super.beginTask(name, totalWork);
		safeRun(new Runnable() {
			@Override
			public void run() {
				if (!fPmPart.isDisposed()) {
					fPmPart.beginTask(name, totalWork);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.ProgressMonitorWrapper#isCanceled()
	 */
	@Override
	public boolean isCanceled() {
		boolean cancel = fPmPart.isCanceled();
		return cancel || super.isCanceled();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.ProgressMonitorWrapper#subTask(java.lang.String)
	 */
	@Override
	public void subTask(final String name) {
		super.subTask(name);
		safeRun(new Runnable() {
			@Override
			public void run() {
				if (!fPmPart.isDisposed()) {
					fPmPart.subTask(name);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.ProgressMonitorWrapper#done()
	 */
	@Override
	public void done() {
		super.done();
		safeRun(new Runnable() {
			@Override
			public void run() {
				if (!fPmPart.isDisposed()) {
					fPmPart.done();
					fPmPart.setVisible(false);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.ProgressMonitorWrapper#setTaskName(java.lang.String)
	 */
	@Override
	public void setTaskName(final String name) {
		super.setTaskName(name);
		safeRun(new Runnable() {
			@Override
			public void run() {
				if (!fPmPart.isDisposed()) {
					fPmPart.setTaskName(name);
				}
			}
		});

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.ProgressMonitorWrapper#setCanceled(boolean)
	 */
	@Override
	public void setCanceled(final boolean b) {
		super.setCanceled(b);
		safeRun(new Runnable() {
			@Override
			public void run() {
				if (!fPmPart.isDisposed()) {
					fPmPart.setCanceled(b);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.ProgressMonitorWrapper#worked(int)
	 */
	@Override
	public void worked(final int work) {
		super.worked(work);
		safeRun(new Runnable() {
			@Override
			public void run() {
				if (!fPmPart.isDisposed()) {
					fPmPart.worked(work);
				}
			}
		});
	}
	
	/**
	 * Run the runnable in a UI thread asynchronously.
	 * 
	 * @param runnable The runnable to schedule.
	 */
	void safeRun(Runnable runnable) {
		if(Display.getCurrent() != null) {
			runnable.run();
		}
		else {
			PlatformUI.getWorkbench().getDisplay().asyncExec(runnable);
		}
	}
}
