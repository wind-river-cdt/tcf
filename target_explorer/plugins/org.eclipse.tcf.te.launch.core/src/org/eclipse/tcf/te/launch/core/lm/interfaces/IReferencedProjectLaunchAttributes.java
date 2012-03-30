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
 * Defines the launch configuration attribute id's to access the launch step contexts.
 */
public interface IReferencedProjectLaunchAttributes {

	/**
	 * Launch configuration attribute: The referenced project items the launch is operating with. Use
	 * 								   {@link ReferencedProjectsPersistanceDelegate} to access
	 * 								   this attribute within a launch configuration.
	 */
	public static final String ATTR_REFERENCED_PROJECTS = ICommonLaunchAttributes.ATTR_PREFIX + ".referencedProjects";     //$NON-NLS-1$
}
