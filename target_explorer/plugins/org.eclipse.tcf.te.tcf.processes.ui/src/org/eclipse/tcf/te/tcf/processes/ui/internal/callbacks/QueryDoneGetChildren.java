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

import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IProcesses;
import org.eclipse.tcf.services.ISysMonitor;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessModel;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessTreeNode;

/**
 * The callback handler that handles the result of service.getChildren when querying.
 */
public class QueryDoneGetChildren implements ISysMonitor.DoneGetChildren {
	// The channel used for query.
	IChannel channel;
	// The service used for query.
	ISysMonitor service;
	// The parent node to be queried.
	ProcessTreeNode parentNode;
	// The process model attached.
	ProcessModel model;
	
	/**
	 * Create an instance with the field parameters.
	 */
	public QueryDoneGetChildren(ProcessModel model, IChannel channel, ISysMonitor service, ProcessTreeNode parentNode) {
		this.model = model;
		this.channel = channel;
		this.service = service;
		this.parentNode = parentNode;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.ISysMonitor.DoneGetChildren#doneGetChildren(org.eclipse.tcf.protocol.IToken, java.lang.Exception, java.lang.String[])
	 */
    @Override
    public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
        if (error == null && context_ids != null && context_ids.length > 0) {
			IProcesses pService = channel.getRemoteService(IProcesses.class);
			if (pService != null) {
	        	Map<String, Boolean> status = createStatusMap(context_ids);
				for (String contextId : context_ids) {
					QueryDoneGetContext done = new QueryDoneGetContext(model, contextId, channel, status, parentNode); 
					service.getContext(contextId, done);
					pService.getContext(contextId, done);
				}
			}
    	} else {
            parentNode.childrenQueryRunning = false;
            parentNode.childrenQueried = true;
			model.firePropertyChanged(parentNode);
            Tcf.getChannelManager().closeChannel(channel);
    	}
    }

    /**
     * Create and initialize a status map with all the context ids and completion status
     * set to false.
     * 
     * @param context_ids All the context ids.
     * @return A map with initial values
     */
	private Map<String, Boolean> createStatusMap(String[] context_ids) {
        Map<String, Boolean> status = new HashMap<String, Boolean>();
        for (String contextId : context_ids) {
        	status.put(contextId, Boolean.FALSE);
        }
        return status;
    }		
}