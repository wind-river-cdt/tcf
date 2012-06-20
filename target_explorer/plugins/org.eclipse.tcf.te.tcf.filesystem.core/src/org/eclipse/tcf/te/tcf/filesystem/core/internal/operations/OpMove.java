/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.core.internal.operations;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IFileSystem;
import org.eclipse.tcf.services.IFileSystem.DoneRename;
import org.eclipse.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IConfirmCallback;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.exceptions.TCFException;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.exceptions.TCFFileSystemException;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.core.nls.Messages;

/**
 * FSMove moves specified tree nodes to a destination folder.
 */
public class OpMove extends Operation {
	// The file/folder nodes to be moved.
	List<FSTreeNode> nodes;
	// The destination folder to be moved to.
	FSTreeNode dest;
	// The callback
	IConfirmCallback confirmCallback;

	/**
	 * Create a move operation to move the specified nodes to the destination folder.
	 *
	 * @param nodes The nodes to be moved.
	 * @param dest the destination folder to move to.
	 */
	public OpMove(List<FSTreeNode> nodes, FSTreeNode dest) {
		this(nodes, dest, null);
	}

	/**
	 * Create a move operation to move the specified nodes to the destination folder
	 * and a confirmation callback.
	 *
	 * @param nodes The nodes to be moved.
	 * @param dest the destination folder to move to.
	 * @param confirmCallback the confirmation callback.
	 */
	public OpMove(List<FSTreeNode> nodes, FSTreeNode dest, IConfirmCallback confirmCallback) {
		super();
		this.nodes = getAncestors(nodes);
		this.dest = dest;
		this.confirmCallback = confirmCallback;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.Operation#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		super.run(monitor);
		// Remove its self from the clipped nodes.
		nodes.remove(dest);
		IChannel channel = null;
		try {
			if (!nodes.isEmpty()) {
				FSTreeNode head = nodes.get(0);
				channel = openChannel(head.peerNode.getPeer());
				if (channel != null) {
					IFileSystem service = getBlockingFileSystem(channel);
					if (service != null) {
						for (FSTreeNode node : nodes) {
							// Move each node.
							moveNode(service, node, dest);
						}
					}
					else {
						String message = NLS.bind(Messages.Operation_NoFileSystemError, head.peerNode.getPeerId());
						throw new TCFFileSystemException(IStatus.ERROR, message);
					}
				}
			}
		}
		catch (TCFException e) {
			throw new InvocationTargetException(e, e.getMessage());
		}
		finally {
			if (channel != null) Tcf.getChannelManager().closeChannel(channel);
			monitor.done();
		}
	}

	/**
	 * Move the file/folder to the destination folder using the specified file system service.
	 *
	 * @param monitor The monitor used to report the moving progress.
	 * @param service The file system service used to move the remote files.
	 * @param node The file/folder node to be moved.
	 * @param dest The destination folder.
	 * @throws TCFFileSystemException The exception thrown during moving.
	 * @throws InterruptedException Thrown when the operation is canceled.
	 */
	void moveNode(IFileSystem service, final FSTreeNode node, FSTreeNode dest) throws TCFFileSystemException, InterruptedException {
		if (monitor.isCanceled()) throw new InterruptedException();
		monitor.subTask(NLS.bind(Messages.OpMove_Moving, node.name));
		FSTreeNode copy = findChild(service, dest, node.name);
		if (copy == null || !copy.equals(node) && confirmReplace(node, confirmCallback)) {
			if (copy != null && copy.isDirectory() && node.isDirectory()) {
				List<FSTreeNode> children = getChildren(node, service);
				for (FSTreeNode child : children) {
					moveNode(service, child, copy);
				}
				removeFolder(node, service);
				monitor.worked(1);
			}
			else if (copy != null && copy.isFile() && node.isDirectory()) {
				String error = NLS.bind(Messages.OpMove_FileExistsError, copy.name);
				throw new TCFFileSystemException(IStatus.ERROR, error);
			}
			else if (copy != null && copy.isDirectory() && node.isFile()) {
				String error = NLS.bind(Messages.OpMove_FolderExistsError, copy.name);
				throw new TCFFileSystemException(IStatus.ERROR, error);
			}
			else {
				if (copy != null && copy.isFile() && node.isFile()) {
					removeFile(copy, service);
				}
				else if (copy == null) {
					copy = (FSTreeNode) node.clone();
				}
				addChild(service, dest, copy);
				String dst_path = copy.getLocation(true);
				String src_path = node.getLocation(true);
				final FSTreeNode copyNode = copy;
				final TCFFileSystemException[] errors = new TCFFileSystemException[1];
				service.rename(src_path, dst_path, new DoneRename() {
					@Override
					public void doneRename(IToken token, FileSystemException error) {
						if (error != null) {
							String message = NLS.bind(Messages.OpMove_CannotMove, node.name, error);
							errors[0] = new TCFFileSystemException(IStatus.ERROR, message, error);
						}
						else {
							cleanUpNode(node, copyNode);
						}
					}
				});
				if (errors[0] != null) {
					removeChild(service, dest, copy);
					throw errors[0];
				}
				monitor.worked(1);
			}
		}
	}

	/**
	 * Clean up the node after successful moving.
	 *
	 * @param node The node being moved.
	 * @param copyNode The target node that is moved to.
	 */
	void cleanUpNode(FSTreeNode node, FSTreeNode copyNode) {
		if (node.isFile()) {
			super.cleanUpFile(node);
		}
		else if (node.isDirectory()) {
			super.cleanUpFolder(node);
			List<FSTreeNode> children = node.getChildren();
			copyNode.addChidren(children);
			for (FSTreeNode child : children) {
				child.setParent(copyNode);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IOperation#getName()
	 */
	@Override
    public String getName() {
	    return Messages.OpMove_MovingFile;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IOperation#getTotalWork()
	 */
	@Override
    public int getTotalWork() {
	    return nodes == null ? IProgressMonitor.UNKNOWN : nodes.size();
    }
}
