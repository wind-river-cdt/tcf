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

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
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
		String ext = "noext"; //$NON-NLS-1$
		int index = name.lastIndexOf("."); //$NON-NLS-1$
		if (index != -1) ext = name.substring(index + 1);
		return ext;
	}
	
	/**
	 * Get the directory to store the temporary mirror files.
	 * 
	 * @return The directory to contain the mirror files.
	 */
	private File getMirrorDir() {
		File tmpDir = getTempDir();
		File mrrDir = new File(tmpDir, ".mrr"); //$NON-NLS-1$
		if(!mrrDir.exists()) mrrDir.mkdirs();
		return mrrDir;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.columns.LabelProviderUpdateDaemon#getImageKey(org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode)
	 */
	@Override
	protected String getImageKey(FSTreeNode node) {
		String ext = getFileExt(node);
		return "EXT_IMAGE@" + ext; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.columns.LabelProviderUpdateDaemon#getMirrorFile(org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode)
	 */
	@Override
	protected File getMirrorFile(FSTreeNode node) {
		String ext = getFileExt(node);
		File mrrDir = getMirrorDir();
		File file = new File(mrrDir, "mirror" + "." + ext); //$NON-NLS-1$ //$NON-NLS-2$
		if (!file.exists()) {
			try {
				file.createNewFile();
			}
			catch (IOException e) {
			}
		}
		return file;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.columns.LabelProviderUpdateDaemon#getImage(org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode)
	 */
	@Override
	public Image getImage(FSTreeNode node) {
		String key = getImageKey(node);
		Image image = UIPlugin.getImage(key);
		if (image == null) {
			String name = node.name;
			int dot = name.lastIndexOf("."); //$NON-NLS-1$
			if (dot != -1) {
				String ending = name.substring(dot);
				Program program = Program.findProgram(ending);
				if (program != null) {
					ImageData iconData = program.getImageData();
					image = new Image(Display.getCurrent(), iconData);
					UIPlugin.getDefault().getImageRegistry().put(key, image);
				}
			}
		}
		return UIPlugin.getImage(key);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.columns.LabelProviderUpdateDaemon#getImgFile(org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode)
	 */
	@Override
    protected File getImgFile(FSTreeNode node) {
		String ext = getFileExt(node);
	    return getTempImg(ext);
    }
}
