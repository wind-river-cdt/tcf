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

class DelegateProgressMonitor extends ProgressMonitorWrapper {
	ProgressMonitorPart fProgressMonitor;
	public DelegateProgressMonitor(IProgressMonitor monitor, ProgressMonitorPart monitorPart) {
		super(monitor);
		fProgressMonitor = monitorPart;
		safeRun(new Runnable() {
			@Override
			public void run() {
				if (!fProgressMonitor.isDisposed()) {
					fProgressMonitor.attachToCancelComponent(null);
					fProgressMonitor.setCanceled(false);
					fProgressMonitor.setVisible(true);
				}
			}
		});
	}

	@Override
	public void beginTask(final String name, final int totalWork) {
		super.beginTask(name, totalWork);
		safeRun(new Runnable() {
			@Override
			public void run() {
				if (!fProgressMonitor.isDisposed()) {
					fProgressMonitor.beginTask(name, totalWork);
				}
			}
		});
	}

	@Override
	public boolean isCanceled() {
		boolean cancel = fProgressMonitor.isCanceled();
		return cancel || super.isCanceled();
	}

	@Override
	public void subTask(final String name) {
		super.subTask(name);
		safeRun(new Runnable() {
			@Override
			public void run() {
				if (!fProgressMonitor.isDisposed()) {
					fProgressMonitor.subTask(name);
				}
			}
		});
	}

	@Override
	public void done() {
		super.done();
		safeRun(new Runnable() {
			@Override
			public void run() {
				if (!fProgressMonitor.isDisposed()) {
					fProgressMonitor.done();
					fProgressMonitor.setVisible(false);
				}
			}
		});
	}

	@Override
	public void setTaskName(final String name) {
		super.setTaskName(name);
		safeRun(new Runnable() {
			@Override
			public void run() {
				if (!fProgressMonitor.isDisposed()) {
					fProgressMonitor.setTaskName(name);
				}
			}
		});

	}

	@Override
	public void setCanceled(final boolean b) {
		super.setCanceled(b);
		safeRun(new Runnable() {
			@Override
			public void run() {
				if (!fProgressMonitor.isDisposed()) {
					fProgressMonitor.setCanceled(b);
				}
			}
		});
	}

	@Override
	public void worked(final int work) {
		super.worked(work);
		safeRun(new Runnable() {
			@Override
			public void run() {
				if (!fProgressMonitor.isDisposed()) {
					fProgressMonitor.worked(work);
				}
			}
		});
	}
	
	void safeRun(Runnable runnable) {
		if(Display.getCurrent() != null) {
			runnable.run();
		}
		else {
			PlatformUI.getWorkbench().getDisplay().asyncExec(runnable);
		}
	}
}
