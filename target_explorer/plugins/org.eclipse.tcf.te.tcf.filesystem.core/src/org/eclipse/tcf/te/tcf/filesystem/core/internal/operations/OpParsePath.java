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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.te.runtime.utils.Host;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.exceptions.TCFException;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.testers.TargetPropertyTester;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.CacheManager;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSModel;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.model.Model;

/**
 * The operation to parse a platform specific path to a target's node.
 */
public class OpParsePath extends Operation {
	// The peer on which the file is located.
	IPeerModel peer;
	// The path on the target.
	String path;
	// The parsing result.
	FSTreeNode result;
	
	/**
	 * Create an instance with a path on a specified target.
	 * 
	 * @param peer The target peer.
	 * @param path The path to be parsed.
	 */
	public OpParsePath(IPeerModel peer, String path) {
		this.peer = peer;
		this.path = path;
	}
	
	/**
	 * The path of the cache file to be parsed.
	 * 
	 * @param filePath The local cache's file.
	 */
	@SuppressWarnings("unchecked")
    public OpParsePath(String filePath) {
		String cache_root = CacheManager.getCacheRoot().getAbsolutePath();
		if (filePath.startsWith(cache_root)) {
			filePath = filePath.substring(cache_root.length() + 1);
			int slash = filePath.indexOf(File.separator);
			if (slash != -1) {
				String peerId = filePath.substring(0, slash);
				peerId = peerId.replace(CacheManager.PATH_ESCAPE_CHAR, ':');
				Map<String, IPeerModel> peers = (Map<String, IPeerModel>) Model.getModel().getAdapter(Map.class);
				peer = peers.get(peerId);
				if (peer != null) {
					boolean hostWindows = Host.isWindowsHost();
					boolean windows = TargetPropertyTester.isWindows(peer);
					filePath = filePath.substring(slash + 1);
					if (hostWindows) {
						if (windows) {
							slash = filePath.indexOf(File.separator);
							if (slash != -1) {
								String disk = filePath.substring(0, slash);
								filePath = filePath.substring(slash + 1);
								disk = disk.replace(CacheManager.PATH_ESCAPE_CHAR, ':');
								filePath = disk + File.separator + filePath;
							}
						}
						else {
							filePath = "/" + filePath.replace('\\', '/'); //$NON-NLS-1$
						}
					}
					else {
						if (windows) {
							slash = filePath.indexOf(File.separator);
							if (slash != -1) {
								String disk = filePath.substring(0, slash);
								filePath = filePath.substring(slash + 1);
								disk = disk.replace(CacheManager.PATH_ESCAPE_CHAR, ':');
								filePath = disk + File.separator + filePath;
							}
							filePath = filePath.replace(File.separatorChar, '\\');
						}
						else {
							filePath = "/" + filePath; //$NON-NLS-1$
						}
					}
					path = filePath;
				}
			}
		}
	}
	
	/**
	 * Get the parsing result, which is a node that representing
	 * a file on the target system.
	 * 
	 * @return The file system node.
	 */
	public FSTreeNode getResult() {
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.Operation#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		if (peer != null && path != null) {
			final FSTreeNode root = FSModel.getFSModel(peer).getRoot();
			if (!root.childrenQueried) {
				new NullOpExecutor().execute(new OpRefreshRoots(root));
			}
			Object[] elements = root.getChildren().toArray();
			if (elements != null && elements.length != 0 && path.length() != 0) {
				final FSTreeNode[] children = new FSTreeNode[elements.length];
				System.arraycopy(elements, 0, children, 0, elements.length);
				SafeRunner.run(new ISafeRunnable() {
					@Override
					public void handleException(Throwable e) {
						// Ignore exception
					}

					@Override
					public void run() throws Exception {
						result = findPath(peer.getPeer(), children, path);
					}
				});
			}
		}
	}
	

	/**
	 * Search the path in the children list. If it exists, then search the children of the found
	 * node recursively until the whole path is found. Or else return null.
	 *
	 * @param children The children nodes to search the path.
	 * @param path The path to be searched.
	 * @return The leaf node that has the searched path.
	 * @throws TCFException Thrown during searching.
	 */
	FSTreeNode findPath(IPeer peer, FSTreeNode[] children, String path) throws TCFException, InterruptedException {
		Assert.isTrue(children != null && children.length != 0);
		Assert.isTrue(path != null && path.length() != 0);
		FSTreeNode node = children[0];
		String osPathSep = node.isWindowsNode() ? "\\" : "/"; //$NON-NLS-1$ //$NON-NLS-2$
		int delim = path.indexOf(osPathSep);
		String segment = null;
		if (delim != -1) {
			segment = path.substring(0, delim);
			path = path.substring(delim + 1);
			if (node.isRoot()) {
				// If it is root directory, the name ends with the path separator.
				segment += osPathSep;
			}
		}
		else {
			segment = path;
			path = null;
		}
		node = findPathSeg(children, segment);
		if (path == null || path.trim().length() == 0) {
			// The end of the path.
			return node;
		}
		else if (node != null) {
			if (node.isDirectory()) {
				List<FSTreeNode> nodes= new Operation().getChildren(node);
				children = nodes.toArray(new FSTreeNode[nodes.size()]);
			}
			else {
				children = null;
			}
			if (children != null && children.length != 0) {
				return findPath(peer, children, path);
			}
		}
		return null;
	}

	/**
	 * Find in the children array the node that has the specified name.
	 *
	 * @param children The children array in which to find the node.
	 * @param name The name of the node to be searched.
	 * @return The node that has the specified name.
	 */
	private FSTreeNode findPathSeg(FSTreeNode[] children, String name) {
		for (FSTreeNode child : children) {
			if (child.isWindowsNode()) {
				if (child.name.equalsIgnoreCase(name)) return child;
			}
			else if (child.name.equals(name)) return child;
		}
		return null;
	}
}
