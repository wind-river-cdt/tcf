/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * William Chen (Wind River) - [345384] Provide property pages for remote file system nodes
 * William Chen (Wind River) - [352302]Opening a file in an editor depending on
 *                             the client's permissions.
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.model;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.IFileSystem;
import org.eclipse.tcf.services.IFileSystem.DirEntry;
import org.eclipse.tcf.services.IFileSystem.FileAttrs;
import org.eclipse.tcf.te.tcf.filesystem.interfaces.IWindowsFileAttributes;
import org.eclipse.tcf.te.tcf.filesystem.internal.UserAccount;
import org.eclipse.tcf.te.tcf.filesystem.internal.testers.TargetPropertyTester;
import org.eclipse.tcf.te.tcf.filesystem.internal.url.TcfURLConnection;
import org.eclipse.tcf.te.tcf.filesystem.internal.url.TcfURLStreamHandlerService;
import org.eclipse.tcf.te.tcf.filesystem.internal.utils.UserManager;
import org.eclipse.tcf.te.tcf.filesystem.nls.Messages;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProvider;
import org.eclipse.tcf.te.ui.interfaces.IViewerInput;

/**
 * Representation of a file system tree node.
 * <p>
 * <b>Note:</b> Node construction and child list access is limited to the TCF
 * event dispatch thread.
 */
public final class FSTreeNode extends PlatformObject implements Cloneable, IPeerModelProvider {
	// The pending node constant.
	public static final FSTreeNode PENDING_NODE = createPendingNode();
	// The constant to access the Windows Attributes.
	private static final String KEY_WIN32_ATTRS = "Win32Attrs"; //$NON-NLS-1$

	private final UUID uniqueId = UUID.randomUUID();

	/**
	 * The tree node name.
	 */
	public String name = null;

	/**
	 * The tree node type.
	 */
	public String type = null;

	/**
	 * The tree node file system attributes
	 */
	public IFileSystem.FileAttrs attr = null;

	/**
	 * The peer node the file system tree node is associated with.
	 */
	public IPeerModel peerNode = null;

	/**
	 * The tree node parent.
	 */
	public FSTreeNode parent = null;

	/**
	 * The tree node children.
	 */
	private List<FSTreeNode> children;

	/**
	 * Flag to mark once the children of the node got queried
	 */
	public boolean childrenQueried = false;

	/**
	 * Flag to mark once the children query is running
	 */
	public boolean childrenQueryRunning = false;

	/**
	 * Create a folder node using the specified parent node, the directory entry
	 * and the flag to indicate if it is a root node.
	 * 
	 * @param parentNode The parent node.
	 * @param entry The directory entry.
	 * @param entryIsRootNode If this folder is root folder.
	 */
	public FSTreeNode(FSTreeNode parentNode, DirEntry entry, boolean entryIsRootNode) {
		Assert.isNotNull(entry);
		children = Collections.synchronizedList(new ArrayList<FSTreeNode>());
		IFileSystem.FileAttrs attrs = entry.attrs;

		this.attr = attrs;
		this.name = entry.filename;
		if (attrs == null || attrs.isDirectory()) {
			this.type = entryIsRootNode ? "FSRootDirNode" : "FSDirNode"; //$NON-NLS-1$ //$NON-NLS-2$
		} else if (attrs.isFile()) {
			this.type = "FSFileNode"; //$NON-NLS-1$
		}
		this.parent = parentNode;
		this.peerNode = parentNode.peerNode;
		Assert.isTrue(Protocol.isDispatchThread());
	}
	
	/**
	 * Constructor.
	 */
	public FSTreeNode() {
		super();
		children = Collections.synchronizedList(new ArrayList<FSTreeNode>());
		Assert.isTrue(Protocol.isDispatchThread());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		if (Protocol.isDispatchThread()) {
			FSTreeNode clone = new FSTreeNode();
			clone.childrenQueried = childrenQueried;
			clone.childrenQueryRunning = childrenQueryRunning;
			clone.name = name;
			clone.parent = parent;
			clone.peerNode = peerNode;
			clone.type = type;
			if (attr != null) {
				Map<String, Object> attributes = new HashMap<String, Object>(attr.attributes);
				clone.attr = new IFileSystem.FileAttrs(attr.flags, attr.size, attr.uid, attr.gid, attr.permissions, attr.atime, attr.mtime, attributes);
			} else {
				clone.attr = null;
			}
			return clone;
		}
		final Object[] objects = new Object[1];
		Protocol.invokeAndWait(new Runnable() {

			@Override
			public void run() {
				objects[0] = FSTreeNode.this.clone();
			}
		});
		return objects[0];
	}

