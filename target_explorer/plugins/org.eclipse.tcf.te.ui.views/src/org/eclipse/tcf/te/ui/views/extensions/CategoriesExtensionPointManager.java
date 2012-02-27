/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.extensions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager;
import org.eclipse.tcf.te.runtime.extensions.ExecutableExtensionProxy;
import org.eclipse.tcf.te.ui.views.interfaces.ICategory;

/**
 * Categories extension point manager implementation.
 */
public class CategoriesExtensionPointManager extends AbstractExtensionPointManager<ICategory> {
	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstance {
		public static CategoriesExtensionPointManager instance = new CategoriesExtensionPointManager();
	}

	/**
	 * Constructor.
	 */
	/* default */ CategoriesExtensionPointManager() {
		super();
	}

	/**
	 * Returns the singleton instance of the extension point manager.
	 */
	public static CategoriesExtensionPointManager getInstance() {
		return LazyInstance.instance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager#getExtensionPointId()
	 */
	@Override
	protected String getExtensionPointId() {
		return "org.eclipse.tcf.te.ui.views.categories"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager#getConfigurationElementName()
	 */
	@Override
	protected String getConfigurationElementName() {
		return "category"; //$NON-NLS-1$
	}

	/**
	 * Returns the list of all contributed categories.
	 *
	 * @param unique If <code>true</code>, the method returns new instances for each
	 *               contributed category.
	 *
	 * @return The list of contributed categories, or an empty array.
	 */
	public ICategory[] getCategories(boolean unique) {
		List<ICategory> contributions = new ArrayList<ICategory>();
		Collection<ExecutableExtensionProxy<ICategory>> editorPages = getExtensions().values();
		for (ExecutableExtensionProxy<ICategory> editorPage : editorPages) {
			ICategory instance = unique ? editorPage.newInstance() : editorPage.getInstance();
			if (instance != null && !contributions.contains(instance)) {
				contributions.add(instance);
			}
		}

		return contributions.toArray(new ICategory[contributions.size()]);
	}

	/**
	 * Returns the category identified by its unique id. If no category
	 * with the specified id is registered, <code>null</code> is returned.
	 *
	 * @param id The unique id of the category or <code>null</code>
	 * @param unique If <code>true</code>, the method returns new instances of the category contribution.
	 *
	 * @return The category instance or <code>null</code>.
	 */
	public ICategory getCategory(String id, boolean unique) {
		ICategory contribution = null;
		if (getExtensions().containsKey(id)) {
			ExecutableExtensionProxy<ICategory> proxy = getExtensions().get(id);
			// Get the extension instance
			contribution = unique ? proxy.newInstance() : proxy.getInstance();
		}

		return contribution;
	}
}
