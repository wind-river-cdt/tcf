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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceService;
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
	// Internal list of peer id's belonging to the "Favorites" category
	private final List<String> favorites = new ArrayList<String>();

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

		// Restore the favorites list
		IDialogSettings settings = getSettings();
		if (settings != null) {
			String[] candidates = settings.getArray("cat.favorites"); //$NON-NLS-1$
			if (candidates != null && candidates.length > 0) {
				favorites.addAll(Arrays.asList(candidates));
			}
		}
	}

	/**
	 * Returns the singleton instance.
	 */
	public static CategoryManager getInstance() {
		return LazyInstance.instance;
	}

	/**
	 * Returns the dialog settings.
	 *
	 * @return The dialog settings or <code>null</code>.
	 */
	protected IDialogSettings getSettings() {
		IDialogSettings settings = UIPlugin.getDefault().getDialogSettings();
		IDialogSettings section = null;
		if (settings != null) {
			section = settings.getSection(getClass().getName());
			if (section == null) {
				section = settings.addNewSection(getClass().getName());
			}
		}
		return section;
	}

	/**
	 * Adds the given peer id to the "Favorites" list.
	 *
	 * @param id The peer id. Must not be <code>null</code>.
	 */
	public void addToFavorites(String id) {
		Assert.isNotNull(id);

		if (!favorites.contains(id)) {
			favorites.add(id);

			// Persist the new favorites list immediately
			IDialogSettings settings = getSettings();
			if (settings != null) {
				settings.put("cat.favorites", favorites.toArray(new String[favorites.size()])); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Removes the given peer id from the "Favorites" list.
	 *
	 * @param id The peer id. Must not be <code>null</code>.
	 */
	public void removeFromFavorites(String id) {
		Assert.isNotNull(id);

		if (favorites.contains(id)) {
			favorites.remove(id);

			// Persist the new favorites list immediately
			IDialogSettings settings = getSettings();
			if (settings != null) {
				settings.put("cat.favorites", favorites.toArray(new String[favorites.size()])); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Returns if or if not the given peer id is a favorite.
	 *
	 * @param id The peer id. Must not be <code>null</code>.
	 * @return <code>True</code> if the peer id is a favorite, <code>false</code> otherwise.
	 */
	public boolean isFavorite(String id) {
		Assert.isNotNull(id);
		return favorites.contains(id);
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
				if (isStatic) return;

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
					IPersistenceService persistenceService = ServiceManager.getInstance().getService(IPersistenceService.class);
					if (persistenceService == null) throw new IOException("Persistence service instance unavailable."); //$NON-NLS-1$
					persistenceService.write(attrs);

					Model.getModel().getService(ILocatorModelRefreshService.class).refresh();
				} catch (IOException e) {
					IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
												NLS.bind(Messages.CategoryManager_dnd_failed, e.getLocalizedMessage()), e);
					UIPlugin.getDefault().getLog().log(status);
				}
			}
		};

		if (Protocol.isDispatchThread()) runnable.run();
		else Protocol.invokeAndWait(runnable);
	}
}
