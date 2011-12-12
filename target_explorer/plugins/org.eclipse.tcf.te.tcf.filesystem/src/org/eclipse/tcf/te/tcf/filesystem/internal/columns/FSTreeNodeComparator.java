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

import java.util.Comparator;

import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;

/**
 * The base comparator for all the file system tree column.
 */
public abstract class FSTreeNodeComparator implements Comparator<FSTreeNode> {

	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(FSTreeNode node1, FSTreeNode node2) {
		// Get the type labels
		String type1 = node1.type;
		String type2 = node2.type;

		// Group directories and files always together before sorting by name
		if (("FSRootDirNode".equals(type1) || "FSDirNode".equals(type1)) //$NON-NLS-1$ //$NON-NLS-2$
		                && !("FSRootDirNode".equals(type2) || "FSDirNode".equals(type2))) { //$NON-NLS-1$ //$NON-NLS-2$
			return -1;
		}

		if (("FSRootDirNode".equals(type2) || "FSDirNode".equals(type2)) //$NON-NLS-1$ //$NON-NLS-2$
		                && !("FSRootDirNode".equals(type1) || "FSDirNode".equals(type1))) { //$NON-NLS-1$ //$NON-NLS-2$
			return 1;
		}

		// If the nodes are of the same type and one entry starts
		// with a '.', it comes before the one without a '.'
		if (type1 != null && type2 != null && type1.equals(type2)) {
			return doCompare(node1, node2);
		}
		return 0;
	}

	/**
	 * Sort the node1 and node2 when they are both directories or files.
	 * 
	 * @param node1 The first node.
	 * @param node2 The second node.
	 * @return The comparison result.
	 */
	public abstract int doCompare(FSTreeNode node1, FSTreeNode node2);
}
