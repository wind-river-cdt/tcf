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

import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IFileSystem;
import org.eclipse.tcf.services.IFileSystem.DoneMkDir;
import org.eclipse.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.tcf.filesystem.internal.exceptions.TCFFileSystemException;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;

/**
 * The file operation class to create a folder in the file system of Target Explorer.
 */
public class FSCreateFolder extends FSCreate {

	/**
	 * Create an instance to create a folder with the name in the folder.
	 *
	 * @param folder The folder in which the new folder is to be created.
	 * @param name The name of the new folder.
	 * @param callback The callback
	 */
	public FSCreateFolder(FSTreeNode folder, String name, ICallback callback) {
		super(folder, name, callback);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.wizards.FSCreate#create(org.eclipse.tcf.services.IFileSystem)
	 */
	@Override
	protected void create(IFileSystem service) throws TCFFileSystemException {
		String path = folder.getLocation(true);
		if (!path.endsWith("/")) path += "/"; //$NON-NLS-1$ //$NON-NLS-2$
		path += name;
		final FileSystemException[] errors = new FileSystemException[1];
		service.mkdir(path, null, new DoneMkDir() {
			@Override
			public void doneMkDir(IToken token, FileSystemException error) {
				if (error != null) {
					errors[0] = error;
				}
			}
		});
		if (errors[0] != null) {
			TCFFileSystemException exception = new TCFFileSystemException(errors[0].toString());
			exception.initCause(errors[0]);
			throw exception;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSCreate#newTreeNode()
	 */
	@Override
	protected FSTreeNode newTreeNode() {
		return FSTreeNode.createFolderNode(name, folder);
	}
}
