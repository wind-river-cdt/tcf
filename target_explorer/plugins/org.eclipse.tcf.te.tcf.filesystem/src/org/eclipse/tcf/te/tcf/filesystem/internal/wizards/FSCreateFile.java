/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.wizards;

import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IFileSystem;
import org.eclipse.tcf.services.IFileSystem.DoneClose;
import org.eclipse.tcf.services.IFileSystem.DoneOpen;
import org.eclipse.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tcf.services.IFileSystem.IFileHandle;
import org.eclipse.tcf.te.tcf.filesystem.internal.exceptions.TCFFileSystemException;
import org.eclipse.tcf.te.tcf.filesystem.internal.nls.Messages;
import org.eclipse.tcf.te.tcf.filesystem.internal.url.Rendezvous;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;

/**
 * The file operation class to create a file in the file system of Target Explorer.
 */
public class FSCreateFile extends FSCreate {

	/**
	 * Create an instance to create a file with the name in the folder.
	 * 
	 * @param folder The folder in which the file is to be created.
	 * @param name The new file's name.
	 */
	public FSCreateFile(FSTreeNode folder, String name) {
		super(folder, name);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.wizards.FSCreate#create(org.eclipse.tcf.services.IFileSystem)
	 */
	@Override
	protected void create(IFileSystem service) throws TCFFileSystemException {
		String path = folder.getLocation(true);
		if (!path.endsWith("/")) path += "/"; //$NON-NLS-1$//$NON-NLS-2$
		path += name;
		final Rendezvous rendezvous = new Rendezvous();
		final FileSystemException[] errors = new FileSystemException[1];
		// Open the file.
		final IFileHandle[] handles = new IFileHandle[1];
		service.open(path, IFileSystem.TCF_O_WRITE | IFileSystem.TCF_O_CREAT | IFileSystem.TCF_O_TRUNC, null, new DoneOpen() {
			@Override
			public void doneOpen(IToken token, FileSystemException error, IFileHandle hdl) {
				errors[0] = error;
				handles[0] = hdl;
				rendezvous.arrive();
			}
		});
		try {
			rendezvous.waiting(5000L);
		}
		catch (InterruptedException e) {
			throw new TCFFileSystemException(Messages.TcfURLConnection_OpenFileTimeout);
		}
		if (errors[0] != null) {
			TCFFileSystemException exception = new TCFFileSystemException(errors[0].toString());
			exception.initCause(errors[0]);
			throw exception;
		}
		if (handles[0] == null) {
			throw new TCFFileSystemException(Messages.TcfURLConnection_NoFileHandleReturned);
		}
		rendezvous.reset();
		service.close(handles[0], new DoneClose() {
			@Override
			public void doneClose(IToken token, FileSystemException error) {
				rendezvous.arrive();
			}
		});
		try {
			rendezvous.waiting(5000L);
		}
		catch (InterruptedException e) {
			throw new TCFFileSystemException(Messages.TcfURLConnection_CloseFileTimeout);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.wizards.FSCreate#getNodeType()
	 */
	@Override
	protected String getNodeType() {
		return "FSFileNode"; //$NON-NLS-1$
	}
}
