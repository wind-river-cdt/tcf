/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.internal;

/**
 * Image registry constants.
 */
public interface ImageConsts {

	// ***** The directory structure constants *****

	/**
	 * The root directory where to load the images from, relative to
	 * the bundle directory.
	 */
    public final static String IMAGE_DIR_ROOT = "icons/"; //$NON-NLS-1$

    /**
     * The directory where to load colored local toolbar images from,
     * relative to the image root directory.
     */
    public final static String  IMAGE_DIR_CLCL = "clcl16/"; //$NON-NLS-1$

    /**
     * The directory where to load disabled local toolbar images from,
     * relative to the image root directory.
     */
    public final static String  IMAGE_DIR_DLCL = "dlcl16/"; //$NON-NLS-1$

    /**
     * The directory where to load enabled local toolbar images from,
     * relative to the image root directory.
     */
    public final static String  IMAGE_DIR_ELCL = "elcl16/"; //$NON-NLS-1$

    // ***** The image constants *****

    /**
     * The key to access the refresh action image (enabled).
     */
    public static final String  ACTION_Refresh_Enabled = "RefreshAction_enabled"; //$NON-NLS-1$

    /**
     * The key to access the refresh action image (disabled).
     */
    public static final String  ACTION_Refresh_Disabled = "RefreshAction_disabled"; //$NON-NLS-1$
}
