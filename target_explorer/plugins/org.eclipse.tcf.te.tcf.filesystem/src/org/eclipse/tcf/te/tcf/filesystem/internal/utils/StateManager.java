/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * William Chen (Wind River)- [345552] Edit the remote files with a proper editor
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.utils;

import java.io.File;

import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IFileSystem;
import org.eclipse.tcf.services.IFileSystem.DoneSetStat;
import org.eclipse.tcf.services.IFileSystem.DoneStat;
import org.eclipse.tcf.services.IFileSystem.FileAttrs;
import org.eclipse.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.filesystem.internal.exceptions.TCFException;
import org.eclipse.tcf.te.tcf.filesystem.internal.exceptions.TCFFileSystemException;
import org.eclipse.tcf.te.tcf.filesystem.internal.nls.Messages;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSOperation;
import org.eclipse.tcf.te.tcf.filesystem.model.CacheState;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;

/**
 * This class provides several utility methods to get, update, commit
 * or refresh a file node's state.
 *
 */
public class StateManager {

	// The singleton instance.
	private static StateManager instance;

	/**
	 * Get the singleton user manager.
	 *
	 * @return The singleton cache manager.
	 */
	public static StateManager getInstance() {
		if (instance == null) {
			instance = new StateManager();
		}
		return instance;
	}

	/**
	 * Create a StateManager fInstance.
	 */
	private StateManager() {
	}

	/**
	 * Refresh the state of the specified node.
	 *
	 * @param node The tree node whose state is going to be refreshed.
	 * @throws TCFException
	 */
	public void refreshState(final FSTreeNode node) throws TCFException {
		IChannel channel = null;
		try {
			channel = FSOperation.openChannel(node.peerNode.getPeer());
			if (channel != null) {
				IFileSystem service = FSOperation.getBlockingFileSystem(channel);
				if (service != null) {
					final TCFFileSystemException[] errors = new TCFFileSystemException[1];
					String path = node.getLocation(true);
					service.stat(path, new DoneStat() {
						@Override
						public void doneStat(IToken token, FileSystemException error, FileAttrs attrs) {
							if (error == null) {
								node.attr = attrs;
							} else {
								String message = NLS.bind(Messages.StateManager_CannotGetFileStatMessage, new Object[]{node.name, error});
								errors[0] = new TCFFileSystemException(message, error);
							}
						}
					});
					if (errors[0] != null) {
						throw errors[0];
					}
				}else{
					String message = NLS.bind(Messages.StateManager_TCFNotProvideFSMessage, node.peerNode.getPeerId());
					throw new TCFFileSystemException(message);
				}
			}
		} finally {
			if (channel != null) Tcf.getChannelManager().closeChannel(channel);
		}
	}

	/**
	 * Set the file's attributes using the new attributes.
	 *
	 * @param node The file's node.
	 * @param attrs The new file attributes.
	 * @throws TCFException
	 */
	public void setFileAttrs(final FSTreeNode node, final IFileSystem.FileAttrs attrs) throws TCFException {
	    IChannel channel = null;
		try {
			channel = FSOperation.openChannel(node.peerNode.getPeer());
			if (channel != null) {
				IFileSystem service = FSOperation.getBlockingFileSystem(channel);
				if (service != null) {
					final TCFFileSystemException[] errors = new TCFFileSystemException[1];
					String path = node.getLocation(true);
					service.setstat(path, attrs, new DoneSetStat() {
						@Override
						public void doneSetStat(IToken token, FileSystemException error) {
							if (error == null) {
								commitNodeAttr(node, attrs);
							} else {
								String message = NLS.bind(Messages.StateManager_CannotSetFileStateMessage, new Object[] { node.name, error });
								errors[0] = new TCFFileSystemException(message, error);
							}
						}
					});
					if (errors[0] != null) {
						throw errors[0];
					}
				} else {
					String message = NLS.bind(Messages.StateManager_TCFNotProvideFSMessage2, node.peerNode.getPeerId());
					throw new TCFFileSystemException(message);
				}
			}
		}  finally {
			if (channel != null) Tcf.getChannelManager().closeChannel(channel);
		}
	}

	/**
	 * Commit the file attribute of the specified tree node with the specified value.
	 *
	 * @param node The tree node whose file attribute is to committed.
	 * @param attr The new file attribute.
	 */
	void commitNodeAttr(FSTreeNode node, FileAttrs attr){
		node.attr = attr;
		PersistenceManager.getInstance().setBaseTimestamp(node.getLocationURL(), attr.mtime);
		node.firePropertyChange();
	}

	/**
	 * Get the local file's state of the specified tree node. The local file must exist
	 * before calling this method to get its state.
	 *
	 * @param node The tree node whose local file state is going to retrieved.
	 * @return The tree node's latest cache state.
	 */
	public CacheState getCacheState(FSTreeNode node) {
		File file = CacheManager.getInstance().getCacheFile(node);
		if(!file.exists())
			return CacheState.consistent;
		long ltime = file.lastModified();
		long btime = PersistenceManager.getInstance().getBaseTimestamp(node.getLocationURL());
		long mtime = 0;
		if(node.attr!=null)
			mtime = node.attr.mtime;
		if(isUnchanged(btime, ltime)){
			if(isUnchanged(mtime, btime))
				return CacheState.consistent;
			return CacheState.outdated;
		}
		if(isUnchanged(mtime, btime))
			return CacheState.modified;
		return CacheState.conflict;
	}

	/**
	 * Compare the modified time of the remote file and the base timestamp
	 * and see if they are equal to each other.
	 *
	 * @param mtime The modified time of the remote file.
	 * @param btime The base timestamp cached.
	 * @return true if they are equal in minute precision.
	 */
	private boolean isUnchanged(long mtime, long btime){
		return Math.abs(mtime-btime)/60000 == 0;
	}
}
