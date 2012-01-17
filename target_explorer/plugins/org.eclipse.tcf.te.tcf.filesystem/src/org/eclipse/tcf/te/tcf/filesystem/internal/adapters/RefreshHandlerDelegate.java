/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.adapters;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.tcf.filesystem.internal.handlers.RefreshNodeJob;
import org.eclipse.tcf.te.tcf.filesystem.internal.utils.PersistenceManager;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.ui.views.interfaces.handler.IRefreshHandlerDelegate;

/**
 * File System tree node refresh handler delegate implementation.
 */
public class RefreshHandlerDelegate implements IRefreshHandlerDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.interfaces.handler.IRefreshHandlerDelegate#canRefresh(java.lang.Object)
	 */
	@Override
	public boolean canRefresh(Object element) {
		if (element instanceof FSTreeNode) {
			FSTreeNode node = (FSTreeNode) element;
			return node.isSystemRoot() || node.isRoot() || node.isDirectory() 
							|| node.isFile() && !PersistenceManager.getInstance().isAutoSaving();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.interfaces.handler.IRefreshHandlerDelegate#refresh(java.lang.Object, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
	 */
	@Override
	public void refresh(Object element, IPropertiesContainer state, final ICallback callback) {
		Assert.isNotNull(element);
		Assert.isNotNull(state);

		if (canRefresh(element)) {
			Job job = new RefreshNodeJob((FSTreeNode) element);
			job.addJobChangeListener(new JobChangeAdapter(){
				@Override
                public void done(IJobChangeEvent event) {
					if(callback != null) {
						callback.done(this, event.getResult());
					}
                }});
			job.schedule();
		}
		else {
			if (callback != null) callback.done(this, Status.OK_STATUS);
		}
	}
}
