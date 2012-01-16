/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.callbacks;

import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IFileSystem.DirEntry;
import org.eclipse.tcf.services.IFileSystem.DoneRoots;
import org.eclipse.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.filesystem.model.FSModel;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;

/**
 * The callback handler that handles the event when the roots are listed.
 */
public class QueryDoneRoots implements DoneRoots {
	// The channel being used.
	IChannel channel;
	// The parent directory node.
	FSTreeNode parentNode;

	/**
	 * Create an instance with parameters to initialize the fields.
	 * 
	 * @param channel The tcf channel.
	 * @param parentNode The parent directory node.
	 */
	public QueryDoneRoots(IChannel channel, FSTreeNode parentNode) {
		this.channel = channel;
		this.parentNode = parentNode;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.IFileSystem.DoneRoots#doneRoots(org.eclipse.tcf.protocol.IToken, org.eclipse.tcf.services.IFileSystem.FileSystemException, org.eclipse.tcf.services.IFileSystem.DirEntry[])
	 */
	@Override
	public void doneRoots(IToken token, FileSystemException error, DirEntry[] entries) {
		if (error == null) {
			for (DirEntry entry : entries) {
				FSTreeNode node = new FSTreeNode(parentNode, entry, true);
				parentNode.getChildren().add(node);
			}

			// Reset the children query markers
			parentNode.childrenQueryRunning = false;
			parentNode.childrenQueried = true;
			FSModel.firePropertyChange(parentNode);
		}
		// Close the channel, not needed anymore
		Tcf.getChannelManager().closeChannel(channel);
	}
}
