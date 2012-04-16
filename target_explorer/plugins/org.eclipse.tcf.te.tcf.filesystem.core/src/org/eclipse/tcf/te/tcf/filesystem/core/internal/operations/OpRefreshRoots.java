/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.core.internal.operations;

/**
 * The file operation class to create the root node in the file system of Target Explorer.
 */
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IFileSystem;
import org.eclipse.tcf.services.IFileSystem.DirEntry;
import org.eclipse.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.exceptions.TCFChannelException;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;

/**
 * The operation to refresh the root of the file system.
 */
public class OpRefreshRoots extends Operation {
	/* default */FSTreeNode root;

	/**
	 * Create an instance using the peer model.
	 *
	 * @param peerModel The peer model.
	 */
	public OpRefreshRoots(FSTreeNode root) {
		this.root = root;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.Operation#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
	    super.run(monitor);
		IChannel channel = null;
		try {
			channel = openChannel(root.peerNode.getPeer());
			IFileSystem service = getBlockingFileSystem(channel);
			if (service != null) {
				root.queryStarted();
				service.roots(new IFileSystem.DoneRoots() {
					@Override
					public void doneRoots(IToken token, FileSystemException error, DirEntry[] entries) {
						if (error == null) {
							for (DirEntry entry : entries) {
								FSTreeNode node = new FSTreeNode(root, entry, true);
								root.addChild(node);
							}
						}
					}
				});
				// Reset the children query markers
				root.queryDone();
			}
		}
		catch(TCFChannelException e) {
			throw new InvocationTargetException(e);
		}
		finally {
			if (channel != null) Tcf.getChannelManager().closeChannel(channel);
		}
	}
}
