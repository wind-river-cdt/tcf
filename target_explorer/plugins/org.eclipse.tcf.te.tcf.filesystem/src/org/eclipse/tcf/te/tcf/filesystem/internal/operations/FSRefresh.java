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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.services.IFileSystem;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.filesystem.internal.exceptions.TCFException;
import org.eclipse.tcf.te.tcf.filesystem.internal.exceptions.TCFFileSystemException;
import org.eclipse.tcf.te.tcf.filesystem.internal.nls.Messages;
import org.eclipse.tcf.te.tcf.filesystem.model.FSModel;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.ui.PlatformUI;

/**
 * FSRefresh refreshes a specified tree node and its children and grand children recursively.
 */
public class FSRefresh extends FSOperation {
	/**
	 * The root node to be refreshed.
	 */
	private FSTreeNode node;

	/**
	 * Create an FSRefresh to refresh the specified node and its descendants.
	 *
	 * @param node The root node to be refreshed.
	 */
	public FSRefresh(FSTreeNode node) {
		this.node = node;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSOperation#doit()
	 */
	@Override
	public boolean doit() {
		if (node.childrenQueried) {
			IChannel channel = null;
			try {
				channel = openChannel(node.peerNode.getPeer());
				if (channel != null) {
					IFileSystem service = channel.getRemoteService(IFileSystem.class);
					if (service != null) {
						refresh(node, service);
					}
					else {
						String message = NLS.bind(Messages.FSOperation_NoFileSystemError, node.peerNode.getPeer().getID());
						throw new TCFFileSystemException(message);
					}
				}
			}
			catch (TCFException e) {
				// Display the error reported during moving.
				Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				MessageDialog.openError(parent, Messages.FSMove_MoveFileFolderTitle, e.getLocalizedMessage());
				return false;
			}
			finally {
				if (channel != null) Tcf.getChannelManager().closeChannel(channel);
				// Refresh the file system tree.
				FSModel.getInstance().fireNodeStateChanged(node);
			}
		}
		return true;
	}

	/**
	 * Refresh the specified node and its children recursively using the file system service.
	 *
	 * @param node The node to be refreshed.
	 * @param service The file system service.
	 * @throws TCFFileSystemException Thrown during refreshing.
	 */
	private void refresh(final FSTreeNode node, final IFileSystem service) throws TCFFileSystemException {
		if (node.isDirectory() && node.childrenQueried) {
			updateChildren(node, service);
			List<FSTreeNode> children = new ArrayList<FSTreeNode>(getCurrentChildren(node));
			for (FSTreeNode child : children) {
				refresh(child, service);
			}
		}
	}


	/**
	 * Update the children of the specified folder node using the file system service.
	 *
	 * @param node The folder node.
	 * @param service The file system service.
	 * @throws TCFFileSystemException Thrown during querying the children nodes.
	 */
	protected void updateChildren(final FSTreeNode node, final IFileSystem service) throws TCFFileSystemException {
		List<FSTreeNode> current = getCurrentChildren(node);
		List<FSTreeNode> latest = queryChildren(node, service);
		List<FSTreeNode> newNodes = diff(latest, new ArrayList<FSTreeNode>(current));
		List<FSTreeNode> deleted = diff(new ArrayList<FSTreeNode>(current), latest);
		for (FSTreeNode aNode : deleted) {
			current.remove(aNode);
		}
		for (FSTreeNode aNode : newNodes) {
			aNode.parent = node;
			aNode.peerNode = node.peerNode;
			current.add(aNode);
			FSModel.getInstance().addNode(aNode);
		}
	}

	/**
	 * Find those nodes which are in aList yet not in bList and return them as a list.
	 *
	 * @param aList
	 * @param bList
	 * @return the difference list.
	 */
	private List<FSTreeNode> diff(List<FSTreeNode> aList, List<FSTreeNode> bList) {
		List<FSTreeNode> newList = new ArrayList<FSTreeNode>();
		for (FSTreeNode aNode : aList) {
			boolean found = false;
			for (FSTreeNode bNode : bList) {
				if (aNode.name.equals(bNode.name)) {
					found = true;
					break;
				}
			}
			if (!found) {
				newList.add(aNode);
			}
		}
		return newList;
	}
}
