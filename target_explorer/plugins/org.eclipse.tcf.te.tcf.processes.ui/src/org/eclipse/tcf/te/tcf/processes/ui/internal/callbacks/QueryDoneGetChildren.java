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

import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IProcesses;
import org.eclipse.tcf.services.ISysMonitor;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessTreeNode;

/**
 * The callback handler that handles the result of service.getChildren when querying.
 */
public class QueryDoneGetChildren implements ISysMonitor.DoneGetChildren, Runnable {
	// The channel used for query.
	IChannel channel;
	// The service used for query.
	ISysMonitor service;
	// The parent node to be queried.
	ProcessTreeNode parentNode;
	/**
	 * Create an instance with the field parameters.
	 */
	public QueryDoneGetChildren(IChannel channel, ISysMonitor service, ProcessTreeNode parentNode) {
		this.channel = channel;
		this.service = service;
		this.parentNode = parentNode;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.ISysMonitor.DoneGetChildren#doneGetChildren(org.eclipse.tcf.protocol.IToken, java.lang.Exception, java.lang.String[])
	 */
    @Override
    public void doneGetChildren(IToken token, Exception error, String[] contextIds) {
        if (error == null) {
			if (contextIds != null && contextIds.length > 0) {
				IProcesses pService = channel.getRemoteService(IProcesses.class);
				if (pService != null) {
					CallbackMonitor monitor = new CallbackMonitor(this, (Object[]) contextIds);
					for (String contextId : contextIds) {
						QueryDoneGetContext done = new QueryDoneGetContext(contextId, channel, monitor, parentNode);
						service.getContext(contextId, done);
						pService.getContext(contextId, done);
					}
				}
			}
			else {
				parentNode.clearChildren();
				run();
			}
    	} else {
    		run();
    	}
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
	@Override
    public void run() {
		parentNode.childrenQueryRunning = false;
		parentNode.childrenQueried = true;
		Tcf.getChannelManager().closeChannel(channel);
    }
}