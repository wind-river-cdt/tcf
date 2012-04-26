/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.internal.viewer;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.tcf.te.launch.ui.model.LaunchNode;

/**
 * The common sorter for the launch contribution to the target explorer.
 */
public class LaunchTreeViewerSorter extends ViewerSorter {
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
	 * java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (e1 instanceof LaunchNode && e2 instanceof LaunchNode) {
			return ((LaunchNode)e1).getName().compareTo(((LaunchNode)e2).getName());
		}
		return super.compare(viewer, e1, e2);
	}
}
