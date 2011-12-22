/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.controls;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.ISysMonitor;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.processes.ui.activator.UIPlugin;
import org.eclipse.tcf.te.ui.nls.Messages;
import org.eclipse.ui.PlatformUI;

/**
 * Processes tree control content provider implementation.
 */
public class ProcessesTreeContentProvider implements ITreeContentProvider {
	/* default */static final String PROCESS_ROOT_KEY = UIPlugin.getUniqueIdentifier() + ".process.root"; //$NON-NLS-1$
	/**
	 * Static reference to the return value representing no elements.
	 */
	protected final static Object[] NO_ELEMENTS = new Object[0];

	// Flag to control if the file system root node is visible
	private final boolean rootNodeVisible;

	/* default */Viewer viewer = null;

	/**
	 * Create an instance with the rootNodeVisible set to true.
	 */
	public ProcessesTreeContentProvider() {
		this(true);
	}

	/**
	 * Create an instance specifying if the rootNodeVisible is true.
	 * 
	 * @param rootVisible true if the root is visible.
	 */
	public ProcessesTreeContentProvider(boolean rootVisible) {
		this.rootNodeVisible = rootVisible;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object element) {
		if (element instanceof ProcessesTreeNode) {
			return ((ProcessesTreeNode) element).parent;
		}
		return null;
	}

