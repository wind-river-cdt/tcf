/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.operations;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.tcf.filesystem.nls.Messages;
import org.eclipse.ui.PlatformUI;

/**
 * The operation that is executed as a back ground job.
 */
public class FSJobOperation extends FSOperation {
	// The callback
	protected ICallback callback;

	/**
	 * Create an instance with the specified callback.
	 *
	 * @param callback called when the creation is done.
	 */
	public FSJobOperation(ICallback callback) {
		this.callback = callback;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSOperation#doit()
	 */
	@Override
	public IStatus doit() {
		Assert.isNotNull(Display.getCurrent());
		Job job = new RunnableJob(Messages.FSCreate_JobTitle, this);
		job.addJobChangeListener(new JobChangeAdapter(){
			@Override
            public void done(final IJobChangeEvent event) {
				Display display = PlatformUI.getWorkbench().getDisplay();
				display.asyncExec(new Runnable(){
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
