/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.model.interfaces.services;

import java.util.UUID;

import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;

/**
 * Common interface to be implemented by a model lookup service.
 */
public interface IModelLookupService extends IModelService {

	/**
	 * Search the associated model for a model node matching the given UUID.
	 *
	 * @param uuid The UUID. Must not be <code>null</code>.
	 * @return The model node instance, or <code>null</code> if the node cannot be found.
	 */
	public IModelNode lkupModelNodeByUUID(UUID uuid);

	/**
	 * Search the associated model for model nodes matching the given id.
	 *
	 * @param id The id. Must not be <code>null</code>.
	 * @return The model node instances, or an empty list.
	 */
	public IModelNode[] lkupModelNodesById(String id);

	/**
	 * Search the associated model for model nodes matching the given name.
	 *
	 * @param name The name. Must not be <code>null</code>.
	 * @return The model node instances, or an empty list.
	 */
	public IModelNode[] lkupModelNodesByName(String name);
}
