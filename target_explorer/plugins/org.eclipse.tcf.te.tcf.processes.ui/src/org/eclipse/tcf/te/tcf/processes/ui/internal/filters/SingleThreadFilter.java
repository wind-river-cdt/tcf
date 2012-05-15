/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.internal.filters;

import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.tcf.te.tcf.processes.core.model.ProcessTreeNode;

/**
 * The filter to filter out the single thread of a process which has the same name
 * and id with its parent process.
 */
public class SingleThreadFilter extends ViewerFilter {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if(parentElement instanceof TreePath) {
			parentElement = ((TreePath)parentElement).getLastSegment();
		}
		if(parentElement instanceof ProcessTreeNode && element instanceof ProcessTreeNode) {
			ProcessTreeNode parent = (ProcessTreeNode) parentElement;
			ProcessTreeNode child = (ProcessTreeNode) element;
			if(parent.getChildren().size() == 1) {
				if(parent.pid == child.pid) {
					if (parent.name != null) {
						return !parent.name.equals(child.name);
					}
					else if (child.name != null) {
						return !child.name.equals(parent.name);
					}
					return false;
				}
			}
		}
		return true;
	}
}
