/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.core.model;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.utils.Host;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.filesystem.core.activator.CorePlugin;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.callbacks.QueryDoneOpenChannel;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.exceptions.TCFException;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpCreateRoot;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.Operation;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.testers.TargetPropertyTester;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.CacheManager;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.model.Model;

/**
 * The file system model implementation.
 */
public final class FSModel {
	/* default */static final String FSMODEL_KEY = CorePlugin.getUniqueIdentifier() + ".file.system"; //$NON-NLS-1$

	/**
	 * Get the file system model of the peer model. If it does not
	 * exist yet, create a new instance and store it.
	 *
	 * @param peerModel The peer model
	 * @return The file system model connected this peer model.
	 */
	public static FSModel getFSModel(final IPeerModel peerModel) {
		if (peerModel != null) {
			if (Protocol.isDispatchThread()) {
				FSModel model = (FSModel) peerModel.getProperty(FSMODEL_KEY);
				if (model == null) {
					model = new FSModel(peerModel);
					peerModel.setProperty(FSMODEL_KEY, model);
				}
				return model;
			}
			final FSModel[] result = new FSModel[1];
			Protocol.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					result[0] = getFSModel(peerModel);
				}
			});
			return result[0];
		}
		return null;
	}

	// The root node of the peer model
	private FSTreeNode root;
	/**
	 * Create a File System Model.
	 */
	private FSModel(IPeerModel peerModel) {
	}

	/**
	 * Get the root node of the peer model.
	 *
	 * @return The root node.
	 */
	public FSTreeNode getRoot() {
		return root;
	}

	/**
	 * Set the root node of the peer model.
	 *
	 * @param root The root node
	 */
	public void setRoot(FSTreeNode root) {
		this.root = root;
	}

	/**
	 * Get the corresponding FSTreeNode based on the path of the local cache file.
	 *
	 * @param filePath The local cache's file path.
	 * @return The FSTreeNode.
	 */
	@SuppressWarnings("unchecked")
    public static FSTreeNode getTreeNode(String filePath) {
		String cache_root = CacheManager.getInstance().getCacheRoot().getAbsolutePath();
		if (filePath.startsWith(cache_root)) {
			filePath = filePath.substring(cache_root.length() + 1);
			int slash = filePath.indexOf(File.separator);
			if (slash != -1) {
				String peerId = filePath.substring(0, slash);
				peerId = peerId.replace(CacheManager.PATH_ESCAPE_CHAR, ':');
				Map<String, IPeerModel> peers = (Map<String, IPeerModel>) Model.getModel().getAdapter(Map.class);
				IPeerModel peer = peers.get(peerId);
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
					return findTreeNode(peer, filePath);
				}
			}
		}
		return null;
	}

	/**
	 * Find the tree node in the peer's file system tree.
	 *
	 * @param peer The peer model.
	 * @param path The relative path to the cache file.
	 * @return The FSTreeNode corresponding to this file.
	 */
	public static FSTreeNode findTreeNode(final IPeerModel peer, final String path) {
		FSModel fsModel = getFSModel(peer);
		FSTreeNode root = fsModel.getRoot();
		if (root == null) {
			OpCreateRoot fsRoot = new OpCreateRoot(peer);
			root = fsRoot.create();
			fsModel.setRoot(root);
		}
		Object[] elements = root.unsafeGetChildren().toArray();
		if (elements != null && elements.length != 0 && path.length() != 0) {
			final FSTreeNode[] children = new FSTreeNode[elements.length];
			System.arraycopy(elements, 0, children, 0, elements.length);
			final FSTreeNode[] result = new FSTreeNode[1];
			SafeRunner.run(new ISafeRunnable() {
				@Override
                public void handleException(Throwable e) {
					// Ignore exception
                }
				@Override
				public void run() throws Exception {
					result[0] = findPath(peer.getPeer(), children, path);
				}
			});
			return result[0];
		}
		return null;
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
	static FSTreeNode findPath(IPeer peer, FSTreeNode[] children, String path) throws TCFException {
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
	static private FSTreeNode findPathSeg(FSTreeNode[] children, String name) {
		for (FSTreeNode child : children) {
			if (child.isWindowsNode()) {
				if (child.name.equalsIgnoreCase(name)) return child;
			}
			else if (child.name.equals(name)) return child;
		}
		return null;
	}

	/**
	 * Create a root node for the specified peer.
	 *
	 * @param peerNode The peer.
	 */
	public void createRoot(final IPeerModel peerNode) {
		if (Protocol.isDispatchThread()) {
			this.root = FSTreeNode.createRootNode(peerNode);
		}
		else {
			Protocol.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					createRoot(peerNode);
				}
			});
		}
	}

	/**
	 * Query the children of the given file system node.
	 *
	 * @param parentNode The file system node. Must not be <code>null</code>.
	 */
	public void queryChildren(FSTreeNode parentNode) {
		Assert.isNotNull(parentNode);
		parentNode.queryStarted();
		Tcf.getChannelManager().openChannel(parentNode.peerNode.getPeer(), null, new QueryDoneOpenChannel(parentNode));
    }
}
