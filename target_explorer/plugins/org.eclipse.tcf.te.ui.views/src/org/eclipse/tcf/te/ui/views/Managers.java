/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views;

import org.eclipse.tcf.te.ui.views.interfaces.categories.ICategoryManager;
import org.eclipse.tcf.te.ui.views.internal.categories.CategoryManager;

/**
 * Provides access to singleton manager instances.
 */
public class Managers {
	// The category manager instance
	private static volatile ICategoryManager categoryManager;

	/**
	 * Dispose the manager instances.
	 */
	public static void dispose() {
		if (categoryManager != null) { categoryManager.flush(); categoryManager = null; }
	}

	/**
	 * Returns the category manager instance.
	 *
	 * @return The category manager instance.
	 */
	public static ICategoryManager getCategoryManager() {
		if (categoryManager == null) {
			categoryManager = new CategoryManager();
		}
		return categoryManager;
	}
}
