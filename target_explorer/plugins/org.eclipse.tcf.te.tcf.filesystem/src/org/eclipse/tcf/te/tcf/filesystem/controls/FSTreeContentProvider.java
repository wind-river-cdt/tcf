/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.controls;

import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;



/**
 * File system tree content provider implementation.
 */
public class FSTreeContentProvider extends FSNavigatorContentProvider {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.controls.FSNavigatorContentProvider#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object element) {
		if (element instanceof FSTreeNode) {
			return ((FSTreeNode)element).parent;
		}
		return null;
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.controls.FSNavigatorContentProvider#isRootNodeVisible()
	 */
	@Override
    protected boolean isRootNodeVisible() {
	    return false;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.TreeContentProvider#isRootObject(java.lang.Object)
	 */
	@Override
    protected boolean isRootObject(Object object) {
		if(object instanceof FSTreeNode) {
			FSTreeNode node = (FSTreeNode) object;
			return node.isSystemRoot();
		}
	    return false;
    }		
}