/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.columns;

import java.io.File;
import java.io.IOException;

import org.eclipse.tcf.te.tcf.filesystem.internal.utils.CacheManager;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;

/**
 * The background daemon that updates the images of the file system using
 * images retrieved by FileSystemView from Swing. 
 */
public class FileExtBasedImageUpdater extends LabelProviderUpdateDaemon {

	/**
	 * Get the node's file extension or null if there is no extension.
	 * 
	 * @param node The file tree node.
	 * @return The file's extension or null.
	 */
	private String getFileExt(FSTreeNode node) {
		String name = node.name;
		String ext = null;
		int index = name.lastIndexOf("."); //$NON-NLS-1$
		if (index != -1) ext = name.substring(index + 1);
		return ext;
	}

	/**
	 * Get the temporary file used to obtain icons. If it does not exist, then create one.
	 * 
	 * @param ext The extension of the temporary file.
	 * @return The file object.
	 */
	private File getTempFile(String ext) {
		File tmpDir = getTempDir();
		File file = new File(tmpDir, "temp" + (ext == null ? "" : ("." + ext))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (!file.exists()) {
			try {
				file.createNewFile();
			}
			catch (IOException e) {
			}
		}
		return file;
	}

	/**
	 * Get the temporary directory used to store temporary file and images.
	 * 
	 * @return The directory file object.
	 */
	private File getTempDir() {
		File file = CacheManager.getInstance().getCacheRoot();
		file = new File(file, ".tmp"); //$NON-NLS-1$
		if (!file.exists()) file.mkdirs();
		return file;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.columns.LabelProviderUpdateDaemon#getImageKey(org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode)
	 */
	@Override
	protected String getImageKey(FSTreeNode node) {
		String ext = getFileExt(node);
		String key = ext == null ? "" : ext; //$NON-NLS-1$
		key = "EXT_IMAGE@" + key; //$NON-NLS-1$
		return key;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.columns.LabelProviderUpdateDaemon#getMirrorFile(org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode)
	 */
	@Override
	protected File getMirrorFile(FSTreeNode node) {
		return getTempFile(getFileExt(node));
	}

}
