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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IOperation;
import org.eclipse.tcf.te.tcf.filesystem.ui.activator.UIPlugin;
import org.eclipse.ui.PlatformUI;

/**
 * The operation that is executed as a back ground job.
 */
public class JobExecutor implements IOpExecutor{
	// The callback
	protected ICallback callback;
	
	/**
	 * Create an instance with no callback.
	 */
	public JobExecutor() {
		this(null);
	}
	
	/**
	 * Create an instance with the specified callback.
	 *
	 * @param callback called when the creation is done.
	 */
	public JobExecutor(ICallback callback) {
		this.callback = callback;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.ui.internal.operations.IOpExecutor#execute(org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IOperation)
	 */
	@Override
    public IStatus execute(final IOperation operation) {
		Job job = new Job(operation.getName()){
			@Override
            protected IStatus run(IProgressMonitor monitor) {
				try {
					monitor.setTaskName(operation.getName());
					monitor.beginTask(operation.getName(), operation.getTotalWork());
					operation.run(monitor);
					return Status.OK_STATUS;
				}
				catch (InvocationTargetException e) {
					return new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), e.getLocalizedMessage(), e);
				}
				catch (InterruptedException e) {
					return Status.CANCEL_STATUS;
				}
				finally {
					monitor.done();
				}
            }};
		job.addJobChangeListener(new JobChangeAdapter(){
			@Override
            public void done(final IJobChangeEvent event) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable(){
					@Override
                    public void run() {
						doCallback(event);
                    }});
            }});
		job.schedule();
		return Status.OK_STATUS;
	}
	
	/**
	 * Called when the creation is done. Must be called within UI-thread.
	 * 
	 * @param event The job change event.
	 */
	void doCallback(IJobChangeEvent event) {
		Assert.isNotNull(Display.getCurrent());
		IStatus status = event.getResult();
		if(callback != null) {
			callback.done(this, status);
		}
	}	
}