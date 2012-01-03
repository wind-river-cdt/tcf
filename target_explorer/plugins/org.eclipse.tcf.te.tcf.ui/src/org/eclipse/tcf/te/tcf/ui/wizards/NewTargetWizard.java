/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.wizards;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceService;
import org.eclipse.tcf.te.runtime.services.ServiceManager;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService;
import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelRefreshService;
import org.eclipse.tcf.te.tcf.locator.model.Model;
import org.eclipse.tcf.te.tcf.ui.nls.Messages;
import org.eclipse.tcf.te.ui.views.ViewsUtil;
import org.eclipse.tcf.te.ui.views.interfaces.IUIConstants;
import org.eclipse.tcf.te.ui.wizards.AbstractWizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * New peer wizard implementation.
 */
public class NewTargetWizard extends AbstractWizard implements INewWizard {
	// Session wide new peer counter
	private final static AtomicInteger counter = new AtomicInteger();

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// Set the window title
		setWindowTitle(Messages.NewTargetWizard_windowTitle);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		// Create the minimum set of peer attributes to create a new peer
		final Map<String, String> peerAttributes = new HashMap<String, String>();
		peerAttributes.put(IPeer.ATTR_ID, UUID.randomUUID().toString());
		peerAttributes.put(IPeer.ATTR_NAME, NLS.bind(Messages.NewTargetWizard_newPeer_name, Integer.valueOf(counter.incrementAndGet())));

		try {
			// Save the new peer
			IPersistenceService persistenceService = ServiceManager.getInstance().getService(IPersistenceService.class);
			if (persistenceService == null) throw new IOException("Persistence service instance unavailable."); //$NON-NLS-1$
			persistenceService.write(peerAttributes);

			// Get the locator model
			final ILocatorModel model = Model.getModel();
			if (model != null) {
				// Trigger a refresh of the model to read in the newly created static peer
				final ILocatorModelRefreshService service = model.getService(ILocatorModelRefreshService.class);
				if (service != null) {
					Protocol.invokeLater(new Runnable() {
						@Override
						public void run() {
							// Refresh the model now (must be executed within the TCF dispatch thread)
							service.refresh();

							// Get the peer model node from the model and select it in the tree
							final IPeerModel peerNode = model.getService(ILocatorModelLookupService.class).lkupPeerModelById(peerAttributes.get(IPeer.ATTR_ID));
							if (peerNode != null) {
								// Refresh the viewer
								ViewsUtil.refresh(IUIConstants.ID_EXPLORER);
								// Create the selection
								ISelection selection = new StructuredSelection(peerNode);
								// Set the selection
								ViewsUtil.setSelection(IUIConstants.ID_EXPLORER, selection);
								// And open the properties on the selection
								ViewsUtil.openProperties(selection);
							}
						}
					});
				}
			}
		} catch (IOException e) {
			if (getContainer().getCurrentPage() instanceof WizardPage) {
				String message = NLS.bind(Messages.NewTargetWizard_error_savePeer, e.getLocalizedMessage());
				((WizardPage)getContainer().getCurrentPage()).setMessage(message, IMessageProvider.ERROR);
				getContainer().updateMessage();
			}
			return false;
		}

		return true;
	}

}
