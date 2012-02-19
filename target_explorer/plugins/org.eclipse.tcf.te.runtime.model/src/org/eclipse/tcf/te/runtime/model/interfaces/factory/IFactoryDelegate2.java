/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.model.interfaces.factory;

import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;

/**
 * Interface to be implemented by model node factory delegates.
 */
public interface IFactoryDelegate2 extends IFactoryDelegate {

	/**
	 * Returns a new instance of an node object implementing the given node interface.
	 * <p>
	 * If <code>args</code> is <code>null</code>, the node object returned by this method
	 * should be the same as by calling {@link #newInstance(Class)}.
	 * <p>
	 * If <code>args</code> is not <code>null</code>, the method is matching the argument
	 * types with the argument types of the node object constructor(s) and call the matched
	 * constructor. If no constructor is matching the argument types, the method does return
	 * <code>null</code>.
	 *
	 * @param nodeInterface The node interface to be implemented by the node object to be created.
	 *                      Must not be <code>null</code>.
	 * @param args The arguments to be passed to a matching constructor, or <code>null</code>.
	 *
	 * @return The node object implementing the specified node interface or <code>null</code>.
	 */
	public <V extends IModelNode> V newInstance(Class<V> nodeInterface, Object[] args);
}
