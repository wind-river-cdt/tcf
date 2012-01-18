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

import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;

/**
 * Common interface to be implemented by a model update service.
 */
public interface IModelUpdateService extends IModelService {

	/**
	 * Adds the given node to the model. A previous mapping to a node with the same id as the given
	 * node is overwritten.
	 *
	 * @param node The node object. Must not be <code>null</code>.
	 */
	public void add(IModelNode node);

	/**
	 * Removes the given node from the model.
	 *
	 * @param node The node object. Must not be <code>null</code.
	 */
	public void remove(IModelNode node);

	/**
	 * Updates the destination model node properties from the source model node properties.
	 *
	 * @param dst The destination model node. Must not be <code>null</code>.
	 * @param src The source model node. Must not be <code>null</code>.
	 */
	public void update(IModelNode dst, IModelNode src);
}