	/**
	 * Change the file/folder's write permission.
	 * @param b true if the agent is granted with its write permission.
	 */
	public void setWritable(boolean b) {
		UserAccount account = UserManager.getInstance().getUserAccount(peerNode);
		if (account != null && attr != null) {
			int bit;
			if (attr.uid == account.getEUID()) {
				bit = IFileSystem.S_IWUSR;
			} else if (attr.gid == account.getEGID()) {
				bit = IFileSystem.S_IWGRP;
			} else {
				bit = IFileSystem.S_IWOTH;
			}
			int permissions = attr.permissions;
			setPermissions(b ? (permissions | bit):(permissions & ~ bit));
		}
    }

	/**
	 * Set the file's permissions.
	 * @param permissions The new permissions.
	 */
	public void setPermissions(int permissions) {
		attr = new IFileSystem.FileAttrs(attr.flags, attr.size, attr.uid, attr.gid, permissions, attr.atime, attr.mtime, attr.attributes);
    }

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public final int hashCode() {
		return uniqueId.hashCode();
	}

	/**
	 * Returns the children list storage object.
	 * <p>
	 * <b>Note:</b> This method must be called from within the TCF event
	 * dispatch thread only!
	 *
	 * @return The children list storage object.
	 */
	public final List<FSTreeNode> getChildren() {
		Assert.isTrue(Protocol.isDispatchThread());
		return children;
	}
	
