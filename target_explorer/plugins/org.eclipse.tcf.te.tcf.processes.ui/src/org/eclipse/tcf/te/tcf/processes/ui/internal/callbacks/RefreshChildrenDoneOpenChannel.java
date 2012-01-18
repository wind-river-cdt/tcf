/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.internal.callbacks;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.ISysMonitor;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessModel;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessTreeNode;

/**
 * The callback handler that handles the event when the channel opens when refreshing.
 */
public class RefreshChildrenDoneOpenChannel implements IChannelManager.DoneOpenChannel {
	private static final int PROGRESSIVE_STEP_COUNT = 10;
	// The parent node to be refreshed.
	ProcessTreeNode parentNode;
	Map<String, Boolean> status;
	ProcessModel model;
	/**
	 * Create an instance with the specified field parameters.
	 */
	public RefreshChildrenDoneOpenChannel(ProcessTreeNode parentNode) {
		this.parentNode = parentNode;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel#doneOpenChannel(java.lang.Throwable, org.eclipse.tcf.protocol.IChannel)
	 */
	@Override
	public void doneOpenChannel(Throwable error, final IChannel channel) {
		Assert.isTrue(Protocol.isDispatchThread());
		if (error == null && channel != null) {
			ISysMonitor service = channel.getRemoteService(ISysMonitor.class);
			if (service != null) {
				model = ProcessModel.getProcessModel(parentNode.peerNode);
				status = createStatusMap();
				final String[] contextId = new String[1];
				for (ProcessTreeNode child : parentNode.children) {
					if (!child.childrenQueried && !child.childrenQueryRunning) {
						Queue<ProcessTreeNode> queue = new ConcurrentLinkedQueue<ProcessTreeNode>();
						contextId[0] = child.id;
						service.getChildren(contextId[0], new RefreshDoneGetChildren(model, new Runnable() {
							private String contextID = contextId[0];
							@Override
							public void run() {
								setAndCheckStatus(contextID, channel);
							}
						}, queue, channel, service, child));
					}
				}
			}
		}
	}

    /**
     * Set the complete flag for this context id and check if
     * all tasks have completed.
     */
    void setAndCheckStatus(String contextId, IChannel channel) {
		synchronized (status) {
			status.put(contextId, Boolean.TRUE);
			boolean completed = true;
			int count = 0;
			for (String id : status.keySet()) {
				Boolean bool = status.get(id);
				if (bool.booleanValue()) {
					count++;
				}
				else {
					completed = false;
				}
			}
			if (completed || (count % PROGRESSIVE_STEP_COUNT) == 0) {
				model.firePropertyChanged(parentNode);
			}

			if (completed) {
				Tcf.getChannelManager().closeChannel(channel);
				model.firePropertyChanged(parentNode);
			}
		}
	}

	/**
     * Create and initialize a status map with all the context ids and completion status
     * set to false.
     */
	private Map<String, Boolean> createStatusMap() {
        Map<String, Boolean> status = new HashMap<String, Boolean>();
        for (ProcessTreeNode child : parentNode.children) {
        	String contextId = child.id;
        	status.put(contextId, Boolean.FALSE);
        }
        return status;
    }	
}
