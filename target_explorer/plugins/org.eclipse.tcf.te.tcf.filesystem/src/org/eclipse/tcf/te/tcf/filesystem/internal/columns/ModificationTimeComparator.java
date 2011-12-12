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

import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;

/**
 * The comparator for the tree column "Date Modified".
 */
public class ModificationTimeComparator extends FSTreeNodeComparator {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.columns.FSTreeNodeComparator#doCompare(org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode, org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode)
	 */
	@Override
	public int doCompare(FSTreeNode node1, FSTreeNode node2) {
		long mtime1 = node1.attr != null ? node1.attr.mtime : 0;
		long mtime2 = node2.attr != null ? node2.attr.mtime : 0;
		return mtime1 < mtime2 ? -1 : (mtime1 > mtime2 ? 1 : 0);
	}
}
