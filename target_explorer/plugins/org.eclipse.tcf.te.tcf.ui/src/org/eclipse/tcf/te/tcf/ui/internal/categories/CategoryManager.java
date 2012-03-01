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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.tcf.te.tcf.ui.activator.UIPlugin;


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
}
