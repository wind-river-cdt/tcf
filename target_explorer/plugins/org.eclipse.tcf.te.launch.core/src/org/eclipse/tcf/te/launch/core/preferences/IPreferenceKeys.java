/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.preferences;


/**
 * The locator model bundle preference key identifiers.
 */
public interface IPreferenceKeys {
	/**
	 * Common prefix for all core preference keys
	 */
	public final String PREFIX = "te.launch.core."; //$NON-NLS-1$

	/**
	 * Launch configuration find/create mode: Always create new launch configurations.
	 */
	public static final int MODE_ALWAYS_NEW = 0;

	/**
	 * Launch configuration find/create mode: Re-use launch configuration if all attributes are matching.
	 */
	public static final int MODE_FULL_MATCH_LAUNCH_CONFIG = 1;

	/**
	 * Launch configuration find/create mode: Re-use launch configuration if the target ID matches.
	 */
	public static final int MODE_FULL_MATCH_TARGET = 2;

	/**
	 * Launch configuration find/create mode: Re-use first matching launch configuration.
	 */
	public static final int MODE_FIRST_MATCHING = 3;

	/**
	 * Launch configuration find/create mode. See the <code>MODE_*</code> constants.
	 */
	public static final String PREF_LAUNCH_CONFIG_FIND_CREATE_MODE = PREFIX + "launchConfigFindCreateMode"; //$NON-NLS-1$

	/**
	 * If set to <code>true</code>, project references will be added by default to newly created launch configurations.
	 */
	public static final String PREF_ADD_PROJECT_TO_NEW_LAUNCH_CONFIG = PREFIX + "addProjectToNewLaunchConfig"; //$NON-NLS-1$

	/**
	 * If set to <code>true</code>, launches are sorted by their last launched time stamp.
	 */
	public static final String PREF_SORT_LAUNCHES_BY_LAST_LAUNCHED = PREFIX + ".sortLaunchesByLastLaunched"; //$NON-NLS-1$
}
