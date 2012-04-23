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
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.tcf.te.tcf.processes.core.model.ProcessTreeNode;

/**
 * The label provider for the tree column "user".
 */
public class UserLabelProvider extends LabelProvider {

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
		String username = node.username;
		if (username != null) return username;
		return ""; //$NON-NLS-1$
	}
}
