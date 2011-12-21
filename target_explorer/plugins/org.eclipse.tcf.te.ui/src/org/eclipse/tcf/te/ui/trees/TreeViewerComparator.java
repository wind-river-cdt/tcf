/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.trees;

import java.util.Comparator;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;


/**
 * The tree control viewer comparator implementation.
 */
public class TreeViewerComparator extends ViewerComparator {

	/**
	 * Constructor.
	 *
	 * @param viewer The parent viewer. Must not be <code>null</code>.
	 * @param labelProvider The label provider. Must not be <code>null</code>.
	 */
	public TreeViewerComparator() {
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		Tree tree = ((TreeViewer) viewer).getTree();
		int inverter = tree.getSortDirection() == SWT.DOWN ? -1 : 1;
		TreeColumn treeColumn = tree.getSortColumn();
		if(treeColumn == null) {
			// If the sort column is not set, then use the first column.
			treeColumn = tree.getColumn(0);
		}
		if (treeColumn != null && !treeColumn.isDisposed()) {
			ColumnDescriptor column = (ColumnDescriptor) treeColumn.getData();
			if (column != null) {
				Comparator comparator = column.getComparator();
				if (comparator != null) {
					return inverter * comparator.compare(e1, e2);
				}
			}
		}
		return inverter * super.compare(viewer, e1, e2);
	}
}
