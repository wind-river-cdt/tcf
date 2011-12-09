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

import java.util.Comparator;

import org.eclipse.tcf.te.tcf.processes.ui.controls.ProcessesTreeNode;

/**
 * The comparator for the tree column "state".
 */
public class StateComparator implements Comparator<ProcessesTreeNode> {

	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(ProcessesTreeNode o1, ProcessesTreeNode o2) {
		if (o1.state == null) {
			if (o2.state == null) return 0;
			return -1;
		}
		if (o2.state == null) return 1;
		return o1.state.compareTo(o2.state);
	}
}
