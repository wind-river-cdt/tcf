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

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IURIPersistenceService;
import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
import org.eclipse.tcf.te.runtime.services.ServiceManager;
import org.eclipse.tcf.te.runtime.statushandler.StatusHandlerManager;
import org.eclipse.tcf.te.runtime.statushandler.interfaces.IStatusHandler;
import org.eclipse.tcf.te.runtime.statushandler.interfaces.IStatusHandlerConstants;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelRefreshService;
import org.eclipse.tcf.te.tcf.locator.model.Model;
import org.eclipse.tcf.te.tcf.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.ui.help.IContextHelpIds;
import org.eclipse.tcf.te.tcf.ui.nls.Messages;
import org.eclipse.tcf.te.ui.views.interfaces.handler.IDeleteHandlerDelegate;

/**
 * Peer model node delete handler delegate implementation.
 */
public class DeleteHandlerDelegate implements IDeleteHandlerDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.interfaces.handler.IDeleteHandlerDelegate#canDelete(java.lang.Object)
	 */
	@Override
	public boolean canDelete(final Object element) {
		if (element instanceof IPeerModel) {
			final AtomicBoolean canDelete = new AtomicBoolean();

			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					String value = ((IPeerModel)element).getPeer().getAttributes().get("static.transient"); //$NON-NLS-1$
					canDelete.set(value != null && Boolean.parseBoolean(value.trim()));
				}
			};

			if (Protocol.isDispatchThread()) {
				runnable.run();
			}
			else {
				Protocol.invokeAndWait(runnable);
			}

			return canDelete.get();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.interfaces.handler.IDeleteHandlerDelegate#delete(java.lang.Object, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
	 */
	@Override
	public void delete(Object element, IPropertiesContainer state, ICallback callback) {
		Assert.isNotNull(element);
		Assert.isNotNull(state);

		if (canDelete(element)) {
			try {
				IURIPersistenceService service = ServiceManager.getInstance().getService(IURIPersistenceService.class);
				if (service == null) {
					throw new IOException("Persistence service instance unavailable."); //$NON-NLS-1$
				}
				service.delete(element, null);
			} catch (IOException e) {
				// Create the status
				IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
								Messages.DeleteHandler_error_deleteFailed, e);

				// Fill in the status handler custom data
				IPropertiesContainer data = new PropertiesContainer();
				data.setProperty(IStatusHandlerConstants.PROPERTY_TITLE, Messages.DeleteHandler_error_title);
				data.setProperty(IStatusHandlerConstants.PROPERTY_CONTEXT_HELP_ID, IContextHelpIds.MESSAGE_DELETE_FAILED);
				data.setProperty(IStatusHandlerConstants.PROPERTY_CALLER, this);

				// Get the status handler
				IStatusHandler[] handler = StatusHandlerManager.getInstance().getHandler(element);
				if (handler.length > 0) {
					handler[0].handleStatus(status, data, null);
				}
			}

			// Get the locator model
			final ILocatorModel model = Model.getModel();
			if (model != null) {
				// Trigger a refresh of the model
				final ILocatorModelRefreshService service = model.getService(ILocatorModelRefreshService.class);
				if (service != null) {
					Protocol.invokeLater(new Runnable() {
						@Override
						public void run() {
							// Refresh the model now (must be executed within the TCF dispatch thread)
							service.refresh();
						}
					});
				}
			}
		} else {
			if (callback != null) {
				callback.done(this, Status.OK_STATUS);
			}
		}
	}

}
