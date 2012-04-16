/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.adapters;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Status;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.IOpExecutor;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpRefresh;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.ui.internal.operations.JobExecutor;
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
							|| node.isFile() && !UIPlugin.isAutoSaving();
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
			final FSTreeNode node = (FSTreeNode) element;
			if (node.isSystemRoot() || node.isRoot() || node.isDirectory()) {
				IOpExecutor executor = new JobExecutor(callback);
				executor.execute(new OpRefresh(node));
			}
			else if (node.isFile() && !UIPlugin.isAutoSaving()) {
				node.refreshState(callback);
			}
		}
		else {
			if (callback != null) {
				callback.done(this, Status.OK_STATUS);
			}
		}
	}
}
