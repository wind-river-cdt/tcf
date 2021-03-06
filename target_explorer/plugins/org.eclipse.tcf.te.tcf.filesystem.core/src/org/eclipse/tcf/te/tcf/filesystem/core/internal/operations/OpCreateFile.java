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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IFileSystem;
import org.eclipse.tcf.services.IFileSystem.DoneClose;
import org.eclipse.tcf.services.IFileSystem.DoneOpen;
import org.eclipse.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tcf.services.IFileSystem.IFileHandle;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.exceptions.TCFFileSystemException;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSModel;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.core.nls.Messages;

/**
 * The file operation class to create a file in the file system of Target Explorer.
 */
public class OpCreateFile extends OpCreate {

	/**
	 * Create an instance to create a file with the name in the folder.
	 *
	 * @param folder The folder in which the file is to be created.
	 * @param name The new file's name.
	 */
	public OpCreateFile(FSTreeNode folder, String name) {
		super(folder, name);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpCreate#create(org.eclipse.tcf.services.IFileSystem)
	 */
	@Override
	protected void create(IFileSystem service) throws TCFFileSystemException {
		String path = folder.getLocation(true);
		if (!path.endsWith("/")) path += "/"; //$NON-NLS-1$ //$NON-NLS-2$
		path += name;
		final FileSystemException[] errors = new FileSystemException[1];
		// Open the file.
		final IFileHandle[] handles = new IFileHandle[1];
		service.open(path, IFileSystem.TCF_O_WRITE | IFileSystem.TCF_O_CREAT | IFileSystem.TCF_O_TRUNC, null, new DoneOpen() {
			@Override
			public void doneOpen(IToken token, FileSystemException error, IFileHandle hdl) {
				errors[0] = error;
				handles[0] = hdl;
			}
		});
		if (errors[0] != null) {
			TCFFileSystemException exception = new TCFFileSystemException(IStatus.ERROR, errors[0].toString());
			exception.initCause(errors[0]);
			throw exception;
		}
		if (handles[0] == null) {
			throw new TCFFileSystemException(IStatus.ERROR, Messages.TcfURLConnection_NoFileHandleReturned);
		}
		service.close(handles[0], new DoneClose() {
			@Override
			public void doneClose(IToken token, FileSystemException error) {
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpCreate#newTreeNode()
	 */
	@Override
	protected FSTreeNode newTreeNode() {
		return FSModel.createFileNode(name, folder);
	}
}
