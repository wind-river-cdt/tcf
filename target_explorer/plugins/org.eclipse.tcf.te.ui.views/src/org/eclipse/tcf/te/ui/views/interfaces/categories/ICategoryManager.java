/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.interfaces.categories;

/**
 * Interface to be implemented by category managers.
 */
public interface ICategoryManager {

	/**
	 * Flush the current category manager state.
	 */
	public void flush();

	/**
	 * Adds the given id to the given category.
	 *
	 * @param categoryId The category id. Must not be <code>null</code>.
	 * @param id The id. Must not be <code>null</code>.
	 *
	 * @return <code>True</code> if the id has been added to the category, <code>false</code> if not.
	 */
	public boolean add(String categoryId, String id);

	/**
	 * Removes the given id from the given category.
	 *
	 * @param categoryId The category id. Must not be <code>null</code>.
	 * @param id The id. Must not be <code>null</code>.
	 *
	 * @return <code>True</code> if the id has been removed from the category, <code>false</code> if not.
	 */
	public boolean remove(String categoryId, String id);

	/**
	 * Returns if or if not the given id belongs to the given category.
	 *
	 * @param categoryId The category id. Must not be <code>null</code>.
	 * @param id The id. Must not be <code>null</code>.
	 *
	 * @return <code>True</code> if the id belongs to the category, <code>false</code> if not.
	 */
	public boolean belongsTo(String categoryId, String id);

	/**
	 * Returns the list of categories the given id belongs to.
	 *
	 * @param id The id. Must not be <code>null</code>.
	 * @return The list of category id's the id belongs to, or an empty list.
	 */
	public String[] getCategoryIds(String id);

}