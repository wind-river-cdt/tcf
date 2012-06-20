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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.IFileSystem;
import org.eclipse.tcf.services.IFileSystem.DoneStat;
import org.eclipse.tcf.services.IFileSystem.FileAttrs;
import org.eclipse.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.exceptions.TCFException;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.exceptions.TCFFileSystemException;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.core.nls.Messages;

/**
 * The base operation class for creating a file or a folder in the file system of Target
 * Explorer.
 */
public abstract class OpCreate extends Operation {
	// The folder in which a file/folder is going to be created.
	protected FSTreeNode folder;
	// The node that is created after the operation.
	protected FSTreeNode node;
	// The name of the node to be created.
	protected String name;

	/**
	 * Create an FSCreate instance with the specified folder and the name of the new node.
	 *
	 * @param folder The folder in which the new node is going to be created.
	 * @param name The new node's name.
	 */
	public OpCreate(FSTreeNode folder, String name) {
		this.folder = folder;
		this.name = name;
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
			channel = openChannel(folder.peerNode.getPeer());
			monitor.worked(1);
			IFileSystem service = getBlockingFileSystem(channel);
			if (service != null) {
				if (!folder.childrenQueried) {
					// If the children of folder is not queried, load it first.
					loadChildren(folder, service);
					monitor.worked(1);
				}
				monitor.worked(1);
				create(service);
				monitor.worked(1);
				addNode(service);
				monitor.worked(1);
				refresh(service);
				monitor.worked(1);
			}
			else {
				String message = NLS.bind(Messages.Operation_NoFileSystemError, folder.peerNode.getPeerId());
				throw new TCFFileSystemException(IStatus.ERROR, message);
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
	 * Refresh new node's stat using the file system service.
	 *
	 * @param service The file system service.
	 * @throws TCFFileSystemException Thrown when refreshing the new node's stat.
	 */
	void refresh(final IFileSystem service) throws TCFFileSystemException {
		if (node != null) {
			final TCFFileSystemException[] errors = new TCFFileSystemException[1];
			String path = node.getLocation(true);
			service.stat(path, new DoneStat() {
				@Override
				public void doneStat(IToken token, FileSystemException error, FileAttrs attrs) {
					if (error == null) {
						if (node != null) node.setAttributes(attrs);
					}
					else {
						errors[0] = newTCFException(IStatus.WARNING, error);
					}
				}
			});
			if (errors[0] != null) {
				throw errors[0];
			}
		}
	}

	/**
	 * Add the new node to the folder and its FSModel.
	 *
	 * @param service The file system service to be used.
	 * @throws TCFFileSystemException Thrown when adding.
	 */
	void addNode(final IFileSystem service) throws TCFFileSystemException {
		if (Protocol.isDispatchThread()) {
			node = newTreeNode();
			folder.addChild(node);
		}
		else {
			final TCFFileSystemException[] errors = new TCFFileSystemException[1];
			Protocol.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					try {
						addNode(service);
					}
					catch (TCFFileSystemException e) {
						errors[0] = e;
					}
				}
			});
			if (errors[0] != null) throw errors[0];
		}
	}

	/**
	 * Create the new node, either a directory node or a file node.
	 *
	 * @return The new node.
	 */
	protected abstract FSTreeNode newTreeNode();

	/**
	 * Create the node in the target system.
	 *
	 * @param service The file system service used to create the new node.
	 * @throws TCFFileSystemException Thrown when creating the node.
	 */
	protected abstract void create(IFileSystem service) throws TCFFileSystemException;

	/**
	 * Get the node that is created by this operation.
	 *
	 * @return the node created.
	 */
	public FSTreeNode getNode() {
		return node;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IOperation#getName()
	 */
	@Override
    public String getName() {
	    return NLS.bind(Messages.OpCreate_TaskName, name);
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IOperation#getTotalWork()
	 */
	@Override
    public int getTotalWork() {
	    return folder.childrenQueried ? 5 : 6;
    }
}
