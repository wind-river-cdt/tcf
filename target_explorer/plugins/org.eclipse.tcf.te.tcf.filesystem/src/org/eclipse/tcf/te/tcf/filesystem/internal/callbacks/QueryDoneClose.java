/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.callbacks;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IFileSystem.DoneClose;
import org.eclipse.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;

/**
 * The callback handler that handles the event when a directory is closed.
 */
public class QueryDoneClose implements DoneClose {
	// The tcf channel.
	IChannel channel;
	// The parent node being queried.
	FSTreeNode parentNode;
	// The callback object.
	ICallback callback;

	/**
	 * Constructor
	 * 
	 * @param callback The callback object.
	 * @param channel The channel to close.
	 * @param parentNode The parent node
	 */
	public QueryDoneClose(ICallback callback, IChannel channel, FSTreeNode parentNode) {
		this.callback = callback;
		this.channel = channel;
		this.parentNode = parentNode;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.IFileSystem.DoneClose#doneClose(org.eclipse.tcf.protocol.IToken, org.eclipse.tcf.services.IFileSystem.FileSystemException)
	 */
	@Override
	public void doneClose(IToken token, FileSystemException error) {
		if(callback != null) {
			IStatus status = error == null ? Status.OK_STATUS : new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), error.getLocalizedMessage(), error);
			callback.done(this, status);
		}
	}
}
