/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.core.internal.callbacks;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IFileSystem;
import org.eclipse.tcf.services.IFileSystem.DoneOpen;
import org.eclipse.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tcf.services.IFileSystem.IFileHandle;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.tcf.filesystem.core.activator.CorePlugin;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;

/**
 * The callback handler that handles the event that a directory is opened.
 */
public class QueryDoneOpen extends CallbackBase implements DoneOpen {
	// The tcf channel used.
	IChannel channel;
	// The file system service.
	IFileSystem service;
	// The parent node being queried.
	FSTreeNode parentNode;
	// The callback
	ICallback callback;

	/**
	 * Create an instance with parameters to initialize the fields.
	 * 
	 * @param callback the callback
	 * @param channel The tcf channel.
	 * @param service The file system service.
	 * @param node The parent node.
	 */
	public QueryDoneOpen(ICallback callback, IChannel channel, IFileSystem service, FSTreeNode node) {
		this.callback = callback;
		this.channel = channel;
		this.service = service;
		this.parentNode = node;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.IFileSystem.DoneOpen#doneOpen(org.eclipse.tcf.protocol.IToken, org.eclipse.tcf.services.IFileSystem.FileSystemException, org.eclipse.tcf.services.IFileSystem.IFileHandle)
	 */
	@Override
	public void doneOpen(IToken token, FileSystemException error, IFileHandle handle) {
		if (error == null) {
			// Read the directory content until finished
			service.readdir(handle, new QueryDoneReadDir(callback, channel, service, handle, parentNode));
		}
		else if (callback != null) {
			IStatus status = new Status(IStatus.ERROR, CorePlugin.getUniqueIdentifier(), getErrorMessage(error), error);
			callback.done(this, status);
		}
	}
}
