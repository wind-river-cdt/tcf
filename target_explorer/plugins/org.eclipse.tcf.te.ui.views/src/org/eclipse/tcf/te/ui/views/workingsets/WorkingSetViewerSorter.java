/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.workingsets;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tcf.te.ui.trees.TreeViewerSorter;
import org.eclipse.tcf.te.ui.views.internal.View;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.internal.WorkingSetComparator;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * Working set viewer sorter implementation.
 */
@SuppressWarnings("restriction")
public class WorkingSetViewerSorter extends TreeViewerSorter {
    private final WorkingSetComparator wsComparator = new WorkingSetComparator();

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.TreeViewerSorter#doCompare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object, java.lang.String, int, int)
	 */
    @Override
	protected int doCompare(Viewer viewer, Object node1, Object node2, String sortColumn, int index, int inverter) {
		if (node1 instanceof IWorkingSet && node2 instanceof IWorkingSet) {
			WorkingSetViewStateManager manager = null;
			if (viewer instanceof CommonViewer && ((CommonViewer)viewer).getCommonNavigator() instanceof View) {
				manager = ((View)((CommonViewer)viewer).getCommonNavigator()).getStateManager();
			}
			if (manager != null) {
				if (manager.isSortedWorkingSet()) return wsComparator.compare(node1, node2);
				CustomizedOrderComparator comparator = manager.getWorkingSetComparator();
				if (comparator != null) return comparator.compare((IWorkingSet)node1, (IWorkingSet)node2);
			}

		}
	    return super.doCompare(viewer, node1, node2, sortColumn, index, inverter);
	}

}
