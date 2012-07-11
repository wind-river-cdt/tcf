/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
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
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.callback.Callback;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IURIPersistenceService;
import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
import org.eclipse.tcf.te.runtime.services.ServiceManager;
import org.eclipse.tcf.te.runtime.statushandler.StatusHandlerManager;
import org.eclipse.tcf.te.runtime.statushandler.interfaces.IStatusHandler;
import org.eclipse.tcf.te.runtime.statushandler.interfaces.IStatusHandlerConstants;
import org.eclipse.tcf.te.tcf.core.peers.Peer;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelRefreshService;
import org.eclipse.tcf.te.tcf.locator.model.Model;
import org.eclipse.tcf.te.tcf.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.ui.help.IContextHelpIds;
import org.eclipse.tcf.te.tcf.ui.nls.Messages;
import org.eclipse.tcf.te.ui.views.Managers;
import org.eclipse.tcf.te.ui.views.interfaces.IUIConstants;
import org.eclipse.tcf.te.ui.views.interfaces.categories.ICategorizable;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * "Always available offline" command handler implementation.
 */
public class OfflineCommandHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		// Get the current selection
		final ISelection selection = HandlerUtil.getCurrentSelection(event);
		// Make the selection available offline
		if (selection != null) makeAvailableOffline(selection, new Callback() {
			@Override
			protected void internalDone(Object caller, IStatus status) {
				if (status.getSeverity() == IStatus.ERROR) {
					// Fill in the status handler custom data
					IPropertiesContainer data = new PropertiesContainer();
					data.setProperty(IStatusHandlerConstants.PROPERTY_TITLE, Messages.OfflineCommandHandler_error_title);
					data.setProperty(IStatusHandlerConstants.PROPERTY_CONTEXT_HELP_ID, IContextHelpIds.MESSAGE_MAKEOFFLINE_FAILED);
					data.setProperty(IStatusHandlerConstants.PROPERTY_CALLER, this);

					// Get the status handler
					IStatusHandler[] handler = StatusHandlerManager.getInstance().getHandler(selection);
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
			}
		});

		return null;
	}

	/**
	 * Tests if the elements of the given selection can be made
	 * available offline.
	 *
	 * @param selection The selection. Must not be <code>null</code>.
	 * @return <code>True</code> if the elements can be made available offline, <code>false</code> otherwise.
	 */
	public boolean canMakeAvailableOffline(ISelection selection) {
		Assert.isNotNull(selection);

		boolean enabled = false;

		// The selection must be a structured selection and must not be empty
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			// Assume the selection to be deletable
			enabled = true;
			// Iterate the selection. All elements must be of type IPeerModel
			Iterator<?> iterator = ((IStructuredSelection)selection).iterator();
			while (iterator.hasNext()) {
				Object element = iterator.next();
				if (!(element instanceof IPeerModel)) {
					enabled = false;
					break;
				}

				// Determine if the selected peer model is static
				boolean isStatic = isStatic((IPeerModel)element);
				if (isStatic) enabled = false;

				if (!enabled) break;
			}
		}

		return enabled;
	}

	/**
	 * Determines if the given peer model node is a static node.
	 *
	 * @param node The peer model node. Must not be <code>null</code>.
	 * @return <code>True</code> if the node is static, <code>false</code> otherwise.
	 */
	private boolean isStatic(final IPeerModel node) {
		Assert.isNotNull(node);

		final AtomicBoolean isStatic = new AtomicBoolean();

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				String value = node.getPeer().getAttributes().get("static.transient"); //$NON-NLS-1$
				isStatic.set(value != null && Boolean.parseBoolean(value.trim()));
			}
		};

		if (Protocol.isDispatchThread()) runnable.run();
		else Protocol.invokeAndWait(runnable);

		return isStatic.get();
	}

	/**
	 * Creates an offline copy of the peer attributes.
	 *
	 * @param selection The selection. Must not be <code>null</code>.
	 * @param callback The callback. Must not be <code>null</code>.
	 */
	public void makeAvailableOffline(final ISelection selection, final ICallback callback) {
		Assert.isNotNull(selection);
		Assert.isNotNull(callback);

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				// The status to report back
				IStatus status = Status.OK_STATUS;
				// The selection must be a structured selection and must not be empty
				if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
					// Iterate the selection. All elements must be of type IPeerModel
					Iterator<?> iterator = ((IStructuredSelection)selection).iterator();
					while (iterator.hasNext()) {
						Object element = iterator.next();
						Assert.isTrue(element instanceof IPeerModel);
						IPeerModel node = (IPeerModel)element;

						// Copy the peer attributes
						Map<String, String> attrs = new HashMap<String, String>();
						attrs.putAll(node.getPeer().getAttributes());

						// Remove attributes filled in by the discovery
						attrs.remove(IPeer.ATTR_AGENT_ID);
						attrs.remove(IPeer.ATTR_SERVICE_MANGER_ID);
						attrs.remove("ServerManagerID"); //$NON-NLS-1$
						attrs.remove(IPeer.ATTR_USER_NAME);
						attrs.remove(IPeer.ATTR_OS_NAME);

						// Persist the attributes
						try {
							IURIPersistenceService service = ServiceManager.getInstance().getService(IURIPersistenceService.class);
							if (service == null) {
								throw new IOException("Persistence service instance unavailable."); //$NON-NLS-1$
							}
							service.write(new Peer(attrs), null);

							// Remove the node from the "Neighborhood" category
			        	    ICategorizable categorizable = (ICategorizable)node.getAdapter(ICategorizable.class);
			            	if (categorizable == null) categorizable = (ICategorizable)Platform.getAdapterManager().getAdapter(node, ICategorizable.class);
			            	Assert.isNotNull(categorizable);

							Managers.getCategoryManager().remove(IUIConstants.ID_CAT_NEIGHBORHOOD, categorizable.getId());
						} catch (IOException e) {
							status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
											   NLS.bind(Messages.OfflineCommandHandler_error_makeOffline_failed, node.getName(), e.getLocalizedMessage()), e);
						}

						if (status != null) break;
					}
				}

				// Invoke the callback
				callback.done(OfflineCommandHandler.this, status);
			}
		};

		Protocol.invokeLater(runnable);
	}
}
