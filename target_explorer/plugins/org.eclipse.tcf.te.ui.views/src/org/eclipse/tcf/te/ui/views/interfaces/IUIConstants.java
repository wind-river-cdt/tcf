/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.interfaces;

/**
 * UI constants.
 */
public interface IUIConstants {

	/**
	 * The main view id.
	 */
	public static final String ID_EXPLORER = "org.eclipse.tcf.te.ui.views.View"; //$NON-NLS-1$

	/**
	 * The properties editor id.
	 */
	public static final String ID_EDITOR = "org.eclipse.tcf.te.ui.views.Editor"; //$NON-NLS-1$

	/**
	 * The tabbed properties contributor id.
	 */
	public static final String TABBED_PROPERTIES_CONTRIBUTOR_ID = "org.eclipse.tcf.te.ui";  //$NON-NLS-1$

	// ***** Define the constants for the main view root mode *****

	/**
	 * Root nodes are working sets.
	 */
	public static final int MODE_WORKING_SETS = 0;

	/**
	 * Root nodes are whatever is contributed to the view.
	 */
	public static final int MODE_NORMAL = 1;

	// ***** Define the constants for the default main view categories *****

	/**
	 * Category: Favorites
	 */
	public static final String ID_CAT_FAVORITES = "org.eclipse.tcf.te.ui.views.category.favorites"; //$NON-NLS-1$

	/**
	 * Category: MyTargets
	 */
	public static final String ID_CAT_MY_TARGETS = "org.eclipse.tcf.te.ui.views.category.mytargets"; //$NON-NLS-1$

	/**
	 * Category: Neighborhood
	 */
	public static final String ID_CAT_NEIGHBORHOOD = "org.eclipse.tcf.te.ui.views.category.neighborhood"; //$NON-NLS-1$
}