	/**
	 * Returns the children outside of TCF thread.
	 * 
	 * @return The children list.
	 */
	public List<FSTreeNode> unsafeGetChildren() {
		return new ArrayList<FSTreeNode>(children);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public final boolean equals(Object obj) {
		if(this == obj)
			return true;
		if (obj instanceof FSTreeNode) {
			return uniqueId.equals(((FSTreeNode) obj).uniqueId);
		}
		return super.equals(obj);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder(getClass().getSimpleName());
		buffer.append(": name=" + (name != null ? name : super.toString())); //$NON-NLS-1$
		buffer.append(", UUID=" + uniqueId.toString()); //$NON-NLS-1$
		return buffer.toString();
	}

	/**
	 * Return if the node is a Windows file/folder node.
	 *
	 * @return true if it is a Windows node, or else false.
	 */
	public boolean isWindowsNode() {
		if (attr != null && attr.attributes != null) {
			return attr.attributes.containsKey(KEY_WIN32_ATTRS);
		}
		if (peerNode != null) {
			String OSName = TargetPropertyTester.getOSName(peerNode);
			if(OSName != null){
				return OSName.startsWith("Windows"); //$NON-NLS-1$
			}
		}
		if (!children.isEmpty()) {
			return children.get(0).isWindowsNode();
		}
		return false;
	}

	/**
	 * Return if the node is a file.
	 *
	 * @return true if it is a file, or else false.
	 */
	public boolean isFile() {
		return attr != null && attr.isFile();
	}

	/**
	 * Return if the node is a directory.
	 *
	 * @return true if it is a directory, or else false.
	 */
	public boolean isDirectory() {
		return attr != null && attr.isDirectory();
	}

	/**
	 * Return if the attribute specified by the mask bit is turned on.
	 *
	 * @param bit
	 *            The attribute's mask bit.
	 * @return true if it is on, or else false.
	 */
	public boolean isWin32AttrOn(int bit) {
		if (attr != null && attr.attributes.get(KEY_WIN32_ATTRS) instanceof Integer) {
			Integer win32Attrs = (Integer) attr.attributes.get(KEY_WIN32_ATTRS);
			return (win32Attrs.intValue() & bit) != 0;
		}
		return false;
	}

	/**
	 * Set the attribute specified by the mask bit to on or off.
	 * @param bit The attribute's mask bit.
	 * @param on The flag if the bit should be turned on or off.
	 */
	public void setWin32Attr(int bit, boolean on) {
		if (attr != null && attr.attributes.get(KEY_WIN32_ATTRS) instanceof Integer) {
			int win32attr = ((Integer) attr.attributes.get(KEY_WIN32_ATTRS)).intValue();
			win32attr = on ? (win32attr | bit) : (win32attr & ~bit);
			attr.attributes.put(KEY_WIN32_ATTRS, Integer.valueOf(win32attr));
		}
	}

	/**
	 * Get the file's win32 attributes.
	 * @return The file's win32 attributes.
	 */
	public int getWin32Attrs() {
		if (attr != null && attr.attributes.get(KEY_WIN32_ATTRS) instanceof Integer) {
			return ((Integer) attr.attributes.get(KEY_WIN32_ATTRS)).intValue();
		}
	    return 0;
    }

	/**
	 * Return if this file/folder is hidden.
	 *
	 * @return true if it is hidden, or else false.
	 */
	public boolean isHidden() {
		return isWin32AttrOn(IWindowsFileAttributes.FILE_ATTRIBUTE_HIDDEN);
	}

	/**
	 * Set the file/folder hidden attribute's value.
	 * @param hidden The new value.
	 */
	public void setHidden(boolean hidden) {
		setWin32Attr(IWindowsFileAttributes.FILE_ATTRIBUTE_HIDDEN, hidden);
    }

	/**
	 * Return if this file/folder is read-only.
	 *
	 * @return true if it is read-only, or else false.
	 */
	public boolean isReadOnly() {
		return isWin32AttrOn(IWindowsFileAttributes.FILE_ATTRIBUTE_READONLY);
	}

	/**
	 * Set the file/folder read-only attribute's value.
	 * @param readOnly The new value.
	 */
	public void setReadOnly(boolean readOnly) {
		setWin32Attr(IWindowsFileAttributes.FILE_ATTRIBUTE_READONLY, readOnly);
    }

	/**
	 * Get the location of a file/folder node using the format of the file
	 * system's platform.
	 *
	 * @param parentNode
	 *            The file/folder node.
	 * @return The location of the file/folder.
	 */
	public String getLocation() {
		return getLocation(false);
	}

	/**
	 * Get the location of a file/folder.
	 *
	 * @param cross
	 *            If the format is cross-platform.
	 * @return The path to the file/folder.
	 */
	public String getLocation(boolean cross) {
		if(isRoot()) {
			if(cross) {
				if(isWindowsNode()) {
					return name.substring(0, name.length() - 1) + "/"; //$NON-NLS-1$
				}
			}
			return name;
		}
		String pLoc = parent.getLocation(cross);
		if(parent.isRoot()) {
			return pLoc + name;
		}
		String pathSep = (!cross && isWindowsNode()) ? "\\" : "/"; //$NON-NLS-1$ //$NON-NLS-2$
		return pLoc + pathSep + name;
	}

	/**
	 * Get the URL of the file or folder. The URL's format is created in the
	 * following way: tcf:/<TCF_AGENT_ID>/remote/path/to/the/resource... See
	 * {@link TcfURLConnection#TcfURLConnection(URL)}
	 *
	 * @see TcfURLStreamHandlerService#parseURL(URL, String, int, int)
	 * @see #getLocationURI()
	 * @return The URL of the file/folder.
	 */
	public URL getLocationURL() {
		try {
			return 	getLocationURI().toURL();
		} catch (MalformedURLException e) {
			assert false;
			return null;
		}
	}
	
	/**
	 * Get the URI of the file or folder. The URI's format is created in the
	 * following way: tcf:/<TCF_AGENT_ID>/remote/path/to/the/resource...
	 *
	 * @return The URI of the file/folder.
	 */
	public URI getLocationURI() {
		try {
			String id = peerNode.getPeerId();
			String path = getLocation(true);
			String location = TcfURLConnection.PROTOCOL_SCHEMA + ":/" + id + (path.startsWith("/") ? path : "/" + path); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return new URI(location);
		}
		catch (URISyntaxException e) {
			assert false;
			return null;
		}
	}

	/**
	 * If this node is a root node.
	 *
	 * @return true if this node is a root node.
	 */
	public boolean isRoot() {
		return type != null && type.equals("FSRootDirNode"); //$NON-NLS-1$
	}

	/**
	 * If this node is the system root.
	 *
	 * @return true if this node is the system root.
	 */
	public boolean isSystemRoot() {
		return type != null && type.equals("FSRootNode"); //$NON-NLS-1$
	}

	/**
	 * If this file is readable.
	 *
	 * @return true if it is readable.
	 */
	public boolean isReadable() {
		UserAccount account = UserManager.getInstance().getUserAccount(peerNode);
		if (account != null && attr != null) {
			if (attr.uid == account.getEUID()) {
				return (attr.permissions & IFileSystem.S_IRUSR) != 0;
			} else if (attr.gid == account.getEGID()) {
				return (attr.permissions & IFileSystem.S_IRGRP) != 0;
			} else {
				return (attr.permissions & IFileSystem.S_IROTH) != 0;
			}
		}
		return false;
	}

	/**
	 * If the agent is the owner of this file/folder.
	 *
	 * @return true if the agent is the owner of this file/folder.
	 */
	public boolean isAgentOwner() {
		UserAccount account = UserManager.getInstance().getUserAccount(peerNode);
		if (account != null && attr != null) {
			return attr.uid == account.getEUID();
		}
		return false;
	}

	/**
	 * If this file is writable.
	 *
	 * @return true if it is writable.
	 */
	public boolean isWritable() {
		UserAccount account = UserManager.getInstance().getUserAccount(peerNode);
		if (account != null && attr != null) {
			if (attr.uid == account.getEUID()) {
				return (attr.permissions & IFileSystem.S_IWUSR) != 0;
			} else if (attr.gid == account.getEGID()) {
				return (attr.permissions & IFileSystem.S_IWGRP) != 0;
			} else {
				return (attr.permissions & IFileSystem.S_IWOTH) != 0;
			}
		}
		return false;
	}

	/**
	 * If this file is executable.
	 *
	 * @return true if it is executable.
	 */
	public boolean isExecutable() {
		UserAccount account = UserManager.getInstance().getUserAccount(peerNode);
		if (account != null && attr != null) {
			if (attr.uid == account.getEUID()) {
				return (attr.permissions & IFileSystem.S_IXUSR) != 0;
			} else if (attr.gid == account.getEGID()) {
				return (attr.permissions & IFileSystem.S_IXGRP) != 0;
			} else {
				return (attr.permissions & IFileSystem.S_IXOTH) != 0;
			}
		}
		return false;
	}

	/**
	 * If this node is ancestor of the specified node.
	 * @return true if it is.
	 */
	public boolean isAncestorOf(FSTreeNode node) {
		if (node == null) return false;
		if (node.parent == this) return true;
		return isAncestorOf(node.parent);
	}

	/**
	 * Test if this file is a windows system file.
	 *
	 * @return true if it is a windows system file.
	 */
	public boolean isSystem() {
		return !isRoot() && isWindowsNode() && isWin32AttrOn(IWindowsFileAttributes.FILE_ATTRIBUTE_SYSTEM);
	}

	/**
	 * Get the type label of the file for displaying purpose.
	 *
	 * @return The type label text.
	 */
	public String getFileType() {
		if (isPendingNode()) {
			return ""; //$NON-NLS-1$
		}
		if (isRoot()) {
			return Messages.FSTreeNode_TypeLocalDisk;
		}
		if (isDirectory()) {
			return Messages.FSTreeNode_TypeFileFolder;
		}
		if (isSystem()) {
			return Messages.FSTreeNode_TypeSystemFile;
		}
		IContentType contentType = Platform.getContentTypeManager().findContentTypeFor(name);
		if (contentType != null) {
			return contentType.getName();
		}
		int lastDot = name.lastIndexOf("."); //$NON-NLS-1$
		if (lastDot == -1) {
			return Messages.GeneralInformationPage_UnknownFileType;
		}
		return name.substring(lastDot + 1).toUpperCase() + " " + Messages.FSTreeNode_TypeFile; //$NON-NLS-1$
    }

	/**
	 * Set the file's attributes and trigger property change event.
	 * 
	 * @param attrs The new attributes.
	 */
	public void setAttributes(FileAttrs attrs) {
		FileAttrs oldAttrs = this.attr;
		this.attr = attrs;
		if (attrs != oldAttrs) {
			firePropertyChange(new PropertyChangeEvent(this, "attributes", oldAttrs, attrs)); //$NON-NLS-1$
		}
	}
	
	/**
	 * Fire a property change event to notify one of the node's property has changed.
	 * 
	 * @param event The property change event.
	 */
	protected void firePropertyChange(PropertyChangeEvent event) {
		if(peerNode != null) {
			IViewerInput viewerInput = (IViewerInput) peerNode.getAdapter(IViewerInput.class);
			viewerInput.firePropertyChange(event);
		} else if(parent != null) {
			parent.firePropertyChange(event);
		}
    }

	/**
	 * Set the file's new name and trigger property change event.
	 * 
	 * @param name The new name.
	 */
	public void setName(String name) {
		String oldName = this.name;
		this.name = name;
		if(name == null && oldName != null || name != null && !name.equals(oldName)) {
			firePropertyChange(new PropertyChangeEvent(this, "name", oldName, name)); //$NON-NLS-1$
		}
	}

	/**
	 * Create a root node for the peer.
	 * 
	 * @param peerNode The peer.
	 * @return The root file system node.
	 */
	public static FSTreeNode createRootNode(IPeerModel peerNode) {
		FSTreeNode node = new FSTreeNode();
		node.type = "FSRootNode"; //$NON-NLS-1$
		node.peerNode = peerNode;
		node.name = Messages.FSTreeNodeContentProvider_rootNode_label;
	    return node;
    }

	/**
	 * Return if this node is a pending node.
	 * 
	 * @return true if it is a pending node.
	 */
	public boolean isPendingNode() {
	    return type != null && type.equals("FSPendingNode"); //$NON-NLS-1$
    }


	/**
	 * Create a pending node.
	 * 
	 * @return A pending node.
	 */
	static FSTreeNode createPendingNode() {
		if (Protocol.isDispatchThread()) {
			FSTreeNode pendingNode = new FSTreeNode();
			pendingNode.name = Messages.PendingOperation_label;
			pendingNode.type = "FSPendingNode"; //$NON-NLS-1$
			return pendingNode;
		}
		final AtomicReference<FSTreeNode> reference = new AtomicReference<FSTreeNode>();
		Protocol.invokeAndWait(new Runnable() {

			@Override
			public void run() {
				reference.set(createPendingNode());
			}
		});
		return reference.get();
	}
	
	/**
	 * Create a file node under the folder specified folder using the new name.
	 * 
	 * @param name The file's name.
	 * @param folder The parent folder.
	 * @return The file tree node.
	 */
	public static FSTreeNode createFileNode(String name, FSTreeNode folder) {
		return createTreeNode(name, "FSFileNode", folder); //$NON-NLS-1$
    }

	/**
	 * Create a folder node under the folder specified folder using the new name.
	 * 
	 * @param name The folder's name.
	 * @param folder The parent folder.
	 * @return The folder tree node.
	 */
	public static FSTreeNode createFolderNode(String name, FSTreeNode folder) {
		return createTreeNode(name, "FSDirNode", folder); //$NON-NLS-1$
    }

	/**
	 * Create a tree node under the folder specified folder using the new name.
	 * 
	 * @param name The tree node's name.
	 * @param type The new node's type.
	 * @param folder The parent folder.
	 * @return The tree node.
	 */
	private static FSTreeNode createTreeNode(String name, String type, FSTreeNode folder) {
	    FSTreeNode node = new FSTreeNode();
		node.name = name;
		node.parent = folder;
		node.peerNode = folder.peerNode;
		node.type = type;
	    return node;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProvider#getPeerModel()
	 */
	@Override
    public IPeerModel getPeerModel() {
	    return peerNode;
    }

	/**
	 * Add the specified nodes to the children list.
	 * 
	 * @param nodes The nodes to be added.
	 */
	public void addChidren(List<FSTreeNode> nodes) {
		children.addAll(nodes);
		PropertyChangeEvent event = new PropertyChangeEvent(this, "addChildren", null, null); //$NON-NLS-1$
		firePropertyChange(event);
    }

	/**
	 * Remove the specified nodes from the children list.
	 * 
	 * @param nodes The nodes to be removed.
	 */
	public void removeChildren(List<FSTreeNode> nodes) {
		children.removeAll(nodes);
		PropertyChangeEvent event = new PropertyChangeEvent(this, "removeChildren", null, null); //$NON-NLS-1$
		firePropertyChange(event);
    }

	/**
	 * Add the specified the node to the children list.
	 * 
	 * @param node The child node to be added.
	 */
	public void addChild(FSTreeNode node) {
		children.add(node);
		PropertyChangeEvent event = new PropertyChangeEvent(this, "addChild", null, null); //$NON-NLS-1$
		firePropertyChange(event);
    }

	/**
	 * Remove the specified child node from its children list.
	 * 
	 * @param node The child node to be removed.
	 */
	public void removeChild(FSTreeNode node) {
		children.remove(node);
		PropertyChangeEvent event = new PropertyChangeEvent(this, "removeChild", null, null); //$NON-NLS-1$
		firePropertyChange(event);
    }

	/**
	 * Clear the children of this folder.
	 */
	public void clearChildren() {
		children.clear();
		PropertyChangeEvent event = new PropertyChangeEvent(this, "clearChildren", null, null); //$NON-NLS-1$
		firePropertyChange(event);
    }
}
