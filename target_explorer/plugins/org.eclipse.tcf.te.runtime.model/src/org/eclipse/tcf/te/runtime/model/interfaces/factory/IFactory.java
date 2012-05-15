/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.model.interfaces.factory;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;

/**
 * Interface to be implemented by model node factories.
 */
public interface IFactory extends IAdaptable {

	/**
	 * Creates an new instance of an node object implementing the specified node interface.
	 *
	 * @param nodeInterface The node interface to be implemented by the node object to be created.
	 *                      Must not be <code>null</code>.
	 * @return The node object implementing the specified node interface or <code>null</code>.
	 */
	public <V extends IModelNode> V newInstance(Class<V> nodeInterface);

	/**
	 * Creates an new instance of an node object implementing the specified node interface.
	 * <p>
	 * <b>Note:</b> Factory delegates must implement {@link IFactoryDelegate2} to be invoked by
	 *              this method.
	 *
	 * @param nodeInterface The node interface to be implemented by the node object to be created.
	 *                      Must not be <code>null</code>.
	 * @param args The arguments to be passed to a matching constructor, or <code>null</code>.
	 *
	 * @return The node object implementing the specified node interface or <code>null</code>.
	 *
	 * @see IFactoryDelegate2#newInstance(Class, Object[])
	 */
	public <V extends IModelNode> V newInstance(Class<V> nodeInterface, Object[] args);
}