	/**
	 * Get the root node stored in the peer model.
	 * 
	 * @param peerModel The target's peer model.
	 * @return The root node representing the processes.
	 */
	/* default */ ProcessesTreeNode getProcessRoot(final IPeerModel peerModel) {
		if (peerModel != null) {
			if (Protocol.isDispatchThread()) {
				ProcessesTreeNode root = (ProcessesTreeNode) peerModel.getProperty(PROCESS_ROOT_KEY);
				if (root == null) {
					root = new ProcessesTreeNode();
					root.peerNode = peerModel;
					root.type = "ProcRootNode"; //$NON-NLS-1$
					root.childrenQueried = false;
					root.childrenQueryRunning = false;
					peerModel.setProperty(PROCESS_ROOT_KEY, root);
				}
				return root;
			}
			final AtomicReference<ProcessesTreeNode> reference = new AtomicReference<ProcessesTreeNode>();
			Protocol.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					reference.set(getProcessRoot(peerModel));
				}
			});
			return reference.get();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		Assert.isNotNull(parentElement);

		// For the file system, we need the peer node
		if (parentElement instanceof IPeerModel) {
			final IPeerModel peerModel = (IPeerModel) parentElement;
			final ProcessesTreeNode root = getProcessRoot(peerModel);
			if (rootNodeVisible) {
				return new Object[] { root };
			}
			return getChildren(root);
		}
		else if (parentElement instanceof ProcessesTreeNode) {
			ProcessesTreeNode node = (ProcessesTreeNode) parentElement;
			if (node.childrenQueried) {
				return node.children.toArray();
			}
			if (!node.childrenQueryRunning) {
				doGetChildrenForProcessContext(node);
			}
			ProcessesTreeNode pendingNode = new ProcessesTreeNode();
			pendingNode.name = Messages.PendingOperation_label;
			pendingNode.type = "ProcPendingNode"; //$NON-NLS-1$
			return new Object[] { pendingNode };
		}
		return NO_ELEMENTS;
	}

	/**
	 * Refresh the viewer that displays the processes.
	 */
	/* default */void refreshViewer() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (viewer != null && !viewer.getControl().isDisposed()) viewer.refresh();
			}
		});
	}
	
	/**
	 * The callback handler that handles the result of service.getContext.
	 */
	class SysDoneGetContext implements ISysMonitor.DoneGetContext {
		private IChannel channel;
		private ProcessesTreeNode parentNode;
		private Iterator<String> iterator;
		private ISysMonitor service;
		public SysDoneGetContext(IChannel channel, ISysMonitor service, Iterator<String> iterator, ProcessesTreeNode parentNode) {
			this.channel = channel;
			this.service = service;
			this.iterator = iterator;
			this.parentNode = parentNode;
		}
        @Override
        public void doneGetContext(IToken token, Exception error, ISysMonitor.SysMonitorContext context) {
            if (error == null && context != null) {
                ProcessesTreeNode childNode = createNodeFromSysMonitorContext(context);
                childNode.parent = parentNode;
                childNode.peerNode = parentNode.peerNode;
                parentNode.children.add(childNode);
            }
            if(iterator.hasNext()) {
            	String contextId = iterator.next();
            	service.getContext(contextId, this);
            } else {
                parentNode.childrenQueryRunning = false;
                parentNode.childrenQueried = true;
                Tcf.getChannelManager().closeChannel(channel);
                refreshViewer();
            }
        }
	}
	
	/**
	 * The callback handler that handles the result of service.getChildren.
	 */
	class SysDoneGetChildren implements ISysMonitor.DoneGetChildren {
		IChannel channel;
		ISysMonitor service;
		ProcessesTreeNode parentNode;
		public SysDoneGetChildren(IChannel channel, ISysMonitor service, ProcessesTreeNode parentNode) {
			this.channel = channel;
			this.service = service;
			this.parentNode = parentNode;
		}
        @Override
        public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
            if (error == null && context_ids != null && context_ids.length > 0) {
            	final Iterator<String> iterator = Arrays.asList(context_ids).iterator();
            	String contextId = iterator.next();
                service.getContext(contextId, new SysDoneGetContext(channel, service, iterator, parentNode));
        	} else {
                parentNode.childrenQueryRunning = false;
                parentNode.childrenQueried = true;
                Tcf.getChannelManager().closeChannel(channel);
                refreshViewer();
        	}
        }		
	}
	
	/**
	 * The callback handler that handles the event when the channel opens.
	 */
	class SysDoneOpenChannel implements IChannelManager.DoneOpenChannel {
		ProcessesTreeNode parentNode;
		public SysDoneOpenChannel(ProcessesTreeNode parentNode) {
			this.parentNode = parentNode;
		}
		@Override
        public void doneOpenChannel(Throwable error, final IChannel channel) {
			Assert.isTrue(Protocol.isDispatchThread());
			if (error == null && channel != null) {
				final ISysMonitor service = channel.getRemoteService(ISysMonitor.class);
				if (service != null) {
					service.getChildren(parentNode.id, new SysDoneGetChildren(channel, service, parentNode));
				}
			}
        }
	}

	/**
	 * Query the children of the given process context.
	 * 
	 * @param parentNode The process context node. Must be not <code>null</code>.
	 */
	protected void doGetChildrenForProcessContext(final ProcessesTreeNode parentNode) {
		Assert.isNotNull(parentNode);
		parentNode.childrenQueryRunning = true;
		Tcf.getChannelManager().openChannel(parentNode.peerNode.getPeer(), false, new SysDoneOpenChannel(parentNode));
	}

	/**
	 * Creates a node from the given system monitor context.
	 * 
	 * @param context The system monitor context. Must be not <code>null</code>.
	 * 
	 * @return The node.
	 */
	protected ProcessesTreeNode createNodeFromSysMonitorContext(ISysMonitor.SysMonitorContext context) {
		Assert.isTrue(Protocol.isDispatchThread());
		Assert.isNotNull(context);

		ProcessesTreeNode node = new ProcessesTreeNode();

		node.childrenQueried = false;
		node.childrenQueryRunning = false;
		node.context = context;
		node.name = context.getFile();
		node.type = "ProcNode"; //$NON-NLS-1$
		node.id = context.getID();
		node.pid = context.getPID();
		node.ppid = context.getPPID();
		node.parentId = context.getParentID();
		node.state = context.getState();
		node.username = context.getUserName();

		return node;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(Object element) {
		Assert.isNotNull(element);

		boolean hasChildren = false;

		// No children yet and the element is a process node
		if (element instanceof ProcessesTreeNode) {
			ProcessesTreeNode node = (ProcessesTreeNode) element;
			if (!node.childrenQueried || node.childrenQueryRunning) {
				hasChildren = true;
			}
			else if (node.childrenQueried) {
				hasChildren = node.children.size() > 0;
			}
		}

		return hasChildren;
	}
}
