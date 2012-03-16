/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.launch.core.interfaces;

import org.eclipse.tcf.te.launch.core.lm.interfaces.ICommonLaunchAttributes;

/**
 * Defines the launch configuration attribute id's to access the launch step contexts.
 */
public interface ILinuxAppLaunchAttributes {

	/**
	 * Define the prefix used by all other attribute id's as prefix.
	 */
	public static final String ATTR_PREFIX = "org.eclipse.tcf.te.tcf.launch"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute: The process image.
	 */
	public static final String ATTR_PROCESS_IMAGE = ICommonLaunchAttributes.ATTR_PREFIX + ".process_image"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute: The process arguments.
	 */
	public static final String ATTR_PROCESS_ARGUMENTS = ICommonLaunchAttributes.ATTR_PREFIX + ".process_arguments"; //$NON-NLS-1$
}
