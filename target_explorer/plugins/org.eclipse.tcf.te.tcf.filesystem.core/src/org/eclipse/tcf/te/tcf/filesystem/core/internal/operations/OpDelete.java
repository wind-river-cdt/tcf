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

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IFileSystem;
import org.eclipse.tcf.services.IFileSystem.DoneRemove;
import org.eclipse.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IConfirmCallback;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.exceptions.TCFException;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.exceptions.TCFFileSystemException;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.StateManager;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.core.nls.Messages;

/**
 * FSDelete deletes the selected FSTreeNode list.
 */
public class OpDelete extends Operation {
	//The nodes to be deleted.
	List<FSTreeNode> nodes;
	IConfirmCallback confirmCallback;

	/**
	 * Create a delete operation using the specified nodes.
	 *
	 * @param nodes The nodes to be deleted.
	 */
	public OpDelete(List<FSTreeNode> nodes, IConfirmCallback confirmCallback) {
		this.nodes = getTopNodes(nodes);
		this.confirmCallback = confirmCallback;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSOperation#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		FSTreeNode head = nodes.get(0);
		IChannel channel = null;
		try {
			channel = openChannel(head.peerNode.getPeer());
			if (channel != null) {
				IFileSystem service = getBlockingFileSystem(channel);
				if (service != null) {
					monitor.beginTask(Messages.FSDelete_PrepareToDelete, IProgressMonitor.UNKNOWN);
					monitor.worked(1);
					int count = count(service, nodes);
					monitor.beginTask(Messages.FSDelete_Deleting, count);
					for (FSTreeNode node : nodes) {
						remove(monitor, node, service);
					}
				}
				else {
					String message = NLS.bind(Messages.FSOperation_NoFileSystemError, head.peerNode.getPeerId());
					throw new TCFFileSystemException(message);
				}
			}
		}
		catch (TCFException e) {
			throw new InvocationTargetException(e, e.getLocalizedMessage());
		}
		finally {
			if (channel != null) Tcf.getChannelManager().closeChannel(channel);
			monitor.done();
		}
	}

	/**
	 * Delete the file/folder node using the file system service.
	 *
	 * @param monitor The monitor to report the progress.
	 * @param node The file/folder node to be deleted.
	 * @param service The file system service.
	 * @throws TCFFileSystemException The exception thrown during deleting.
	 * @throws InterruptedException Thrown when the operation is canceled.
	 */
	void remove(IProgressMonitor monitor, FSTreeNode node, IFileSystem service) throws TCFFileSystemException, InterruptedException {
		if (node.isFile()) {
			removeFile(monitor, node, service);
		}
		else if (node.isDirectory()) {
			removeFolder(monitor, node, service);
		}
	}

	/**
	 * Delete the folder node and its children using the file system service.
	 *
	 * @param monitor The monitor to report the progress.
	 * @param node The folder node to be deleted.
	 * @param service The file system service.
	 * @throws TCFFileSystemException The exception thrown during deleting.
	 * @throws InterruptedException Thrown when the operation is canceled.
	 */
	private void removeFolder(IProgressMonitor monitor, final FSTreeNode node, IFileSystem service) throws TCFFileSystemException, InterruptedException {
		List<FSTreeNode> children = getChildren(node, service);
		if (!children.isEmpty()) {
			for (FSTreeNode child : children) {
				// Delete each child node.
				remove(monitor, child, service);
			}
		}
		if (monitor.isCanceled()) throw new InterruptedException();
		monitor.subTask(NLS.bind(Messages.FSDelete_RemovingFileFolder, node.name));
		removeFolder(service, node);
		monitor.worked(1);
	}

	/**
	 * Delete the folder node using the file system service.
	 *
	 * @param monitor The monitor to report the progress.
	 * @param node The folder node to be deleted.
	 * @param service The file system service.
	 * @throws TCFFileSystemException The exception thrown during deleting.
	 * @throws InterruptedException Thrown when the operation is canceled.
	 */
	private void removeFolder(IFileSystem service, final FSTreeNode node) throws TCFFileSystemException {
		final TCFFileSystemException[] errors = new TCFFileSystemException[1];
		String path = node.getLocation(true);
		service.rmdir(path, new DoneRemove() {
			@Override
			public void doneRemove(IToken token, FileSystemException error) {
				if (error == null) {
					cleanUpFolder(node);
				}
				else {
					String message = NLS.bind(Messages.FSDelete_CannotRemoveFolder, node.name, error);
					errors[0] = new TCFFileSystemException(message, error);
				}
			}
		});
		if (errors[0] != null) {
			throw errors[0];
		}
	}

	/**
	 * Delete the file node using the file system service.
	 *
	 * @param monitor The monitor to report the progress.
	 * @param node The file node to be deleted.
	 * @param service The file system service.
	 * @throws TCFFileSystemException The exception thrown during deleting.
	 * @throws InterruptedException Thrown when the operation is canceled.
	 */
	private void removeFile(IProgressMonitor monitor, final FSTreeNode node, IFileSystem service) throws TCFFileSystemException, InterruptedException {
		if (monitor.isCanceled()) throw new InterruptedException();
		monitor.subTask(NLS.bind(Messages.FSDelete_RemovingFileFolder, node.name));
		// If the file is read only on windows or not writable on unix, then make it deletable.
		if (confirmCallback != null && confirmCallback.requires(node)) {
			if (!yes2All) {
				int result = confirmCallback.confirms(node);
				if (result == 1) {
					yes2All = true;
				}
				else if (result == 2) {
					monitor.worked(1);
					return;
				}
				else if (result == 3) {
					// Cancel the whole operation
					monitor.setCanceled(true);
					throw new InterruptedException();
				}
			}
			final FSTreeNode clone = (FSTreeNode) node.clone();
			if (node.isWindowsNode()) {
				clone.setReadOnly(false);
			}
			else {
				clone.setWritable(true);
			}
			// Make the file writable.
			SafeRunner.run(new ISafeRunnable() {
				@Override
                public void handleException(Throwable e) {
					// Ignore exception
                }
				@Override
				public void run() throws Exception {
					StateManager.getInstance().setFileAttrs(node, clone.attr);
				}
			});
		}
		removeFile(node, service);
		monitor.worked(1);
	}
}
