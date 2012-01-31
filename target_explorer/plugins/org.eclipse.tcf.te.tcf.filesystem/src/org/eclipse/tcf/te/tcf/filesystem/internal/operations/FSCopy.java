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

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IFileSystem;
import org.eclipse.tcf.services.IFileSystem.DoneCopy;
import org.eclipse.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.filesystem.dialogs.TimeTriggeredProgressMonitorDialog;
import org.eclipse.tcf.te.tcf.filesystem.internal.exceptions.TCFException;
import org.eclipse.tcf.te.tcf.filesystem.internal.exceptions.TCFFileSystemException;
import org.eclipse.tcf.te.tcf.filesystem.internal.utils.PersistenceManager;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.nls.Messages;
import org.eclipse.ui.PlatformUI;

/**
 * FSCopy copies selected FSTreeNodes to a specify destination folder.
 */
public class FSCopy extends FSOperation {
	// The nodes to be copied.
	List<FSTreeNode> nodes;
	// The destination folder to be copied to.
	FSTreeNode dest;

	/**
	 * Create a copy operation using the specified nodes and destination folder.
	 *
	 * @param nodes The file/folder nodes to be copied.
	 * @param dest The destination folder to be copied to.
	 */
	public FSCopy(List<FSTreeNode> nodes, FSTreeNode dest) {
		this.nodes = getTopNodes(nodes);
		this.dest = dest;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSOperation#doit()
	 */
	@Override
	public boolean doit() {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				FSTreeNode head = nodes.get(0);
				IChannel channel = null;
				try {
					channel = openChannel(head.peerNode.getPeer());
					if (channel != null) {
						IFileSystem service = getBlockingFileSystem(channel);
						if (service != null) {
							monitor.beginTask(Messages.FSCopy_PrepareToCopy, IProgressMonitor.UNKNOWN);
							monitor.worked(1);
							int count = count(service, nodes);
							monitor.beginTask(Messages.FSCopy_CopyingFile, count);
							for (FSTreeNode node : nodes) {
								// Iterate the nodes and copy each of them to the destination
								// folder.
								copyNode(monitor, service, node, dest);
							}
						}
						else {
							String message = NLS.bind(Messages.FSOperation_NoFileSystemError, head.peerNode.getPeerId());
							throw new TCFFileSystemException(message);
						}
					}
				}
				catch (TCFException e) {
					throw new InvocationTargetException(e);
				}
				finally {
					if (channel != null) Tcf.getChannelManager().closeChannel(channel);
					monitor.done();
				}
			}
		};
		Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		TimeTriggeredProgressMonitorDialog dialog = new TimeTriggeredProgressMonitorDialog(parent, 250);
		dialog.setCancelable(true);
		try {
			dialog.run(true, true, runnable);
		}
		catch (InvocationTargetException e) {
			// Display the error during copy.
			Throwable throwable = e.getTargetException() != null ? e.getTargetException() : e;
			MessageDialog.openError(parent, Messages.FSCopy_CopyFileFolderTitle, throwable.getLocalizedMessage());
		}
		catch (InterruptedException e) {
			// It is canceled.
		}
		return true;
	}

	/**
	 * Copy the file/folder represented by the specified node to the destination folder.
	 *
	 * @param monitor The monitor to report the progress.
	 * @param service The file system service to do the remote copying.
	 * @param node The file/folder node to be copied.
	 * @param dest The destination folder.
	 * @throws TCFFileSystemException The exception thrown during copying
	 * @throws InterruptedException The exception thrown when the operation is canceled.
	 */
	void copyNode(IProgressMonitor monitor, IFileSystem service, FSTreeNode node, FSTreeNode dest) throws TCFFileSystemException, InterruptedException {
		if (node.isFile()) {
			copyFile(monitor, service, node, dest);
		}
		else if (node.isDirectory()) {
			copyFolder(monitor, service, node, dest);
		}
	}

	/**
	 * Copy the folder represented by the specified node to the destination folder.
	 *
	 * @param monitor The monitor to report the progress.
	 * @param service The file system service to do the remote copying.
	 * @param node The folder node to be copied.
	 * @param dest The destination folder.
	 * @throws TCFFileSystemException The exception thrown during copying
	 * @throws InterruptedException The exception thrown when the operation is canceled.
	 */
	private void copyFolder(IProgressMonitor monitor, IFileSystem service, FSTreeNode node, FSTreeNode dest) throws TCFFileSystemException, InterruptedException {
		if (monitor.isCanceled()) throw new InterruptedException();
		FSTreeNode copy = findChild(service, dest, node.name);
		if (copy == null) {
			// If no existing directory with the same name, create it.
			copy = (FSTreeNode) node.clone();
			addChild(service, dest, copy);
			mkdir(service, copy);
			copyChildren(monitor, service, node, copy);
		}
		else if (node == copy) {
			copy = createCopyDestination(service, node, dest);
			mkdir(service, copy);
			copyChildren(monitor, service, node, copy);
		}
		else if (confirmReplace(node)) {
			copyChildren(monitor, service, node, copy);
		}
		monitor.worked(1);
	}

	/**
	 * Copy the children of the node to the destination folder.
	 *
	 * @param monitor The monitor to report the progress.
	 * @param service The file system service to do the remote copying.
	 * @param node The folder node to be copied.
	 * @param dest The destination folder.
	 * @throws TCFFileSystemException The exception thrown during copying
	 * @throws InterruptedException The exception thrown when the operation is canceled.
	 */
	private void copyChildren(IProgressMonitor monitor, IFileSystem service, FSTreeNode node, FSTreeNode dest) throws TCFFileSystemException, InterruptedException {
	    List<FSTreeNode> children = getChildren(node, service);
	    if (!children.isEmpty()) {
	    	for (FSTreeNode child : children) {
	    		// Iterate and copy its children nodes.
	    		copyNode(monitor, service, child, dest);
	    	}
	    }
    }

	/**
	 * Copy the file represented by the specified node to the destination folder.
	 *
	 * @param monitor The monitor to report the progress.
	 * @param service The file system service to do the remote copying.
	 * @param node The file node to be copied.
	 * @param dest The destination folder.
	 * @throws TCFFileSystemException The exception thrown during copying
	 * @throws InterruptedException The exception thrown when the operation is canceled.
	 */
	private void copyFile(IProgressMonitor monitor, IFileSystem service, FSTreeNode node, FSTreeNode dest) throws TCFFileSystemException, InterruptedException {
		if (monitor.isCanceled()) throw new InterruptedException();
		monitor.subTask(NLS.bind(Messages.FSCopy_Copying, node.name));
		// Create the copy target file
		final FSTreeNode copy = createCopyDestination(service, node, dest);
		String src_path = node.getLocation(true);
		String dst_path = copy.getLocation(true);
		final TCFFileSystemException[] errors = new TCFFileSystemException[1];
		// Get the options of copy permission and ownership.
		boolean copyPermission = PersistenceManager.getInstance().isCopyPermission();
		boolean copyOwnership = PersistenceManager.getInstance().isCopyOwnership();
		service.copy(src_path, dst_path, copyPermission, copyOwnership, new DoneCopy() {
			@Override
			public void doneCopy(IToken token, FileSystemException error) {
				if (error != null) {
					String message = NLS.bind(Messages.FSCopy_CannotCopyFile, copy.name, error);
					errors[0] = new TCFFileSystemException(message, error);
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
