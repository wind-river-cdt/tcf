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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.internal.ImageConsts;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * The default implementation of ImageProvider, defining the images
 * based on predefined images based on the node type. 
 */
public class DefaultImageProvider implements ImageProvider {
	// The editor registry used to search a file's image.
	private IEditorRegistry editorRegistry = null;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.columns.ImageProvider#getImage(org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode)
	 */
	@Override
	public Image getImage(FSTreeNode node) {
		if (node.isPendingNode()) {
			return UIPlugin.getImage(ImageConsts.PENDING);
		}
		else if (node.isSystemRoot()) {
			return UIPlugin.getImage(ImageConsts.ROOT);
		}
		else if (node.isRoot()) {
			return UIPlugin.getImage(ImageConsts.ROOT_DRIVE);
		}
		else if (node.isDirectory()) {
			return UIPlugin.getImage(ImageConsts.FOLDER);
		}
		else if (node.isFile()) {
			Image image = getProgramImage(node);
			if(image != null)
				return image;
			return getPredefinedImage(node);
		}
		return null;
	}

	/**
	 * Get the image provided by program class first.
	 * 
	 * @param node The file node.
	 * @return The image provided by program.
	 */
	protected Image getProgramImage(FSTreeNode node) {
		String name = node.name;
		int dot = name.lastIndexOf("."); //$NON-NLS-1$
		if (dot != -1) {
			String ending = name.substring(dot);
			String key = "EXT_IMAGE" + ending; //$NON-NLS-1$
			Image image = UIPlugin.getImage(key);
			if (image == null) {
				Program program = Program.findProgram(ending);
				if (program != null) {
					ImageData iconData = program.getImageData();
					image = new Image(Display.getCurrent(), iconData);
					UIPlugin.getDefault().getImageRegistry().put(key, image);
				}
			}
			return image;
		}
		return null;
	}
	/**
	 * Get a predefined image for the tree node. These images are retrieved from
	 * editor registry.
	 * 
	 * @param node The file tree node.
	 * @return The editor image for this type.
	 */
	protected Image getPredefinedImage(FSTreeNode node) {
	    Image image;
	    String key = node.name;
	    image = UIPlugin.getImage(key);
	    if (image == null) {
	    	ImageDescriptor descriptor = getEditorRegistry().getImageDescriptor(key);
	    	if (descriptor == null) {
	    		descriptor = getEditorRegistry().getSystemExternalEditorImageDescriptor(key);
	    	}
	    	if (descriptor != null) {
	    		UIPlugin.getDefault().getImageRegistry().put(key, descriptor);
	    	}
	    	image = UIPlugin.getImage(key);
	    }
	    return image;
    }

	/**
	 * Returns the workbench's editor registry.
	 */
	private IEditorRegistry getEditorRegistry() {
		if (editorRegistry == null) {
			IWorkbench workbench = PlatformUI.getWorkbench();
			if (workbench != null) editorRegistry = workbench.getEditorRegistry();
		}
		return editorRegistry;
	}
}
