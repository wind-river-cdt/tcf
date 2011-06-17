/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.filters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.tm.te.tcf.filesystem.interfaces.IWindowsFileAttributes;
import org.eclipse.tm.te.tcf.filesystem.model.FSTreeNode;

/**
 * Target Explorer: A filter implementation filtering hidden files or directories.
 */
public class HiddenFilesViewerFilter extends ViewerFilter {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		// The element needs to be a tree node, but not a root node
		if (element instanceof FSTreeNode && !"FSRootDirNode".equals(((FSTreeNode)element).type)) { //$NON-NLS-1$
			FSTreeNode node = (FSTreeNode)element;
			// Check if the tree node has attributes associated
			if (node.attr != null && node.attr.attributes.get("Win32Attrs") instanceof Integer) { //$NON-NLS-1$
				Integer win32Attrs = (Integer)node.attr.attributes.get("Win32Attrs"); //$NON-NLS-1$
				return (win32Attrs.intValue() & IWindowsFileAttributes.FILE_ATTRIBUTE_HIDDEN) == 0;
			}
		}
		// Let pass all other elements unharmed
		return true;
	}

}