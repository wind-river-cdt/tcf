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

import org.eclipse.swt.graphics.Image;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;

/**
 * Windows specific image provider extending the default image provider
 * to retrieve the file's images based on file extension or cached file. 
 */
public class WindowsImageProvider extends DefaultImageProvider {
	// The background daemons that updates the images of the file system nodes.
	static LabelProviderUpdateDaemon updateDaemon;
	static {
		updateDaemon = new LabelProviderUpdateDaemon();
		updateDaemon.start();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.columns.DefaultImageProvider#getImage(org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode)
	 */
	@Override
    public Image getImage(FSTreeNode node) {
		Image image = null;
		if (node.isRoot()) {
            image = updateDaemon.getDiskImage();
		}
		else if (node.isDirectory()) {
			image = updateDaemon.getFolderImage();
		}
		else if(node.isFile()) {
			image = updateDaemon.getImage(node);
            if (image == null) {
            	updateDaemon.enqueue(node);
    			image = getProgramImage(node);
				if (image == null) {
					image = getPredefinedImage(node);
				}
            }
		}
	    return image != null ? image : super.getImage(node);
    }
}
