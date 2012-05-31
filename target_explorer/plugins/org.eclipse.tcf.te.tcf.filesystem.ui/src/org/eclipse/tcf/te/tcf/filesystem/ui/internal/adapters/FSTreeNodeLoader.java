/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.adapters;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.exceptions.TCFException;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.NullOpExecutor;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpRefreshRoots;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.Operation;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.ui.interfaces.ILazyLoader;

/**
 * The implementation of ILazyLoader for FSTreeNode check its data availability
 * and load its children if not ready.
 */
public class FSTreeNodeLoader implements ILazyLoader {
	// The node to be checked.
	private FSTreeNode node;
	/**
	 * Constructor
	 * 
	 * @param node The file/folder node.
	 */
	public FSTreeNodeLoader(FSTreeNode node) {
		this.node = node;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ILazyLoader#isDataLoaded()
	 */
	@Override
	public boolean isDataLoaded() {
		return node.isFile() || (node.isSystemRoot() || node.isDirectory()) && node.childrenQueried;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ILazyLoader#loadData(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void loadData(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		try {
			if(node.isFile()) return;
			if (node.isSystemRoot()) {
				new NullOpExecutor().execute(new OpRefreshRoots(node));
			}
			else {
				new Operation().getChildren(node);
			}
		}
		catch (TCFException e) {
			throw new InvocationTargetException(e);
		}
	}
}
