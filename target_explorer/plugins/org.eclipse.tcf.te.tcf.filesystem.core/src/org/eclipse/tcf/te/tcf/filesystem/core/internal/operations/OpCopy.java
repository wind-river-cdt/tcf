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
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IFileSystem;
import org.eclipse.tcf.services.IFileSystem.DoneCopy;
import org.eclipse.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IConfirmCallback;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.exceptions.TCFException;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.exceptions.TCFFileSystemException;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.core.nls.Messages;

/**
 * The operation class that copies selected FSTreeNodes to a specify destination folder.
 */
public class OpCopy extends Operation {
	// The nodes to be copied.
	List<FSTreeNode> nodes;
	// The destination folder to be copied to.
	FSTreeNode dest;
	// The callback invoked to confirm overwriting when there're files with same names.
	IConfirmCallback confirmCallback;
	// If it is required to copy the permissions.
	boolean cpPermission;
	// If it is required to copy the ownership.
	boolean cpOwnership;
	
	/**
	 * Create a copy operation using the specified nodes and destination folder.
	 * 
	 * @param nodes The file/folder nodes to be copied.
	 * @param dest The destination folder to be copied to.
	 */
	public OpCopy(List<FSTreeNode> nodes, FSTreeNode dest) {
		this(nodes, dest, false, false, null);
	}

