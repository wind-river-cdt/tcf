/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
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
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IFileSystem.DirEntry;
import org.eclipse.tcf.services.IFileSystem.DoneRoots;
import org.eclipse.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.tcf.filesystem.core.activator.CorePlugin;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;

/**
 * The callback handler that handles the event when the roots are listed.
 */
public class QueryDoneRoots extends CallbackBase implements DoneRoots {
	// The parent directory node.
	FSTreeNode parentNode;
	// The callback object
	ICallback callback;

	/**
	 * Create an instance with parameters to initialize the fields.
	 * 
	 * @param callback the callback.
	 * @param parentNode The parent directory node.
	 */
	public QueryDoneRoots(ICallback callback, FSTreeNode parentNode) {
		this.callback = callback;
		this.parentNode = parentNode;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.IFileSystem.DoneRoots#doneRoots(org.eclipse.tcf.protocol.IToken, org.eclipse.tcf.services.IFileSystem.FileSystemException, org.eclipse.tcf.services.IFileSystem.DirEntry[])
	 */
	@Override
	public void doneRoots(IToken token, FileSystemException error, DirEntry[] entries) {
		if (error == null) {
			if (entries.length > 0) {
				for (DirEntry entry : entries) {
					FSTreeNode node = new FSTreeNode(parentNode, entry, true);
					parentNode.addChild(node);
				}
			}
			else {
				parentNode.clearChildren();
			}
		}
		if(callback != null) {
			IStatus status = error == null ? Status.OK_STATUS : new Status(IStatus.ERROR, CorePlugin.getUniqueIdentifier(), getErrorMessage(error), error);
			callback.done(this, status);
		}
	}
}
