/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.model.interfaces.contexts;

import org.eclipse.tcf.te.runtime.model.PendingOperationModelNode;

/**
 * Interface to be implemented or being adaptable to by nodes which are refreshed in an asynchronous
 * manner.
 */
public interface IAsyncRefreshableCtx {

	/**
	 * The query state constants.
	 */
	public enum QueryState {
		PENDING, IN_PROGRESS, DONE
	}

	/**
	 * The query type constants.
	 */
	public enum QueryType {
		CONTEXT, CHILD_LIST
	}

	/**
	 * Returns the state of the given query type.
	 * <p>
	 * The method should return {@link QueryState#PENDING} for all model nodes representing a remote
	 * context object until the remote context object has been queried from the agent for the first
	 * time.
	 *
	 * @param type The query type. Must not be <code>null</code>.
	 * @return The query state for the given query type.
	 */
	public QueryState getQueryState(QueryType type);

	/**
	 * Sets the query state for the given query type.
	 *
	 * @param type The query type. Must not be <code>null</code>.
	 * @param state The new query state. Must not be <code>null</code>.
	 *
	 * @return <code>True</code> if the query state changed, <code>false</code> otherwise.
	 */
	public boolean setQueryState(QueryType type, QueryState state);

	/**
	 * Associates the given pending operation model node.
	 *
	 * @param pendingNode The pending operation model node, or <code>null</code>.
	 */
	public void setPendingOperationNode(PendingOperationModelNode pendingNode);

	/**
	 * Returns the associated pending operation model node.
	 *
	 * @return The pending operation model node or <code>null</code>.
	 */
	public PendingOperationModelNode getPendingOperationNode();
}
