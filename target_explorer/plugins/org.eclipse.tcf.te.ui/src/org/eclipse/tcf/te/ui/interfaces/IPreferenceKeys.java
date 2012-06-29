/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.interfaces;

/**
 * The bundle's preference key identifiers.
 */
public interface IPreferenceKeys {
	/**
	 * Common prefix for all ui preference keys
	 */
	public final String PREFIX = "te.ui."; //$NON-NLS-1$

	// The preference key to access the option that if the search is a DFS.
	public static final String PREF_DEPTH_FIRST_SEARCH = "PrefDFS"; //$NON-NLS-1$
	// The default value of the option that if expanded nodes should be persisted
	public static final boolean DEFAULT_DEPTH_FIRST_SEARCH = false;
}
