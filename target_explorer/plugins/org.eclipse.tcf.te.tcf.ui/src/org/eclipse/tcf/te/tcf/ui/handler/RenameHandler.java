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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.callback.Callback;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistableNodeProperties;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IURIPersistenceService;
import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
import org.eclipse.tcf.te.runtime.services.ServiceManager;
import org.eclipse.tcf.te.runtime.statushandler.StatusHandlerManager;
import org.eclipse.tcf.te.runtime.statushandler.interfaces.IStatusHandler;
import org.eclipse.tcf.te.runtime.statushandler.interfaces.IStatusHandlerConstants;
import org.eclipse.tcf.te.tcf.core.peers.Peer;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProperties;
import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelRefreshService;
import org.eclipse.tcf.te.tcf.locator.model.Model;
import org.eclipse.tcf.te.tcf.locator.nodes.PeerRedirector;
import org.eclipse.tcf.te.tcf.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.ui.help.IContextHelpIds;
import org.eclipse.tcf.te.tcf.ui.nls.Messages;
import org.eclipse.tcf.te.ui.dialogs.RenameDialog;
import org.eclipse.tcf.te.ui.views.ViewsUtil;
import org.eclipse.tcf.te.ui.views.interfaces.IUIConstants;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Rename handler implementation.
 */
public class RenameHandler extends AbstractHandler {
	// Remember the shell from the execution event
	private Shell shell = null;

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get the shell
		shell = HandlerUtil.getActiveShell(event);
		// Get the current selection
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		// Delete the selection
		if (selection != null) {
			rename(selection, new Callback() {
				@Override
				protected void internalDone(Object caller, IStatus status) {
					// Refresh the view
					ViewsUtil.refresh(IUIConstants.ID_EXPLORER);
				}
			});
		}
		// Reset the shell
		shell = null;

