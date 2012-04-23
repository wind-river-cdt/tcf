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

import java.io.Serializable;
import java.util.Comparator;

import org.eclipse.tcf.te.tcf.processes.core.model.ProcessTreeNode;

/**
 * The comparator for the tree column "PPID".
 */
public class PPIDComparator implements Comparator<ProcessTreeNode>, Serializable {
    private static final long serialVersionUID = 1L;
	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(ProcessTreeNode o1, ProcessTreeNode o2) {
		return o1.ppid == o2.ppid ? 0 : (o1.ppid < o2.ppid ? -1 : 1);
	}
}
