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

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.internal.ImageConsts;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSOperation;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.ui.trees.TreeColumnLabelProvider;
import org.eclipse.tcf.te.ui.views.interfaces.IUIConstants;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;

/**
 * The label provider for the tree column "name".
 */
public class FSTreeElementLabelProvider extends TreeColumnLabelProvider {
	// The editor registry used to search a file's image.
	private IEditorRegistry editorRegistry = null;

	/**
	 * Constructor.
	 */
	public FSTreeElementLabelProvider() {
	}

	/**
	 * Constructor.
	 *
	 * @param viewer The tree viewer or <code>null</code>.
	 */
	public FSTreeElementLabelProvider(TreeViewer viewer) {
		this.viewer = viewer;
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

	/**
	 * Returns the parent tree viewer instance.
	 *
	 * @return The parent tree viewer or <code>null</code>.
	 */
	private TreeViewer getViewer() {
		if (viewer == null) {
			if (PlatformUI.getWorkbench() != null && PlatformUI.getWorkbench()
			                .getActiveWorkbenchWindow() != null && PlatformUI.getWorkbench()
			                .getActiveWorkbenchWindow().getActivePage() != null) {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				                .getActivePage();
				IViewPart part = page.findView(IUIConstants.ID_EXPLORER);
				if (part instanceof CommonNavigator) {
					viewer = ((CommonNavigator) part).getCommonViewer();
				}
			}
		}
		return viewer;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element) {
		if (element != null) {
			boolean isExpanded = getViewer().getExpandedState(element);
			if (element instanceof FSTreeNode) {
				FSTreeNode node = (FSTreeNode) element;
				if ("FSRootNode".equals(node.type)) { //$NON-NLS-1$
					return UIPlugin.getImage(ImageConsts.ROOT);
				}
				else if ("FSRootDirNode".equals(node.type)) {//$NON-NLS-1$
					return (isExpanded && hasChildren(node)) ? UIPlugin
					                .getImage(ImageConsts.ROOT_DRIVE_OPEN) : UIPlugin
					                .getImage(ImageConsts.ROOT_DRIVE);
				}
				else if ("FSDirNode".equals(node.type)) { //$NON-NLS-1$
					return (isExpanded && hasChildren(node)) ? PlatformUI.getWorkbench()
					                .getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER) : UIPlugin
					                .getImage(ImageConsts.FOLDER);
				}
				else if ("FSFileNode".equals(node.type)) { //$NON-NLS-1$
					String key = node.name;
					Image image = UIPlugin.getImage(key);
					if (image == null) {

						ImageDescriptor descriptor = getEditorRegistry().getImageDescriptor(key);
						if (descriptor == null) descriptor = getEditorRegistry()
						                .getSystemExternalEditorImageDescriptor(key);
						if (descriptor != null) UIPlugin.getDefault().getImageRegistry()
						                .put(key, descriptor);
						image = UIPlugin.getImage(key);
					}
					return image;
				}
			}
		}

		return super.getImage(element);
	}

	/**
	 * If the specified folder has children.
	 *
	 * @param folder The folder node.
	 * @return true if it has children.
	 */
	private boolean hasChildren(FSTreeNode folder) {
		List<FSTreeNode> children = FSOperation.getCurrentChildren(folder);
		return children != null && !children.isEmpty();
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
