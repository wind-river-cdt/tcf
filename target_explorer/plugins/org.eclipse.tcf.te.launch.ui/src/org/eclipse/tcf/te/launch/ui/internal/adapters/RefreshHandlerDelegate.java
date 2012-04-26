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
import org.eclipse.tcf.te.launch.ui.model.LaunchModel;
import org.eclipse.tcf.te.launch.ui.model.LaunchNode;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.ui.views.interfaces.handler.IRefreshHandlerDelegate;

/**
 * Launch tree node refresh handler delegate implementation.
 */
public class RefreshHandlerDelegate implements IRefreshHandlerDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.interfaces.handler.IRefreshHandlerDelegate#canRefresh(java.lang.Object)
	 */
	@Override
	public boolean canRefresh(Object element) {
		if (element instanceof LaunchNode) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.interfaces.handler.IRefreshHandlerDelegate#refresh(java.lang.Object, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
	 */
	@Override
	public void refresh(Object element, IPropertiesContainer state, ICallback callback) {
		Assert.isNotNull(element);
		Assert.isNotNull(state);

		if (canRefresh(element)) {
			LaunchNode node = (LaunchNode) element;
			LaunchModel model = LaunchModel.getLaunchModel(node.getRootModelNode());
			model.refresh();
		}
		if (callback != null) {
			callback.done(this, Status.OK_STATUS);
		}
	}
}
