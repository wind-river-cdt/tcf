/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.lm.interfaces;

/**
 * Defines the common attribute id's used to access launch configuration properties.
 */
public interface ICommonLaunchAttributes {

	/**
	 * Define the prefix used by all other attribute id's as prefix.
	 */
	public static final String ATTR_PREFIX = "org.eclipse.tcf.te.launch"; //$NON-NLS-1$

	/**
	 * Time stamp when last launched.
	 */
	public static final String ATTR_LAST_LAUNCHED = ATTR_PREFIX + ".lastLaunched";     //$NON-NLS-1$

	/**
	 * Attribute used exclusively with <code>ILaunch.setAttribute</code> to mark when
	 * then launch sequence finished. The attribute does not tell if an error occurred
	 * during the launch!
	 */
	public static final String ILAUNCH_ATTRIBUTE_LAUNCH_SEQUENCE_COMPLETED = "launchSequenceCompleted"; //$NON-NLS-1$

	// Copied from IDebugUIConstants to make them available for non-UI plugin's.

	/**
	 * Debug UI plug-in identifier (value <code>"org.eclipse.debug.ui"</code>).
	 */
	public static final String PLUGIN_ID_DEBUG_UI = "org.eclipse.debug.ui"; //$NON-NLS-1$;

	/**
	 * Debug CORE plug-in identifier (value <code>"org.eclipse.debug.core"</code>).
	 */
	public static final String PLUGIN_ID_DEBUG_CORE = "org.eclipse.debug.core"; //$NON-NLS-1$;

	/**
	 * Launch configuration boolean attribute specifying whether output from the launched process will
	 * be captured and written to the console. Default value is <code>true</code>.
	 *
	 * @since 3.4
	 */
	public static final String ATTR_CAPTURE_OUTPUT = PLUGIN_ID_DEBUG_CORE + ".capture_output"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute specifying a file name that console output should
	 * be written to or <code>null</code> if none. Default value is <code>null</code>.
	 * When specified, all output from the launched process will be written to the file.
	 * The file name attribute may contain variables which will be resolved by the
	 * {@link org.eclipse.core.variables.IStringVariableManager}.
	 *
	 * @see IDebugUIConstants
	 * @since 3.1
	 */
	public static final String ATTR_CAPTURE_IN_FILE = PLUGIN_ID_DEBUG_UI + ".ATTR_CAPTURE_IN_FILE"; //$NON-NLS-1$
}



