/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.ISysMonitor;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.processes.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.processes.ui.interfaces.INodeStateListener;

/**
 * The process tree model implementation.
 */
public final class ProcessTreeModel {
	/* default */static final String PROCESS_ROOT_KEY = UIPlugin.getUniqueIdentifier() + ".process.root"; //$NON-NLS-1$
	// All of the process tree models used for global notification.
	private static List<ProcessTreeModel> allModels;

	/**
	 * Add a process tree model to the list.
	 * 
	 * @param model The new process tree model.
	 */
	private static void addModel(ProcessTreeModel model) {
		if (allModels == null) {
			allModels = Collections.synchronizedList(new ArrayList<ProcessTreeModel>());
		}
		allModels.add(model);
	}

	/**
	 * Notify all the process tree models that the model has changed.
	 */
	public static void notifyAllChanged() {
		if (allModels != null) {
			for (ProcessTreeModel model : allModels) {
				model.fireNodeStateChanged(null);
			}
		}
	}
	
	/**
	 * Remove the listener from the models.
	 * 
	 * @param listener The listener.
	 */
	public static void removeAllListener(INodeStateListener listener) {
		if (allModels != null) {
			for (ProcessTreeModel model : allModels) {
				model.removeNodeStateListener(listener);
			}
		}
	}

	/**
	 * Get the root node stored in the peer model.
	 * 
	 * @param peerModel The target's peer model.
	 * @return The root node representing the processes.
	 */
	public static ProcessTreeModel getProcessTreeModel(final IPeerModel peerModel) {
		if (peerModel != null) {
			if (Protocol.isDispatchThread()) {
				ProcessTreeModel model = (ProcessTreeModel) peerModel.getProperty(PROCESS_ROOT_KEY);
				if (model == null) {
					model = new ProcessTreeModel(peerModel);
					peerModel.setProperty(PROCESS_ROOT_KEY, model);
				}
				return model;
			}
			final AtomicReference<ProcessTreeModel> reference = new AtomicReference<ProcessTreeModel>();
			Protocol.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					reference.set(getProcessTreeModel(peerModel));
				}
			});
			return reference.get();
		}
		return null;
	}

	// Node state listeners.
	private List<INodeStateListener> listeners;
	// The root node of the peer model
	private ProcessTreeNode root;

	// The peer model it attaches to.
	private IPeerModel peerModel;
	/**
	 * Create a File System Model.
	 */
	private ProcessTreeModel(IPeerModel peerModel) {
		listeners = Collections.synchronizedList(new ArrayList<INodeStateListener>());
		this.peerModel = peerModel;
		addModel(this);
	}

	/**
	 * Get the root node of the peer model.
	 * 
	 * @return The root node.
	 */
	public ProcessTreeNode getRoot() {
		return root;
	}

	/**
	 * Set the root node of the peer model.
	 * 
	 * @param root The root node
	 */
	public void createRoot(IPeerModel peerModel ) {
		ProcessTreeNode root = new ProcessTreeNode();
		root.type = "ProcRootNode"; //$NON-NLS-1$
		root.peerNode = peerModel;
		root.childrenQueried = false;
		root.childrenQueryRunning = false;
		this.root = root;
	}

	/**
	 * Add an INodeStateListener to the File System model if it is not in the listener list yet.
	 * 
	 * @param listener The INodeStateListener to be added.
	 */
	public void addNodeStateListener(INodeStateListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	/**
	 * Remove the INodeStateListener from the File System model if it exists in the listener list.
	 * 
	 * @param listener The INodeStateListener to be removed.
	 */
	public void removeNodeStateListener(INodeStateListener listener) {
		if (listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}

	/**
	 * Fire a node state changed event with the specified node.
	 * 
	 * @param node The node whose state has changed.
	 */
	/* default */ void fireNodeStateChanged(ProcessTreeNode node) {
		synchronized (listeners) {
			for (INodeStateListener listener : listeners) {
				listener.stateChanged(node);
			}
		}
	}
	
	
	/**
	 * The callback handler that handles the result of service.getContext.
	 */
	class SysDoneGetContext implements ISysMonitor.DoneGetContext {
		private IChannel channel;
		private ProcessTreeNode parentNode;
		private Iterator<String> iterator;
		private ISysMonitor service;
		public SysDoneGetContext(IChannel channel, ISysMonitor service, Iterator<String> iterator, ProcessTreeNode parentNode) {
			this.channel = channel;
			this.service = service;
			this.iterator = iterator;
			this.parentNode = parentNode;
		}
        @Override
        public void doneGetContext(IToken token, Exception error, ISysMonitor.SysMonitorContext context) {
            if (error == null && context != null) {
                ProcessTreeNode childNode = createNodeForContext(context);
                childNode.parent = parentNode;
                childNode.peerNode = parentNode.peerNode;
                parentNode.children.add(childNode);
        		fireNodeStateChanged(parentNode);
            }
            if(iterator.hasNext()) {
            	String contextId = iterator.next();
            	service.getContext(contextId, this);
            } else {
                parentNode.childrenQueryRunning = false;
                parentNode.childrenQueried = true;
                Tcf.getChannelManager().closeChannel(channel);
            }
        }
        
    	/**
    	 * Creates a node from the given system monitor context.
    	 * 
    	 * @param context The system monitor context. Must be not <code>null</code>.
    	 * 
    	 * @return The node.
    	 */
    	/* default */ ProcessTreeNode createNodeForContext(ISysMonitor.SysMonitorContext context) {
    		Assert.isTrue(Protocol.isDispatchThread());
    		Assert.isNotNull(context);

    		ProcessTreeNode node = new ProcessTreeNode();

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
	}
	
	/**
	 * The callback handler that handles the result of service.getChildren.
	 */
	class SysDoneGetChildren implements ISysMonitor.DoneGetChildren {
		IChannel channel;
		ISysMonitor service;
		ProcessTreeNode parentNode;
		public SysDoneGetChildren(IChannel channel, ISysMonitor service, ProcessTreeNode parentNode) {
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
        		fireNodeStateChanged(parentNode);
        	}
        }		
	}
	
	/**
	 * The callback handler that handles the event when the channel opens.
	 */
	class SysDoneOpenChannel implements IChannelManager.DoneOpenChannel {
		ProcessTreeNode parentNode;
		public SysDoneOpenChannel(ProcessTreeNode parentNode) {
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
	public void queryChildren(final ProcessTreeNode parentNode) {
		Assert.isNotNull(parentNode);
		parentNode.childrenQueryRunning = true;
		Tcf.getChannelManager().openChannel(peerModel.getPeer(), false, new SysDoneOpenChannel(parentNode));
	}
}
