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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.services.IFileSystem;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.exceptions.TCFException;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.exceptions.TCFFileSystemException;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.core.nls.Messages;

/**
 * FSRefresh refreshes a specified tree node and its children and grand children recursively.
 */
public class OpRefresh extends Operation {
	//The root node to be refreshed.
	FSTreeNode node;

	/**
	 * Create an FSRefresh to refresh the specified node and its descendants.
	 *
	 * @param node The root node to be refreshed.
	 */
	public OpRefresh(FSTreeNode node) {
		this.node = node;
	}
	
	/**
	 * Create an FSRefresh to refresh the specified nodes and its descendants.
	 *
	 * @param nodes The node list to be refreshed.
	 */
	public OpRefresh(List<FSTreeNode> nodes) {
		this.node = getAncestor(nodes);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.Operation#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		super.run(monitor);
		if (node.childrenQueried || node.isFile()) {
			IChannel channel = null;
			try {
				channel = openChannel(node.peerNode.getPeer());
				if (channel != null) {
					IFileSystem service = getBlockingFileSystem(channel);
					if (service != null) {
						refresh(node, service);
					}
					else {
						String message = NLS.bind(Messages.Operation_NoFileSystemError, node.peerNode.getPeerId());
						throw new TCFFileSystemException(message);
					}
				}
			}
			catch (TCFException e) {
				throw new InvocationTargetException(e, e.getMessage());
			}
			finally {
				if (channel != null) Tcf.getChannelManager().closeChannel(channel);
				monitor.done();
			}
		}
		else {
			monitor.done();
		}
	}
	
	/**
	 * Refresh the specified node and its children recursively using the file system service.
	 *
	 * @param node The node to be refreshed.
	 * @param service The file system service.
	 * @throws TCFFileSystemException Thrown during refreshing.
	 */
	void refresh(final FSTreeNode node, final IFileSystem service) throws TCFException, InterruptedException {
		if(monitor.isCanceled()) throw new InterruptedException();
		if ((node.isSystemRoot() || node.isDirectory()) && node.childrenQueried) {
			if (!node.isSystemRoot()) {
				updateChildren(node, service);
			}
			monitor.worked(1);
			List<FSTreeNode> children = node.getChildren();
			for (FSTreeNode child : children) {
				refresh(child, service);
			}
		}
		else if(node.isFile()) {
			node.refresh();
		}
	}


	/**
	 * Update the children of the specified folder node using the file system service.
	 *
	 * @param node The folder node.
	 * @param service The file system service.
	 * @throws TCFFileSystemException Thrown during querying the children nodes.
	 */
	protected void updateChildren(final FSTreeNode node, final IFileSystem service) throws TCFFileSystemException, InterruptedException {
		if(monitor.isCanceled()) throw new InterruptedException();
		List<FSTreeNode> current = node.getChildren();
		List<FSTreeNode> latest = queryChildren(node, service);
		List<FSTreeNode> newNodes = diff(latest, current);
		List<FSTreeNode> deleted = diff(current, latest);
		node.removeChildren(deleted);
		node.addChidren(newNodes);
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IOperation#getName()
	 */
	@Override
    public String getName() {
	    return NLS.bind(Messages.OpRefresh_RefreshJobTitle, node == null ? "" : node.name); //$NON-NLS-1$
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.Operation#getTotalWork()
	 */
	@Override
	public int getTotalWork() {
		return count(node);
	}
	
	/**
	 * Count the nodes that should be refreshed under 
	 * the specified directory.
	 * 
	 * @param node The specified directory.
	 * @return the total count of the qualified nodes.
	 */
	private int count(FSTreeNode node) {
		if ((node.isSystemRoot() || node.isDirectory()) && node.childrenQueried) {
			int total = 1;
			List<FSTreeNode> children = node.getChildren();
			for (FSTreeNode child : children) {
				total += count(child);
			}
			return total;
		}
		return 0;
	}
}
