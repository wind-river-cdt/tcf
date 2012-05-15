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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.tcf.core.TransientPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IURIPersistenceService;
import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
import org.eclipse.tcf.te.runtime.services.ServiceManager;
import org.eclipse.tcf.te.runtime.statushandler.StatusHandlerManager;
import org.eclipse.tcf.te.runtime.statushandler.interfaces.IStatusHandler;
import org.eclipse.tcf.te.runtime.statushandler.interfaces.IStatusHandlerConstants;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProperties;
import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelRefreshService;
import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelUpdateService;
import org.eclipse.tcf.te.tcf.locator.model.Model;
import org.eclipse.tcf.te.tcf.locator.nodes.PeerRedirector;
import org.eclipse.tcf.te.tcf.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.ui.dialogs.RedirectAgentSelectionDialog;
import org.eclipse.tcf.te.tcf.ui.help.IContextHelpIds;
import org.eclipse.tcf.te.tcf.ui.nls.Messages;
import org.eclipse.tcf.te.ui.swt.DisplayUtil;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.navigator.CommonNavigator;

/**
 * Redirect peer command handler implementation.
 */
public class RedirectHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		// Determine the peer selected in Target Explorer tree
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			// Redirect is supporting single selection only
			Object candidate = ((IStructuredSelection)selection).getFirstElement();
			if (candidate instanceof IPeerModel) {
				final IPeerModel peerModel = (IPeerModel)candidate;

				// Create the agent selection dialog
				RedirectAgentSelectionDialog dialog = new RedirectAgentSelectionDialog(HandlerUtil.getActiveShell(event), null) {
					@Override
					protected void configureTableViewer(TableViewer viewer) {
						Assert.isNotNull(viewer);

						List<ViewerFilter> filter = new ArrayList<ViewerFilter>();
						if (viewer.getFilters() != null && viewer.getFilters().length > 0) {
							filter.addAll(Arrays.asList(viewer.getFilters()));
						}

						filter.add(new ViewerFilter() {
							@Override
							public boolean select(Viewer viewer, Object parentElement, Object element) {
								if (peerModel.equals(element)) {
									return false;
								}
								return true;
							}
						});

						viewer.setFilters(filter.toArray(new ViewerFilter[filter.size()]));
					}
				};

				// Open the dialog
				if (dialog.open() == Window.OK) {
					// Get the selected proxy from the dialog
					selection = dialog.getSelection();
					if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
						candidate = ((IStructuredSelection)selection).getFirstElement();
						if (candidate instanceof IPeerModel) {
							final IPeerModel proxy = (IPeerModel)candidate;

							Protocol.invokeLater(new Runnable() {
								@Override
								public void run() {
									redirect(peerModel, proxy);

									DisplayUtil.safeAsyncExec(new Runnable() {
										@Override
										public void run() {
											IWorkbenchPart part = HandlerUtil.getActivePart(event);
											if (part instanceof CommonNavigator) {
												CommonNavigator navigator = (CommonNavigator)part;
												navigator.selectReveal(new StructuredSelection(peerModel));
											}
										}
									});
								}
							});
						}
					}
				}
			}
		}


		return null;
	}

	/**
	 * Redirect the communication to the given peer through the given proxy.
	 * <p>
	 * The method must be called from within the TCF dispatch thread.
	 *
	 * @param peerModel The peer to redirect. Must not be <code>null</code>.
	 * @param proxy The proxy. Must not be <code>null</code>
	 */
	public void redirect(IPeerModel peerModel, IPeerModel proxy) {
		Assert.isNotNull(peerModel);
		Assert.isNotNull(proxy);
		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$

		// Get the peer attributes
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.putAll(peerModel.getPeer().getAttributes());
		// Set the redirection
		attributes.put(IPeerModelProperties.PROP_REDIRECT_PROXY, proxy.getPeerId());

		try {
			IURIPersistenceService uRIPersistenceService = ServiceManager.getInstance().getService(IURIPersistenceService.class);
			if (uRIPersistenceService == null) {
				throw new IOException("Persistence service instance unavailable."); //$NON-NLS-1$
			}
			uRIPersistenceService.write(new TransientPeer(attributes), null);

			// Create a peer redirector
			PeerRedirector redirector = new PeerRedirector(proxy.getPeer(), attributes);
			// And update the instance
			peerModel.setProperty(IPeerModelProperties.PROP_INSTANCE, redirector);

			// Associate proxy (parent) and peer model (child)
			peerModel.setParent(proxy);
			Model.getModel().getService(ILocatorModelUpdateService.class).addChild(peerModel);

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
							Messages.RedirectHandler_error_redirectFailed, e);

			// Fill in the status handler custom data
			IPropertiesContainer data = new PropertiesContainer();
			data.setProperty(IStatusHandlerConstants.PROPERTY_TITLE, Messages.RedirectHandler_error_title);
			data.setProperty(IStatusHandlerConstants.PROPERTY_CONTEXT_HELP_ID, IContextHelpIds.MESSAGE_REDIRECT_FAILED);
			data.setProperty(IStatusHandlerConstants.PROPERTY_CALLER, this);

			// Get the status handler
			IStatusHandler[] handler = StatusHandlerManager.getInstance().getHandler(peerModel);
			if (handler.length > 0) {
				handler[0].handleStatus(status, data, null);
			}
		}
	}
}
