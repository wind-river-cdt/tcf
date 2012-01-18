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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;

/**
 * The label provider for the tree column "Date Modified".
 */
public class ModificationTimeLabelProvider extends LabelProvider {
	// The date formatter.
	private static final SimpleDateFormat DATE_MODIFIED_FORMAT = new SimpleDateFormat("M/d/yyyy h:mm aa"); //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof FSTreeNode) {
			FSTreeNode node = (FSTreeNode) element;
			// Pending nodes does not have column texts at all
			if (node.isPendingNode()) return ""; //$NON-NLS-1$
			if (node.attr != null) {
				return DATE_MODIFIED_FORMAT.format(new Date(node.attr.mtime));
			}
		}
		return ""; //$NON-NLS-1$
	}
}
