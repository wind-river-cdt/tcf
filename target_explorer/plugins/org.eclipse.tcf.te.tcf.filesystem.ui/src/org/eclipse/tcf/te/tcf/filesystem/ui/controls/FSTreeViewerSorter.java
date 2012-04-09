/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.controls;

import org.eclipse.jface.viewers.TreePathViewerSorter;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.ui.internal.columns.FSTreeElementComparator;

/**
 * File system tree control viewer sorter implementation.
 */
public class FSTreeViewerSorter extends TreePathViewerSorter {
	private final FSTreeElementComparator comparator;

	/**
	 * Constructor.
	 */
	public FSTreeViewerSorter() {
		comparator = new FSTreeElementComparator();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (e1 instanceof FSTreeNode && e2 instanceof FSTreeNode) {
			return comparator.compare((FSTreeNode) e1, (FSTreeNode) e2);
		}
		return super.compare(viewer, e1, e2);
	}
}
