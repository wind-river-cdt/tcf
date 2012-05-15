/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.core.callbacks;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IProcesses;
import org.eclipse.tcf.services.ISysMonitor;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.tcf.processes.core.activator.CoreBundleActivator;
import org.eclipse.tcf.te.tcf.processes.core.model.ProcessTreeNode;

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
	// The callback object
	ICallback callback;
	/**
	 * Create an instance with the field parameters.
	 */
	public QueryDoneGetChildren(ICallback callback, IChannel channel, ISysMonitor service, ProcessTreeNode parentNode) {
		this.callback = callback;
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
					CallbackMonitor monitor = new CallbackMonitor(callback, (Object[]) contextIds);
					for (String contextId : contextIds) {
						QueryDoneGetContext done = new QueryDoneGetContext(contextId, channel, monitor, parentNode);
						service.getContext(contextId, done);
						pService.getContext(contextId, done);
					}
				}
			}
			else {
				parentNode.clearChildren();
				if(callback != null) {
					callback.done(this, Status.OK_STATUS);
				}
	 		}
		}
		else if (callback != null) {
			IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), error.getMessage(), error);
			callback.done(this, status);
		}
    }
}