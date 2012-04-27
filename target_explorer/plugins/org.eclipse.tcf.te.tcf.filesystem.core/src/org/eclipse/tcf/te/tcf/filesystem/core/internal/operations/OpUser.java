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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.IFileSystem;
import org.eclipse.tcf.services.IFileSystem.DoneUser;
import org.eclipse.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.filesystem.core.activator.CorePlugin;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.UserAccount;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.exceptions.TCFFileSystemException;
import org.eclipse.tcf.te.tcf.filesystem.core.nls.Messages;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;

/**
 * The operation to get the user account of a specified peer.
 */
public class OpUser extends Operation {
	// The key to save and retrieve the user account in a peer model.
	/* default */ static final String USER_ACCOUNT_KEY = CorePlugin.getUniqueIdentifier()+".user.account"; //$NON-NLS-1$

	// The target peer.
	IPeerModel peerNode;
	// The resulting account.
	UserAccount result;
	
	/**
	 * Create an operation using the target peer.
	 * @param peerNode The peer whose user account to be checked.
	 */
	public OpUser(IPeerModel peerNode) {
		this.peerNode = peerNode;
	}
	
	/**
	 * Get the checking result.
	 * 
	 * @return The user account checked.
	 */
	public UserAccount getUserAccount() {
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.Operation#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		if(peerNode != null) {
			result = getUserFromPeer(peerNode);
			if (result == null) {
				final UserAccount[] accounts = new UserAccount[1];
				SafeRunner.run(new ISafeRunnable(){
					@Override
                    public void handleException(Throwable e) {
						// Just ignore it.
                    }
					@Override
                    public void run() throws Exception {
						IChannel channel = null;
						try {
							channel = Operation.openChannel(peerNode.getPeer());
							if (channel != null) {
								accounts[0] = getUserByChannel(channel);
								if (accounts[0] != null) setUserToPeer(peerNode, accounts[0]);
							}
						}
						finally {
							if (channel != null) Tcf.getChannelManager().closeChannel(channel);
						}
                    }});
				result = accounts[0];
			}
		}
    }

	/**
	 * Get the user account from the peer using the channel connected to the
	 * remote target.
	 *
	 * @param channel
	 *            The channel connected to the remote target.
	 * @return The user account information or null if it fails.
	 */
	UserAccount getUserByChannel(final IChannel channel) throws TCFFileSystemException {
		IFileSystem service = Operation.getBlockingFileSystem(channel);
		if (service != null) {
			final TCFFileSystemException[] errors = new TCFFileSystemException[1];
			final UserAccount[] accounts = new UserAccount[1];
			service.user(new DoneUser() {
				@Override
				public void doneUser(IToken token, FileSystemException error, int real_uid, int effective_uid, int real_gid, int effective_gid, String home) {
					if (error == null) {
						accounts[0] = new UserAccount(real_uid, real_gid, effective_uid, effective_gid, home);
					}else {
						errors[0] = newTCFException(error);
					}
				}
			});
			if (errors[0] != null) {
				throw errors[0];
			}
			return accounts[0];
		}
		String message = NLS.bind(Messages.Operation_NoFileSystemError, channel.getRemotePeer().getID());
		throw new TCFFileSystemException(message);
	}

	/**
	 * Get the user account stored in the specified peer model using a key named
	 * "user.account" defined by the constant USER_ACCOUNT_KEY.
	 *
	 * @param peer
	 *            The peer model from which the user account is retrieved.
	 * @return The user account if it exists or null if not.
	 */
	private UserAccount getUserFromPeer(final IPeerModel peer) {
		Assert.isNotNull(peer);
		if (Protocol.isDispatchThread()) {
			return (UserAccount) peer.getProperty(USER_ACCOUNT_KEY);
		}
		final UserAccount[] accounts = new UserAccount[1];
		Protocol.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				accounts[0] = (UserAccount) peer.getProperty(USER_ACCOUNT_KEY);
			}
		});
		return accounts[0];
	}

	/**
	 * Save the user account to the specified peer model using a key named
	 * "user.account" defined by the constant USER_ACCOUNT_KEY.
	 *
	 * @param peer
	 *            The peer model to which the user account is saved.
	 */
	void setUserToPeer(final IPeerModel peer, final UserAccount account) {
		Assert.isNotNull(peer);
		Assert.isNotNull(account);

		if (Protocol.isDispatchThread()) {
			peer.setProperty(USER_ACCOUNT_KEY, account);
		} else {
			Protocol.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					peer.setProperty(USER_ACCOUNT_KEY, account);
				}
			});
		}
	}
}
