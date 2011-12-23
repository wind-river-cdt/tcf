/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.operations;

/**
 * The file operation class to create the root node in the file system of Target Explorer.
 */
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.IFileSystem;
import org.eclipse.tcf.services.IFileSystem.DirEntry;
import org.eclipse.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.filesystem.internal.exceptions.TCFChannelException;
import org.eclipse.tcf.te.tcf.filesystem.model.FSModel;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;

public class FSCreateRoot extends FSOperation {
	// The peer model in which the file system root is going to be created.
	/* default */IPeerModel peerModel;

	/**
	 * Create an instance using the peer model.
	 * 
	 * @param peerModel The peer model.
	 */
	public FSCreateRoot(IPeerModel peerModel) {
		this.peerModel = peerModel;
	}

	/**
	 * Create the file system's root node.
	 * 
	 * @return The root file system node.
	 */
	public FSTreeNode create() {
		Assert.isTrue (!Protocol.isDispatchThread());
		final FSTreeNode[] result = new FSTreeNode[1];
		Protocol.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				result[0] = createRootNode();
			}
		});
		SafeRunner.run(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				queryRootNodes(result[0]);
			}
		});
		return result[0];
	}

	/**
	 * Create the root node instance.
	 * 
	 * @return The root node instance.
	 */
	FSTreeNode createRootNode() {
	    final FSTreeNode rootNode = new FSTreeNode();
	    rootNode.type = "FSRootNode"; //$NON-NLS-1$
	    rootNode.peerNode = peerModel;
	    FSModel.getFSModel(peerModel).setRoot(rootNode);
	    return rootNode;
    }

	/**
	 * Query the root file system node's children nodes.
	 * 
	 * @param rootNode The root file system node.
	 * @throws TCFChannelException Thrown when openning a channel.
	 */
	/* default */void queryRootNodes(final FSTreeNode rootNode) throws TCFChannelException {
		rootNode.childrenQueried = false;
		IChannel channel = null;
		try {
			channel = openChannel(peerModel.getPeer());
			IFileSystem service = getBlockingFileSystem(channel);
			if (service != null) {
				rootNode.childrenQueryRunning = true;
				service.roots(new IFileSystem.DoneRoots() {
					@Override
					public void doneRoots(IToken token, FileSystemException error, DirEntry[] entries) {
						if (error == null) {
							for (DirEntry entry : entries) {
								FSTreeNode node = createNodeFromDirEntry(entry, true);
								if (node != null) {
									node.parent = rootNode;
									node.peerNode = rootNode.peerNode;
									rootNode.getChildren().add(node);
								}
							}
						}
					}
				});
				// Reset the children query markers
				rootNode.childrenQueryRunning = false;
			}
		}
		finally {
			if (channel != null) Tcf.getChannelManager().closeChannel(channel);
		}
		rootNode.childrenQueried = true;
	}
}