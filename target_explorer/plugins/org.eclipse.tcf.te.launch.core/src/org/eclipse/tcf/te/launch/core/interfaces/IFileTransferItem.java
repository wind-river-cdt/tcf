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
public interface IFileTransferItem extends IPropertiesContainer {

	public static final String PROPERTY_ENABLED = "enabled"; //$NON-NLS-1$
	public static final String PROPERTY_DIRECTION = "direction"; //$NON-NLS-1$
	public static final String PROPERTY_HOST = "host"; //$NON-NLS-1$
	public static final String PROPERTY_TARGET = "target"; //$NON-NLS-1$
	public static final String PROPERTY_OPTIONS = "options"; //$NON-NLS-1$

	public static final int HOST_TO_TARGET = 1;
	public static final int TARGET_TO_HOST = 2;
}
