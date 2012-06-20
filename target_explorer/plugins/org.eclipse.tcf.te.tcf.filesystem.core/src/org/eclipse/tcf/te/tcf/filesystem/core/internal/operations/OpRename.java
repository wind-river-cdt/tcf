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

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IFileSystem;
import org.eclipse.tcf.services.IFileSystem.DoneRename;
import org.eclipse.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.exceptions.TCFException;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.exceptions.TCFFileSystemException;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.CacheManager;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.PersistenceManager;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.core.nls.Messages;
/**
 * FSRename renames the specified file/folder to a
 * new name.
 *
 */
public class OpRename extends Operation {
	// The file/folder node to be renamed.
	FSTreeNode node;
	// The new name the file/folder is renamed to.
	String newName;

	/**
	 * Create a rename operation that renames the node with the new name.
	 *
	 * @param node The file/folder node to be renamed.
	 * @param newName The new name of this node.
	 */
	public OpRename(FSTreeNode node, String newName) {
		this.node = node;
		this.newName = newName;
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
			channel = openChannel(node.peerNode.getPeer());
			if (channel != null) {
				IFileSystem service = getBlockingFileSystem(channel);
				if (service != null) {
					renameNode(service);
				}
				else {
					String message = NLS.bind(Messages.Operation_NoFileSystemError, node.peerNode.getPeerId());
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
	 * Rename the node using the new name.
	 *
	 * @param service File system service used to rename.
	 * @throws TCFFileSystemException The exception thrown during renaming.
	 */
	void renameNode(IFileSystem service) throws TCFFileSystemException {
		String src_path = node.getLocation(true);
		String oldName = node.name;
		node.name = newName;
		String dst_path = node.getLocation(true);
		node.name = oldName;
		final TCFFileSystemException[] errors = new TCFFileSystemException[1];
		service.rename(src_path, dst_path, new DoneRename() {
			@Override
			public void doneRename(IToken token, FileSystemException error) {
				if (error != null) {
					String message = NLS.bind(Messages.OpRename_CannotRename, node.name, error);
					errors[0] = new TCFFileSystemException(IStatus.ERROR, message, error);
				}
				else {
					final File file = CacheManager.getCacheFile(node);
					if (node.isFile() && file.exists()) {
						PersistenceManager.getInstance().removeFileDigest(node.getLocationURI());
					}
					deleteFileChecked(file);
					node.setName(newName);
				}
			}
		});
		monitor.worked(1);
		if (errors[0] != null) {
			throw errors[0];
		}
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IOperation#getName()
	 */
	@Override
    public String getName() {
	    return Messages.OpRename_TitleRename;
    }
}
