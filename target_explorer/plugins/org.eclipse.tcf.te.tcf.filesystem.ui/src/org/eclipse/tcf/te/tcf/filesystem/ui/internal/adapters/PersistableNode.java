/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.adapters;

import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

/**
 * The adapter class of FSTreeNode for IPersistableElement, used to 
 * persist an FSTreeNode. 
 */
public class PersistableNode implements IPersistableElement {
	// The node to be persisted.
	private FSTreeNode node;
	/**
	 * Create an instance.
	 * 
	 * @param node The node to be persisted.
	 */
	public PersistableNode(FSTreeNode node) {
		this.node = node;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IPersistable#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void saveState(IMemento memento) {
		memento.putString("peerId", node.peerNode.getPeerId()); //$NON-NLS-1$
		String path = null;
		if (!node.isSystemRoot()) path = node.getLocation();
		if (path != null) memento.putString("path", path); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IPersistableElement#getFactoryId()
	 */
	@Override
	public String getFactoryId() {
		return "org.eclipse.tcf.te.tcf.filesystem.ui.nodeFactory"; //$NON-NLS-1$
	}
}
