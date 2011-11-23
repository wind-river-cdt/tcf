/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * William Chen (Wind River) - [361324] Add more file operations in the file 
 * 												system of Target Explorer.
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.handlers;

import java.net.URL;
import java.util.List;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSClipboard;
import org.eclipse.tcf.te.tcf.filesystem.model.FSModel;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
/**
 * Provide a tester to test if the paste operation is enabled.
 */
public class ClipboardPropertyTester extends PropertyTester {

	/**
	 * Create an instance.
	 */
	public ClipboardPropertyTester() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (property.equals("canPaste")) { //$NON-NLS-1$
			FSClipboard cb = UIPlugin.getDefault().getClipboard();
			if (!cb.isEmpty()) {
				int operation = cb.getOperation();
				List<URL> urls = cb.getFiles();
				for (URL url : urls) {
					FSTreeNode node = FSModel.getInstance().getTreeNode(url);
					if (node != null) {
						if (operation == FSClipboard.COPY) {
							// If it is not a windows node and it is not readable, 
							// then it cannot be moved.
							if (!node.isWindowsNode() && !node.isReadable()) return false;
						}
						else if (operation == FSClipboard.CUT) {
							// If it is a windows node and is read only, or it is not 
							// a windows node and is not writable, then it cannot be moved.
							if (node.isWindowsNode() && node.isReadOnly() || !node.isWindowsNode() && !node.isWritable()) {
								return false;
							}
						}
					}
				}
				return true;
			}
		}
		return false;
	}
}
