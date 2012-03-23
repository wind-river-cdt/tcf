/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.columns;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.internal.ImageConsts;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * The label provider for the tree column "name".
 */
public class FSTreeElementLabelProvider extends LabelProvider {
	// The background daemon that updates the images of the file system nodes.
	private static LabelProviderUpdateDaemon updateDaemon;
	static {
		updateDaemon = new LabelProviderUpdateDaemon();
		updateDaemon.start();
	}
	
	// The editor registry used to search a file's image.
	private IEditorRegistry editorRegistry = null;

	/**
	 * Constructor.
	 */
	public FSTreeElementLabelProvider() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof FSTreeNode) {
			return ((FSTreeNode) element).name;
		}
		return super.getText(element);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element) {
		if (element instanceof FSTreeNode) {
			FSTreeNode node = (FSTreeNode) element;
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
				String key = node.name;
				Image image = UIPlugin.getImage(key);
				if (image == null) {
					updateDaemon.enqueue(node);
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
		}

		return super.getImage(element);
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
