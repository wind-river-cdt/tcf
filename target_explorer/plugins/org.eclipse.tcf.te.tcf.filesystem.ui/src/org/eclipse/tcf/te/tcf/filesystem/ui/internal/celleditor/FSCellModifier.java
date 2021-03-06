/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.celleditor;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.Item;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.IOpExecutor;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.JobExecutor;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpRename;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.ui.internal.handlers.RenameCallback;

/**
 * FSCellModifier is an <code>ICellModifier</code> of the file system tree of the target explorer.
 */
public class FSCellModifier implements ICellModifier {
	// The column property used to get the name of a given file system node.
	public static final String PROPERTY_NAME = "name"; //$NON-NLS-1$

	public FSCellModifier() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
	 */
	@Override
	public boolean canModify(Object element, String property) {
		if (property.equals(PROPERTY_NAME)) {
			if (element instanceof Item) {
				element = ((Item) element).getData();
			}
			if (element instanceof FSTreeNode) {
				FSTreeNode node = (FSTreeNode) element;
				if (!node.isRoot()) {
					return node.isWindowsNode() && !node.isReadOnly() || !node.isWindowsNode() && node.isWritable();
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
	 */
	@Override
	public Object getValue(Object element, String property) {
		if (property.equals(PROPERTY_NAME)) {
			if (element instanceof Item) {
				element = ((Item) element).getData();
			}
			if (element instanceof FSTreeNode) {
				FSTreeNode node = (FSTreeNode) element;
				return node.name;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String,
	 * java.lang.Object)
	 */
	@Override
	public void modify(Object element, String property, Object value) {
		if (property.equals(PROPERTY_NAME)) {
			if (element instanceof Item) {
				element = ((Item) element).getData();
			}
			if (element instanceof FSTreeNode) {
				FSTreeNode node = (FSTreeNode) element;
				Assert.isTrue(value != null && value instanceof String);
				String newName = (String) value;
				// Rename the node with the new name using an FSRename.
				IOpExecutor executor = new JobExecutor(new RenameCallback());
				executor.execute(new OpRename(node, newName));
			}
		}
	}
}
