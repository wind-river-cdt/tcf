/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.interfaces;

/**
 * UIConstants for file system.
 */
public interface IUIConstants extends org.eclipse.tcf.te.ui.interfaces.IUIConstants {
	
	/**
	 * The viewer id of the file tree of Target Explorer.
	 */
	public static final String ID_TREE_VIEWER_FS = ID_CONTROL_MENUS_BASE + ".viewer.fs"; //$NON-NLS-1$
	
	/**
	 * The help id of the file tree of Target Explorer.
	 */
	public static final String ID_TREE_VIEWER_FS_HELP = ID_TREE_VIEWER_FS + ".help"; //$NON-NLS-1$
	
	/**
	 * The menu id of the file tree of Target Explorer.
	 */
	public static final String ID_TREE_VIEWER_FS_CONTEXT_MENU = ID_CONTROL_MENUS_BASE + ".menu.fs";  //$NON-NLS-1$
}
