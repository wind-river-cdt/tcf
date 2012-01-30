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

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object element) {
		if (element instanceof FSTreeNode) {
			FSTreeNode parent = ((FSTreeNode) element).parent;
			return parent != null && !parent.isSystemRoot() ? parent : null;
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
}