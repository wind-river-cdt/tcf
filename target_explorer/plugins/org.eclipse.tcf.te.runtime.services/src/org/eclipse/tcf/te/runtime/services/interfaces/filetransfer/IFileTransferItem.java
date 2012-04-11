/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.tcf.te.runtime.services.interfaces.filetransfer;

import org.eclipse.core.runtime.IPath;
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

	/**
	 * Return <code>true</code> if the item is enabled.
	 */
	public boolean isEnabled();

	/**
	 * Return the host path. Must not be <code>null</code>.
	 * The host path needs to be a file for transfer from host to target.
	 * For a transfer from target to host, the path can be a file or directory.
	 */
	public IPath getHostPath();

	/**
	 * Return the target (remote) path. Must not be <code>null</code>.
	 * The target path needs to be a file for transfer from target to host.
	 * For a transfer from host to target, the path can be a file or directory.
	 */
	public IPath getTargetPath();

	/**
	 * Return the transfer direction.
	 */
	public int getDirection();

	/**
	 * Return the option string for the transfer or <code>null</code>.
	 */
	public String getOptions();
}
