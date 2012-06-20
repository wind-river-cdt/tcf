/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
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
import org.eclipse.tcf.services.IFileSystem;
import org.eclipse.tcf.services.IFileSystem.DoneSetStat;
import org.eclipse.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.exceptions.TCFException;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.exceptions.TCFFileSystemException;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.core.nls.Messages;
/**
 * The operation implementation to commit the new attributes to
 * the file system node.
 */
public class OpCommitAttr extends Operation {
	// The node whose attributes are being updated.
	FSTreeNode node;
	// The new attributes for the file system node.
	IFileSystem.FileAttrs attrs;
	
	/**
	 * Create an instance
	 */
	public OpCommitAttr(FSTreeNode node, IFileSystem.FileAttrs attrs) {
		this.node = node;
		this.attrs = attrs;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.Operation#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
	    IChannel channel = null;
		try {
			channel = openChannel(node.peerNode.getPeer());
			if (channel != null) {
				IFileSystem service = Operation.getBlockingFileSystem(channel);
				if (service != null) {
					final TCFFileSystemException[] errors = new TCFFileSystemException[1];
					String path = node.getLocation(true);
					service.setstat(path, attrs, new DoneSetStat() {
						@Override
						public void doneSetStat(IToken token, FileSystemException error) {
							if (error == null) {
								node.setAttributes(attrs);
							} else {
								errors[0] = newTCFException(IStatus.WARNING, error);
							}
						}
					});
					if (errors[0] != null) {
						throw errors[0];
					}
				} else {
					String message = NLS.bind(Messages.Operation_NoFileSystemError, node.peerNode.getPeerId());
					throw new TCFFileSystemException(IStatus.ERROR, message);
				}
			}
		}
		catch(TCFException e) {
			throw new InvocationTargetException(e);
		}
		finally {
			if (channel != null) Tcf.getChannelManager().closeChannel(channel);
		}
	}
}
