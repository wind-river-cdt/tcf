/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.internal.adapters;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.launch.ui.activator.UIPlugin;
import org.eclipse.tcf.te.launch.ui.model.LaunchNode;
import org.eclipse.tcf.te.launch.ui.nls.Messages;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.utils.StatusHelper;
import org.eclipse.tcf.te.ui.views.interfaces.handler.IDeleteHandlerDelegate;

/**
 * File System tree node delete handler delegate implementation.
 */
public class DeleteHandlerDelegate implements IDeleteHandlerDelegate {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.interfaces.handler.IDeleteHandlerDelegate#canDelete(java.lang.Object)
	 */
	@Override
	public boolean canDelete(Object element) {
		if (element instanceof LaunchNode) {
			LaunchNode node = (LaunchNode)element;
			return LaunchNode.TYPE_LAUNCH_CONFIG.equals(node.getType()) && !node.getLaunchConfiguration().isReadOnly();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.interfaces.handler.IDeleteHandlerDelegate#delete(java.lang.Object, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
	 */
	@Override
	public void delete(Object element, IPropertiesContainer state, final ICallback callback) {
		Assert.isNotNull(element);
		Assert.isNotNull(state);

		if (element instanceof LaunchNode) {
			final LaunchNode node = (LaunchNode)element;
			try {
				if (MessageDialog.openQuestion(
								UIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(),
								Messages.DeleteHandlerDelegate_question_title, NLS.bind(Messages.DeleteHandlerDelegate_question_message, node.getLaunchConfiguration().getName()))) {
					node.getLaunchConfiguration().delete();
					if (callback != null) {
						callback.done(this, Status.OK_STATUS);
					}
				}
				else {
					if (callback != null) {
						callback.done(this, Status.CANCEL_STATUS);
					}
				}
			}
			catch (Exception e) {
				if (callback != null) {
					callback.done(this, StatusHelper.getStatus(e));
				}
			}
		}
	}
}
