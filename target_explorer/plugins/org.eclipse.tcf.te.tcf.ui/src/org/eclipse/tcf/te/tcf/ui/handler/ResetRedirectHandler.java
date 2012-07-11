/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IURIPersistenceService;
import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
import org.eclipse.tcf.te.runtime.services.ServiceManager;
import org.eclipse.tcf.te.runtime.statushandler.StatusHandlerManager;
import org.eclipse.tcf.te.runtime.statushandler.interfaces.IStatusHandler;
import org.eclipse.tcf.te.runtime.statushandler.interfaces.IStatusHandlerConstants;
import org.eclipse.tcf.te.tcf.core.peers.Peer;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProperties;
import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelRefreshService;
import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelUpdateService;
import org.eclipse.tcf.te.tcf.locator.model.Model;
import org.eclipse.tcf.te.tcf.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.ui.help.IContextHelpIds;
import org.eclipse.tcf.te.tcf.ui.nls.Messages;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Reset peer redirection command handler.
 */
public class ResetRedirectHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Determine the peer selected in Target Explorer tree
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			// Redirect is supporting single selection only
			Object candidate = ((IStructuredSelection)selection).getFirstElement();
			if (candidate instanceof IPeerModel) {
				final IPeerModel peerModel = (IPeerModel)candidate;

				Protocol.invokeLater(new Runnable() {
					@Override
					public void run() {
						resetRedirect(peerModel);
					}
				});
			}
		}

		return null;
	}

	/**
	 * Reset the communication redirection for the given peer.
	 * <p>
	 * The method must be called from within the TCF dispatch thread.
	 *
	 * @param peerModel The peer to reset. Must not be <code>null</code>.
	 */
	public void resetRedirect(IPeerModel peerModel) {
		Assert.isNotNull(peerModel);
		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$

		// Get the peer attributes
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.putAll(peerModel.getPeer().getAttributes());
		// Redirection set?
		if (attributes.get(IPeerModelProperties.PROP_REDIRECT_PROXY) != null) {
			// Remove the redirection
			attributes.remove(IPeerModelProperties.PROP_REDIRECT_PROXY);

			try {
				// Save it
				IURIPersistenceService uRIPersistenceService = ServiceManager.getInstance().getService(IURIPersistenceService.class);
				if (uRIPersistenceService == null) {
					throw new IOException("Persistence service instance unavailable."); //$NON-NLS-1$
				}
				// Create a peer
				IPeer peer = new Peer(attributes);
				uRIPersistenceService.write(peer, null);

				// And update the instance
				peerModel.setProperty(IPeerModelProperties.PROP_INSTANCE, peer);

				// Reset proxy (parent) and peer model (child) association
				Model.getModel().getService(ILocatorModelUpdateService.class).removeChild(peerModel);
				peerModel.setParent(null);

				// Trigger a refresh of the locator model in a later dispatch cycle
				Protocol.invokeLater(new Runnable() {
					@Override
					public void run() {
						Model.getModel().getService(ILocatorModelRefreshService.class).refresh();
					}
				});
			} catch (IOException e) {
				// Create the status
				IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
								Messages.ResetRedirectHandler_error_resetRedirectFailed, e);

				// Fill in the status handler custom data
				IPropertiesContainer data = new PropertiesContainer();
				data.setProperty(IStatusHandlerConstants.PROPERTY_TITLE, Messages.ResetRedirectHandler_error_title);
				data.setProperty(IStatusHandlerConstants.PROPERTY_CONTEXT_HELP_ID, IContextHelpIds.MESSAGE_RESET_REDIRECT_FAILED);
				data.setProperty(IStatusHandlerConstants.PROPERTY_CALLER, this);

				// Get the status handler
				IStatusHandler[] handler = StatusHandlerManager.getInstance().getHandler(peerModel);
				if (handler.length > 0) {
					handler[0].handleStatus(status, data, null);
				}
			}
		}
	}
}
