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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IFileSystem;
import org.eclipse.tcf.services.IFileSystem.DoneRemove;
import org.eclipse.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.dialogs.TimeTriggeredProgressMonitorDialog;
import org.eclipse.tcf.te.tcf.filesystem.internal.ImageConsts;
import org.eclipse.tcf.te.tcf.filesystem.internal.exceptions.TCFException;
import org.eclipse.tcf.te.tcf.filesystem.internal.exceptions.TCFFileSystemException;
import org.eclipse.tcf.te.tcf.filesystem.internal.nls.Messages;
import org.eclipse.tcf.te.tcf.filesystem.internal.utils.StateManager;
import org.eclipse.tcf.te.tcf.filesystem.model.FSModel;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.ui.PlatformUI;

/**
 * FSDelete deletes the selected FSTreeNode list.
 */
public class FSDelete extends FSOperation {
	//The nodes to be deleted.
	List<FSTreeNode> nodes;

	/**
	 * Create a delete operation using the specified nodes.
	 *
	 * @param nodes The nodes to be deleted.
	 */
	public FSDelete(List<FSTreeNode> nodes) {
		this.nodes = getTopNodes(nodes);
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
							monitor.beginTask(Messages.FSDelete_PrepareToDelete, IProgressMonitor.UNKNOWN);
							monitor.worked(1);
							int count = count(service, nodes);
							monitor.beginTask(Messages.FSDelete_Deleting, count);
							for (FSTreeNode node : nodes) {
								remove(monitor, node, service);
							}
						}
						else {
							String message = NLS.bind(Messages.FSOperation_NoFileSystemError, head.peerNode.getPeer().getID());
							throw new TCFFileSystemException(message);
						}
					}
				}
				catch (TCFException e) {
					throw new InvocationTargetException(e);
				}
				finally {
					if (channel != null) Tcf.getChannelManager().closeChannel(channel);
					// Refresh the file system tree.
					FSModel.getFSModel(head.peerNode).fireNodeStateChanged(null);
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
			// Display the error message during deleting.
			Throwable throwable = e.getTargetException() != null ? e.getTargetException() : e;
			MessageDialog.openError(parent, Messages.FSDelete_DeleteFileFolderTitle, throwable.getLocalizedMessage());
		}
		catch (InterruptedException e) {
			// It is canceled.
		}
		return true;
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
		List<FSTreeNode> children = new ArrayList<FSTreeNode>(getChildren(node, service));
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
		if (node.isWindowsNode() && node.isReadOnly() || !node.isWindowsNode() && !node.isWritable()) {
			if (!yes2All) {
				int result = confirmDelete(node);
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
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					StateManager.getInstance().setFileAttrs(node, clone.attr);
				}
			});
		}
		removeFile(node, service);
		monitor.worked(1);
	}

	/**
	 * Confirm deleting the read only file.
	 *
	 * @param node The read only file node.
	 * @return The confirming result, 0-yes, 1-yes to all, 2-no, 3-cancel.
	 */
	private int confirmDelete(final FSTreeNode node) {
		final int[] results = new int[1];
		Display display = PlatformUI.getWorkbench().getDisplay();
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				String title = Messages.FSDelete_ConfirmDelete;
				String message = NLS.bind(Messages.FSDelete_ConfirmMessage, node.name);
				final Image titleImage = UIPlugin.getImage(ImageConsts.DELETE_READONLY_CONFIRM);
				MessageDialog qDialog = new MessageDialog(parent, title, null, message, MessageDialog.QUESTION, new String[] { Messages.FSDelete_ButtonYes, Messages.FSDelete_ButtonYes2All, Messages.FSDelete_ButtonNo, Messages.FSDelete_ButtonCancel }, 0) {
					@Override
					public Image getQuestionImage() {
						return titleImage;
					}
				};
				results[0] = qDialog.open();
			}
		});
		return results[0];
	}
}
