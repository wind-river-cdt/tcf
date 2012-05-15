/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.controls;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tcf.te.tcf.processes.core.model.ProcessTreeNode;
import org.eclipse.tcf.te.tcf.processes.ui.internal.columns.ProcessComparator;
import org.eclipse.tcf.te.ui.trees.TreeViewerSorter;

/**
 * The common sorter for the process contribution to the target explorer.
 */
public class ProcessViewerSorter extends TreeViewerSorter {
	private ProcessComparator comparator = new ProcessComparator();

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
	 * java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (e1 instanceof ProcessTreeNode && e2 instanceof ProcessTreeNode) {
			return comparator.compare((ProcessTreeNode) e1, (ProcessTreeNode) e2);
		}
		return super.compare(viewer, e1, e2);
	}
}
