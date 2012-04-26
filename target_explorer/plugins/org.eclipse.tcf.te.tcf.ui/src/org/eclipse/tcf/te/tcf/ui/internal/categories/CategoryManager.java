/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.internal.categories;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.core.TransientPeer;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IURIPersistenceService;
import org.eclipse.tcf.te.runtime.services.ServiceManager;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelRefreshService;
import org.eclipse.tcf.te.tcf.locator.model.Model;
import org.eclipse.tcf.te.tcf.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.ui.nls.Messages;


/**
 * Category manager implementation.
 */
public class CategoryManager {
	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstance {
		public static CategoryManager instance = new CategoryManager();
	}

	/**
	 * Constructor.
	 */
	/* default */ CategoryManager() {
		super();
	}

	/**
	 * Returns the singleton instance.
	 */
	public static CategoryManager getInstance() {
		return LazyInstance.instance;
	}

	/**
	 * Adds the given peer node to the "My Targets" list by creating
	 * a static copy of the given peer.
	 *
	 * @param node The peer node. Must be not <code>null</code>.
	 */
	public void addToMyTargets(final IPeerModel node) {
		Assert.isNotNull(node);

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				// If the peer node is a static node already, there is nothing to do here
				String value = node.getPeer().getAttributes().get("static.transient"); //$NON-NLS-1$
				boolean isStatic = value != null && Boolean.parseBoolean(value.trim());
				if (isStatic) {
					return;
				}

				// Not a static node yet, copy the peer attributes
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
					IURIPersistenceService uRIPersistenceService = ServiceManager.getInstance().getService(IURIPersistenceService.class);
					if (uRIPersistenceService == null) {
						throw new IOException("Persistence service instance unavailable."); //$NON-NLS-1$
					}
					uRIPersistenceService.write(new TransientPeer(attrs), null);

					Model.getModel().getService(ILocatorModelRefreshService.class).refresh();
				} catch (IOException e) {
					IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
									NLS.bind(Messages.CategoryManager_dnd_failed, e.getLocalizedMessage()), e);
					UIPlugin.getDefault().getLog().log(status);
				}
			}
		};

		if (Protocol.isDispatchThread()) {
			runnable.run();
		}
		else {
			Protocol.invokeAndWait(runnable);
		}
	}
}
