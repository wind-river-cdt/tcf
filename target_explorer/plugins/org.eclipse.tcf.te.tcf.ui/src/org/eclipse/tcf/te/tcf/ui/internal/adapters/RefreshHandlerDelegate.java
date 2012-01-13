/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.internal.adapters;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Status;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelRefreshService;
import org.eclipse.tcf.te.ui.views.interfaces.handler.IRefreshHandlerDelegate;

/**
 * Peer model node refresh handler delegate implementation.
 */
public class RefreshHandlerDelegate implements IRefreshHandlerDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.interfaces.handler.IRefreshHandlerDelegate#canRefresh(java.lang.Object)
	 */
	@Override
	public boolean canRefresh(Object element) {
		if (element instanceof IPeerModel && ((IPeerModel)element).getAdapter(ILocatorModel.class) != null) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.interfaces.handler.IRefreshHandlerDelegate#refresh(java.lang.Object, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
	 */
	@Override
	public void refresh(final Object element, final IPropertiesContainer state, final ICallback callback) {
		Assert.isNotNull(element);
		Assert.isNotNull(state);

		if (canRefresh(element)) {
			// To avoid refreshing the same locator model again and again for
			// a multi peer node selection, store the already refreshed locator
			// models to the state properties container.
			List<ILocatorModel> refreshedModels = (List<ILocatorModel>)state.getProperty("refreshedModels"); //$NON-NLS-1$
			if (refreshedModels == null) {
				refreshedModels = new ArrayList<ILocatorModel>();
				state.setProperty("refreshedModels", refreshedModels); //$NON-NLS-1$
			}

			final ILocatorModel model = (ILocatorModel)((IAdaptable)element).getAdapter(ILocatorModel.class);
			if (model != null && !refreshedModels.contains(model)) {
				refreshedModels.add(model);

				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						model.getService(ILocatorModelRefreshService.class).refresh();
						if (callback != null) callback.done(this, Status.OK_STATUS);
					}
				};

				if (Protocol.isDispatchThread()) runnable.run();
				else Protocol.invokeLater(runnable);
			} else {
				if (callback != null) callback.done(this, Status.OK_STATUS);
			}
		} else {
			if (callback != null) callback.done(this, Status.OK_STATUS);
		}
	}
}
