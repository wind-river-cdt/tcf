/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.tcf.te.launch.core.interfaces;

import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;

/**
 * IFileTransferItem
 */
public interface IReferencedProjectItem extends IPropertiesContainer {

	public static final String PROPERTY_ENABLED = "enabled"; //$NON-NLS-1$
	public static final String PROPERTY_PROJECT_NAME = "project_name"; //$NON-NLS-1$

	/**
	 * Return <code>true</code> if the item is enabled.
	 */
	public boolean isEnabled();

	/**
	 * Return the project name.
	 */
	public String getProjectName();
}
