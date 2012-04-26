/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.navigator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tcf.te.ui.trees.TreeViewerSorter;
import org.eclipse.tcf.te.ui.views.interfaces.ICategory;

/**
 * Category viewer sorter implementation.
 */
public class ViewerSorter extends TreeViewerSorter {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.TreeViewerSorter#doCompare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object, java.lang.String, int, int)
	 */
	@Override
	protected int doCompare(Viewer viewer, Object node1, Object node2, String sortColumn, int index, int inverter) {
		if (node1 instanceof ICategory && node2 instanceof ICategory) {
			int rank1 = ((ICategory)node1).getRank();
			int rank2 = ((ICategory)node2).getRank();

			if (rank1 != -1 && rank2 != -1 && rank1 != rank2) {
				return (rank1 - rank2) * inverter;
			}
		}

	    return super.doCompare(viewer, node1, node2, sortColumn, index, inverter);
	}
}
