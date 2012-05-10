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
public interface ICommonTCFLaunchAttributes {

	/**
	 * Define the prefix used by all other attribute id's as prefix.
	 */
	public static final String ATTR_PREFIX = "org.eclipse.tcf.te.tcf.launch"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute: The TCF channel.
	 */
	public static final String ATTR_CHANNEL = ICommonLaunchAttributes.ATTR_PREFIX + ".channel"; //$NON-NLS-1$
}
