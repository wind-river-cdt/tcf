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

import org.eclipse.tcf.te.tcf.filesystem.internal.utils.CacheManager;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;

/**
 * The image update adapter that updates the images of the file which
 * has a local cache copy.
 */
public class CacheFileImageUpdater implements ImageUpdateAdapter {
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.columns.ImageUpdateAdapter#getImageKey(org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode)
	 */
	@Override
    public String getImageKey(FSTreeNode node) {
	    return node.getLocationURL().toExternalForm();
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.columns.ImageUpdateAdapter#getMirrorFile(org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode)
	 */
	@Override
	public File getMirrorFile(FSTreeNode node) {
	    return CacheManager.getInstance().getCacheFile(node);
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.columns.ImageUpdateAdapter#getImgFile(org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode)
	 */
	@Override
	public File getImageFile(FSTreeNode node) {
		File cacheFile = CacheManager.getInstance().getCacheFile(node);
		File parentDir = cacheFile.getParentFile();
		if (!parentDir.exists() && !parentDir.mkdirs()) {
			parentDir = CacheManager.getInstance().getCacheRoot();
		}
		return new File(parentDir, node.name + ".png"); //$NON-NLS-1$
	}
}
