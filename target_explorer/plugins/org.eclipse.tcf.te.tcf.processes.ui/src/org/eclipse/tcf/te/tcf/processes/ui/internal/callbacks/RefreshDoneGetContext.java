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

import java.util.List;

import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IProcesses;
import org.eclipse.tcf.services.IProcesses.ProcessContext;
import org.eclipse.tcf.services.ISysMonitor;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessTreeNode;

/**
 * The callback handler that handles the result of service.getContext when refreshing.
 */
public class RefreshDoneGetContext implements ISysMonitor.DoneGetContext, IProcesses.DoneGetContext {
	// The current context id.
	String contextId;
	// The parent node to be refreshed.
	ProcessTreeNode parentNode;
	// The list to record all new nodes for merging.
	List<ProcessTreeNode> newNodes;
	// The current child node
	ProcessTreeNode childNode;
	// The process context of this child node.
	ProcessContext pContext;
	// The flag to indicate if the system monitor service has returned. 
	volatile boolean sysMonitorDone;
	// The flag to indicate if the process service has returned.
	volatile boolean processesDone;
	// The callback monitor
	CallbackMonitor monitor;
	/**
	 * Create an instance with the field parameters.
	 */
	public RefreshDoneGetContext(List<ProcessTreeNode> newNodes, String contextId,  CallbackMonitor monitor, ProcessTreeNode parentNode) {
		this.contextId = contextId;
		this.parentNode = parentNode;
		this.monitor = monitor;
		this.newNodes = newNodes;
		this.sysMonitorDone = false;
		this.processesDone = false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.ISysMonitor.DoneGetContext#doneGetContext(org.eclipse.tcf.protocol.IToken, java.lang.Exception, org.eclipse.tcf.services.ISysMonitor.SysMonitorContext)
	 */
    @Override
	public void doneGetContext(IToken token, Exception error, ISysMonitor.SysMonitorContext context) {
		if (error == null && context != null) {
			childNode = new ProcessTreeNode(parentNode, context);
            final int index = searchChild(childNode);
			if (index != -1) {
				ProcessTreeNode node = parentNode.getChildren().get(index);
				node.updateData(context);
				childNode = node;
			}
			else parentNode.addChild(childNode);
			newNodes.add(childNode);
		}
		sysMonitorDone = true;
		if(sysMonitorDone && processesDone) {
			if (childNode != null) {
				childNode.setProcessContext(pContext);
			}
			monitor.unlock(contextId);
		}
	}

    /**
     * Search the specified child node in the children of the parent node.
     *
     * @param childNode The child node.
     * @return The index of the child node or -1 if no such node.
     */
	private int searchChild(ProcessTreeNode childNode) {
		return searchInList(childNode, parentNode.getChildren());
    }    

    /**
     * Search the specified child node in the specified list.
     *
     * @param childNode The child node.
     * @param list The process node list.
     * @return The index of the child node or -1 if no such node.
     */
    private int searchInList(ProcessTreeNode childNode, List<ProcessTreeNode> list) {
		synchronized (list) {
			for (int i = 0; i < list.size(); i++) {
				ProcessTreeNode node = list.get(i);
				if (childNode.id.equals(node.id)) {
					return i;
				}
			}
			return -1;
		}
    }
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.IProcesses.DoneGetContext#doneGetContext(org.eclipse.tcf.protocol.IToken, java.lang.Exception, org.eclipse.tcf.services.IProcesses.ProcessContext)
	 */
	@Override
    public void doneGetContext(IToken token, Exception error, ProcessContext context) {
		if (error == null && context != null) {
			pContext = context;
		}
		processesDone = true;
		if(sysMonitorDone && processesDone) {
			if (childNode != null) {
				childNode.setProcessContext(pContext);
			}
			monitor.unlock(contextId);
		}
    }
}
