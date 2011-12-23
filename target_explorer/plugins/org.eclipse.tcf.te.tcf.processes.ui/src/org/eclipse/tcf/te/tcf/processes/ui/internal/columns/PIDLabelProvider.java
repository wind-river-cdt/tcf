/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.internal.columns;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessTreeNode;
import org.eclipse.tcf.te.ui.trees.TreeColumnLabelProvider;

/**
 * The label provider for the tree column "PID".
 */
public class PIDLabelProvider extends TreeColumnLabelProvider {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	@Override
	public String getText(Object element) {
		Assert.isTrue(element instanceof ProcessTreeNode);
		ProcessTreeNode node = (ProcessTreeNode) element;
		// Pending nodes does not have column texts at all
		if (node.type.endsWith("PendingNode")) return ""; //$NON-NLS-1$ //$NON-NLS-2$
		String id = Long.toString(node.pid);
		if (id == null) id = node.id;
		if (id != null) return id.startsWith("P") ? id.substring(1) : id; //$NON-NLS-1$
		return ""; //$NON-NLS-1$
	}
}