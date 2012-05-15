/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.help;

import org.eclipse.tcf.te.tcf.filesystem.ui.activator.UIPlugin;

/**
 * Plugin context help id definitions.
 */
public interface IContextHelpIds {

	/**
	 * Target Explorer file system UI plug-in common context help id prefix.
	 */
	public final static String PREFIX = UIPlugin.getUniqueIdentifier() + "."; //$NON-NLS-1$

	/**
	 * Target Explorer details editor page: File system explorer
	 */
	public final static String FS_EXPLORER_EDITOR_PAGE = PREFIX + "FSExplorerEditorPage"; //$NON-NLS-1$

	/**
	 * The wizard for creating a new file.
	 */
	public final static String FS_NEW_FILE_WIZARD_PAGE = PREFIX + "FSNewFilePage"; //$NON-NLS-1$
}
