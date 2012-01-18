/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.model.contexts;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.te.runtime.model.PendingOperationModelNode;
import org.eclipse.tcf.te.tcf.core.model.interfaces.contexts.IAsyncRefreshableCtx;


/**
 * An asynchronous refreshable context adapter implementation.
 */
public class AsyncRefreshableCtxAdapter implements IAsyncRefreshableCtx {

	// The flags representing the refreshable context states
	private final Map<QueryType, QueryState> states = new HashMap<QueryType, QueryState>();

	// The reference to the pending operation model node
	private PendingOperationModelNode pendingNode = null;

	/**
     * Constructor.
     */
    public AsyncRefreshableCtxAdapter() {
    	states.put(QueryType.CONTEXT, QueryState.PENDING);
    	states.put(QueryType.CHILD_LIST, QueryState.PENDING);
    }

    /* (non-Javadoc)
     * @see org.eclipse.tcf.te.tcf.core.model.interfaces.contexts.IAsyncRefreshableCtx#getQueryState(org.eclipse.tcf.te.tcf.core.model.interfaces.contexts.IAsyncRefreshableCtx.QueryType)
     */
    @Override
    public QueryState getQueryState(QueryType type) {
    	Assert.isNotNull(type);
        return states.get(type);
    }

    /* (non-Javadoc)
     * @see org.eclipse.tcf.te.tcf.core.model.interfaces.contexts.IAsyncRefreshableCtx#setQueryState(org.eclipse.tcf.te.tcf.core.model.interfaces.contexts.IAsyncRefreshableCtx.QueryType, org.eclipse.tcf.te.tcf.core.model.interfaces.contexts.IAsyncRefreshableCtx.QueryState)
     */
    @Override
    public boolean setQueryState(QueryType type, QueryState state) {
    	Assert.isNotNull(type);
    	Assert.isNotNull(state);

    	if (!states.get(type).equals(state)) {
    		states.put(type, state);
    		return true;
    	}

        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.tcf.te.tcf.core.model.interfaces.contexts.IAsyncRefreshableCtx#getPendingOperationNode()
     */
    @Override
    public PendingOperationModelNode getPendingOperationNode() {
        return pendingNode;
    }

    /* (non-Javadoc)
     * @see org.eclipse.tcf.te.tcf.core.model.interfaces.contexts.IAsyncRefreshableCtx#setPendingOperationNode(org.eclipse.tcf.te.runtime.model.PendingOperationModelNode)
     */
    @Override
    public void setPendingOperationNode(PendingOperationModelNode pendingNode) {
    	this.pendingNode = pendingNode;
    }
}