	/**
	 * Create a copy operation using the specified nodes and destination folder,
	 * using the specified flags of copying permissions and ownership and a callback
	 * to confirm to overwrite existing files.
	 *
	 * @param nodes The file/folder nodes to be copied.
	 * @param dest The destination folder to be copied to.
	 */
	public OpCopy(List<FSTreeNode> nodes, FSTreeNode dest, boolean cpPerm, boolean cpOwn, IConfirmCallback confirmCallback) {
		super();
		this.nodes = getAncestors(nodes);
		this.dest = dest;
		this.cpOwnership = cpOwn;
		this.cpPermission = cpPerm;
		this.confirmCallback = confirmCallback;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.Operation#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		super.run(monitor);
		FSTreeNode head = nodes.get(0);
		IChannel channel = null;
		try {
			channel = openChannel(head.peerNode.getPeer());
			if (channel != null) {
				IFileSystem service = getBlockingFileSystem(channel);
				if (service != null) {
					for (FSTreeNode node : nodes) {
						// Iterate the nodes and copy each of them to the destination folder.
						copyNode(service, node, dest);
					}
				}
				else {
					String message = NLS.bind(Messages.Operation_NoFileSystemError, head.peerNode.getPeerId());
					throw new TCFFileSystemException(IStatus.ERROR, message);
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
	 * Copy the file/folder represented by the specified node to the destination folder.
	 *
	 * @param service The file system service to do the remote copying.
	 * @param node The file/folder node to be copied.
	 * @param dest The destination folder.
	 * @throws TCFFileSystemException The exception thrown during copying
	 * @throws InterruptedException The exception thrown when the operation is canceled.
	 */
	void copyNode(IFileSystem service, FSTreeNode node, FSTreeNode dest) throws TCFFileSystemException, InterruptedException {
		if (node.isFile()) {
			copyFile(service, node, dest);
		}
		else if (node.isDirectory()) {
			copyFolder(service, node, dest);
		}
	}

	/**
	 * Copy the folder represented by the specified node to the destination folder.
	 *
	 * @param service The file system service to do the remote copying.
	 * @param node The folder node to be copied.
	 * @param dest The destination folder.
	 * @throws TCFFileSystemException The exception thrown during copying
	 * @throws InterruptedException The exception thrown when the operation is canceled.
	 */
	private void copyFolder(IFileSystem service, FSTreeNode node, FSTreeNode dest) throws TCFFileSystemException, InterruptedException {
		if (monitor.isCanceled()) throw new InterruptedException();
		FSTreeNode copy = findChild(service, dest, node.name);
		if (copy == null) {
			// If no existing directory with the same name, create it.
			copy = (FSTreeNode) node.clone();
			addChild(service, dest, copy);
			mkdir(service, copy);
			copyChildren(service, node, copy);
		}
		else if (node.equals(copy)) {
			copy = createCopyDestination(service, node, dest);
			mkdir(service, copy);
			copyChildren(service, node, copy);
		}
		else if (confirmReplace(node, confirmCallback)) {
			copyChildren(service, node, copy);
		}
		monitor.worked(1);
	}

	/**
	 * Copy the children of the node to the destination folder.
	 *
	 * @param service The file system service to do the remote copying.
	 * @param node The folder node to be copied.
	 * @param dest The destination folder.
	 * @throws TCFFileSystemException The exception thrown during copying
	 * @throws InterruptedException The exception thrown when the operation is canceled.
	 */
	private void copyChildren(IFileSystem service, FSTreeNode node, FSTreeNode dest) throws TCFFileSystemException, InterruptedException {
	    List<FSTreeNode> children = getChildren(node, service);
	    if (!children.isEmpty()) {
	    	for (FSTreeNode child : children) {
	    		// Iterate and copy its children nodes.
	    		copyNode(service, child, dest);
	    	}
	    }
    }

	/**
	 * Copy the file represented by the specified node to the destination folder.
	 *
	 * @param service The file system service to do the remote copying.
	 * @param node The file node to be copied.
	 * @param dest The destination folder.
	 * @throws TCFFileSystemException The exception thrown during copying
	 * @throws InterruptedException The exception thrown when the operation is canceled.
	 */
	private void copyFile(IFileSystem service, FSTreeNode node, FSTreeNode dest) throws TCFFileSystemException, InterruptedException {
		if (monitor.isCanceled()) throw new InterruptedException();
		monitor.subTask(NLS.bind(Messages.OpCopy_Copying, node.name));
		// Create the copy target file
		final FSTreeNode copy = createCopyDestination(service, node, dest);
		String src_path = node.getLocation(true);
		String dst_path = copy.getLocation(true);
		final TCFFileSystemException[] errors = new TCFFileSystemException[1];
		// Get the options of copy permission and ownership.
		service.copy(src_path, dst_path, cpPermission, cpOwnership, new DoneCopy() {
			@Override
			public void doneCopy(IToken token, FileSystemException error) {
				if (error != null) {
					String message = NLS.bind(Messages.OpCopy_CannotCopyFile, copy.name, error);
					errors[0] = new TCFFileSystemException(IStatus.ERROR, message, error);
				}
			}
		});
		if (errors[0] != null) {
			removeChild(service, dest, copy);
			throw errors[0];
		}
		monitor.worked(1);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IOperation#getName()
	 */
	@Override
    public String getName() {
	    return Messages.OpCopy_CopyingFile;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IOperation#getTotalWork()
	 */
	@Override
    public int getTotalWork() {
		if(nodes != null && !nodes.isEmpty()) {
			final AtomicReference<Integer> ref = new AtomicReference<Integer>();
			SafeRunner.run(new ISafeRunnable(){
				@Override
                public void handleException(Throwable exception) {
					// Ignore on purpose.
                }
				@Override
                public void run() throws Exception {
					FSTreeNode head = nodes.get(0);
					IChannel channel = null;
					try {
						channel = openChannel(head.peerNode.getPeer());
						if (channel != null) {
							IFileSystem service = getBlockingFileSystem(channel);
							if (service != null) {
								ref.set(Integer.valueOf(count(service, nodes)));
							}
							else {
								String message = NLS.bind(Messages.Operation_NoFileSystemError, head.peerNode.getPeerId());
								throw new TCFFileSystemException(IStatus.ERROR, message);
							}
						}
					}
					finally {
						if (channel != null) Tcf.getChannelManager().closeChannel(channel);
					}
                }});
			Integer value = ref.get();
			return value == null ? IProgressMonitor.UNKNOWN : value.intValue();
		}
		return IProgressMonitor.UNKNOWN;
    }
}
