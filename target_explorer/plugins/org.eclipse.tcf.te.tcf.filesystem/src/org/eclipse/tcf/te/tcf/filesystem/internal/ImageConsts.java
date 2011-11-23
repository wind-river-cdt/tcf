/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal;

/**
 * File system plug-in Image registry constants.
 */
public interface ImageConsts {

	// ***** The directory structure constants *****

	/**
	 * The root directory where to load the images from, relative to
	 * the bundle directory.
	 */
    public final static String  IMAGE_DIR_ROOT = "icons/"; //$NON-NLS-1$

    /**
     * The directory where to load model object images from,
     * relative to the image root directory.
     */
    public final static String  IMAGE_DIR_OBJ = "obj16/"; //$NON-NLS-1$

    /**
     * The directory where to load model object images from,
     * relative to the image root directory.
     */
    public final static String  IMAGE_DIR_OBJ32 = "obj32/"; //$NON-NLS-1$
    
    /**
     * The directory where to load the decorator image from.
     */
    public final static String IMAGE_DIR_OVR = "ovr/"; //$NON-NLS-1$
    
    // ***** The image constants *****

    /**
     * The key to access the base folder object image.
     */
    public static final String FOLDER = "Folder"; //$NON-NLS-1$

    /**
     * The key to access the base folder object image.
     */
    public static final String ROOT_DRIVE = "RootDrive"; //$NON-NLS-1$

    /**
     * The key to access the base folder object image.
     */
    public static final String ROOT_DRIVE_OPEN = "RootDriveOpen"; //$NON-NLS-1$
    
    /**
     * The key to access the image of compare editor.
     */
    public static final String COMPARE_EDITOR = "CompareEditor"; //$NON-NLS-1$
    
    /**
     * The key to access the title image of "replace folder confirm" dialog.
     */
    public static final String REPLACE_FOLDER_CONFIRM = "ReplaceFolderConfirm"; //$NON-NLS-1$
    
    /**
     * The key to access the title image of "confirm read only delete" dialog.
     */
    public static final String DELETE_READONLY_CONFIRM = "ConfirmReadOnlyDelete"; //$NON-NLS-1$
    
    /**
     * The key to access the cut decorator image.
     */
    public static final String CUT_DECORATOR_IMAGE = "CutDecorator";  //$NON-NLS-1$
    
	/** 
	 * The key to access the banner image of the advanced attributes dialog.
	 */
	public static final String BANNER_IMAGE = "BannerImage"; //$NON-NLS-1$

	/**
	 * The key to access the error image used in the tool tip popped up during renaming.
	 */
	public static final String ERROR_IMAGE = "ErrorImage"; //$NON-NLS-1$
}
