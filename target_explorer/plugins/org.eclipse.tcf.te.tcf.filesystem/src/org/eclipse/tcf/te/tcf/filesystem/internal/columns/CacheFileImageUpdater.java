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
 * The background daemon that updates the images of the file system using
 * images retrieved by FileSystemView from Swing. 
 */
public class CacheFileImageUpdater extends LabelProviderUpdateDaemon {
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.columns.LabelProviderUpdateDaemon#getImageKey(org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode)
	 */
	@Override
    protected String getImageKey(FSTreeNode node) {
	    return node.getLocationURL().toExternalForm();
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.columns.LabelProviderUpdateDaemon#getMirrorFile(org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode)
	 */
	@Override
    protected File getMirrorFile(FSTreeNode node) {
	    return CacheManager.getInstance().getCacheFile(node);
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.columns.LabelProviderUpdateDaemon#getImgFile(org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode)
	 */
	@Override
	protected File getImgFile(FSTreeNode node) {
		File cacheFile = CacheManager.getInstance().getCacheFile(node);
		File parentDir = cacheFile.getParentFile();
		if (!parentDir.exists()) parentDir.mkdirs();
		return new File(parentDir, node.name + ".png"); //$NON-NLS-1$
	}
}
