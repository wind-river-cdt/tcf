/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.internal.tabbed;

import org.eclipse.jface.viewers.IFilter;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessTreeNode;

/**
 * The filter to filter out root and pending process node.
 */
public class ProcessFilter implements IFilter {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IFilter#select(java.lang.Object)
	 */
	@Override
	public boolean select(Object toTest) {
		if(toTest instanceof ProcessTreeNode) {
			ProcessTreeNode node = (ProcessTreeNode) toTest;
			return !(node.type.equals("ProcRootNode") || node.type.equals("ProcPendingNode")); //$NON-NLS-1$//$NON-NLS-2$
		}
		return false;
	}

}