		return null;
    }

	/**
	 * Renames all elements from the given selection and invokes the
	 * given callback once done.
	 *
	 * @param selection The selection. Must not be <code>null</code>.
	 * @param callback The callback. Must not be <code>null</code>.
	 */
	public void rename(final ISelection selection, final ICallback callback) {
		Assert.isNotNull(selection);
		Assert.isNotNull(callback);

		// The callback needs to be invoked in any case. However, if called
		// from an asynchronous callback, set this flag to false.
		boolean invokeCallback = true;

		// The selection must be a structured selection and must not be empty
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			Iterator<?> iterator = ((IStructuredSelection)selection).iterator();
			while (iterator.hasNext()) {
				Object element = iterator.next();
				Assert.isTrue(element instanceof IPeerModel);
				final IPeerModel node = (IPeerModel)element;

				RenameDialog dialog = createRenameDialog(shell, node);
				int ok = dialog.open();
				if (ok == Window.OK) {
					// Do the renaming.
					final String newName = dialog.getNewName();

					Runnable runnable = new Runnable() {
						@Override
						public void run() {
							if (newName != null && !"".equals(newName) && !newName.equals(node.getPeer().getName())) { //$NON-NLS-1$
								try {
									// Get the persistence service
									IURIPersistenceService uRIPersistenceService = ServiceManager.getInstance().getService(IURIPersistenceService.class);
									if (uRIPersistenceService == null) {
										throw new IOException("Persistence service instance unavailable."); //$NON-NLS-1$
									}

									// To update the peer attributes, the peer needs to be recreated
									IPeer oldPeer = node.getPeer();
									// Create a write able copy of the peer attributes
									Map<String, String> attributes = new HashMap<String, String>(oldPeer.getAttributes());
									// Update the name
									attributes.put(IPeer.ATTR_NAME, newName);
									// Remove the persistence storage URI (if set)
									attributes.remove(IPersistableNodeProperties.PROPERTY_URI);
									// Create the new peer
									IPeer newPeer = oldPeer instanceof PeerRedirector ? new PeerRedirector(((PeerRedirector)oldPeer).getParent(), attributes) : new Peer(attributes);
									// Update the peer node instance (silently)
									boolean changed = node.setChangeEventsEnabled(false);
									node.setProperty(IPeerModelProperties.PROP_INSTANCE, newPeer);
									if (changed) {
										node.setChangeEventsEnabled(true);
									}

									// Remove the old persisted peer
									uRIPersistenceService.delete(oldPeer, null);
									// Save the peer node to the new persistence storage
									uRIPersistenceService.write(newPeer, null);

									Protocol.invokeLater(new Runnable() {
										@Override
										public void run() {
											// Trigger a change event for the node
											node.fireChangeEvent("properties", null, node.getProperties()); //$NON-NLS-1$
										}
									});

								} catch (IOException e) {
									// Create the status
									IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
																Messages.RenameHandler_error_renameFailed, e);

									// Fill in the status handler custom data
									IPropertiesContainer data = new PropertiesContainer();
									data.setProperty(IStatusHandlerConstants.PROPERTY_TITLE, Messages.RenameHandler_error_title);
									data.setProperty(IStatusHandlerConstants.PROPERTY_CONTEXT_HELP_ID, IContextHelpIds.MESSAGE_RENAME_FAILED);
									data.setProperty(IStatusHandlerConstants.PROPERTY_CALLER, this);

									// Get the status handler
									IStatusHandler[] handler = StatusHandlerManager.getInstance().getHandler(selection);
									if (handler.length > 0) {
										handler[0].handleStatus(status, data, null);
									}
								}
							}
						}
					};

					if (Protocol.isDispatchThread()) runnable.run();
					else Protocol.invokeAndWait(runnable);

					// Get the locator model
					final ILocatorModel model = Model.getModel();
					if (model != null) {
						// Trigger a refresh of the model
						final ILocatorModelRefreshService service = model.getService(ILocatorModelRefreshService.class);
						if (service != null) {
							// Invoke the callback after the refresh
							invokeCallback = false;
							Protocol.invokeLater(new Runnable() {
								@Override
								public void run() {
									// Refresh the model now (must be executed within the TCF dispatch thread)
									service.refresh();
									// Invoke the callback
									callback.done(RenameHandler.this, Status.OK_STATUS);
								}
							});
						}
					}
				}
			}
		}

		if (invokeCallback) {
			callback.done(this, Status.OK_STATUS);
		}
	}

	/**
	 * Create a renaming dialog for the specified peer model node.
	 *
	 * @param shell The parent shell or <code>null</code>
	 * @param node The peer model. Must not be <code>null</code>.
	 *
	 * @return The renaming dialog.
	 */
	private RenameDialog createRenameDialog(final Shell shell, final IPeerModel node) {
		Assert.isNotNull(node);

		final AtomicReference<String> name = new AtomicReference<String>();
		final List<String> usedNames = new ArrayList<String>();

		Runnable runnable = new Runnable() {
			@Override
            public void run() {
				name.set(node.getPeer().getName());

				ILocatorModel model = Model.getModel();
				IPeerModel[] peers = model.getPeers();
				for (IPeerModel peer : peers) {
					if (peer.equals(node)) continue;
					if (!usedNames.contains(peer.getPeer().getName())) {
						usedNames.add(peer.getPeer().getName());
					}
				}
			}
		};

		if (Protocol.isDispatchThread()) runnable.run();
		else Protocol.invokeAndWait(runnable);

		String title = NLS.bind(Messages.RenameHandler_dialog_title, name.get());
		String prompt = Messages.RenameHandler_dialog_message;
		String usedError = Messages.RenameHandler_dialog_error_nameExist;
		String label = Messages.RenameHandler_dialog_promptNewName;

		return new RenameDialog(shell, null, title, prompt, usedError, null, label, name.get(), null, usedNames.toArray(new String[usedNames.size()]), null);
	}

}
