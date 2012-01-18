/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.model.interfaces;

import org.eclipse.tcf.te.runtime.model.interfaces.IContainerModelNode;
import org.eclipse.tcf.te.runtime.model.interfaces.factory.IFactory;
import org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelService;


/**
 * Common interface to be implemented by models.
 */
public interface IModel extends IContainerModelNode {

	/**
	 * Dispose the model instance.
	 */
	public void dispose();

	/**
	 * Returns if or if not the model instance is disposed.
	 *
	 * @return <code>True</code> if the model instance is disposed, <code>false/code> otherwise.
	 */
	public boolean isDisposed();

	/**
	 * Returns the model service, implementing at least the specified service interface.
	 *
	 * @param serviceInterface The service interface class. Must not be <code>null</code>.
	 * @return The service instance implementing the specified service interface, or <code>null</code>.
	 */
	public <V extends IModelService> V getService(Class<V> serviceInterface);

	/**
	 * Sets the model node factory instance to be used for creating model nodes. If set to
	 * <code>null</code>, the next call to {@link #getFactory()} will return the default model node
	 * factory.
	 *
	 * @param factory The factory instance or <code<null</code>.
	 */
	public void setFactory(IFactory factory);

	/**
	 * Returns the model node factory instance used for creating model nodes. If not set explicitly,
	 * a default model node factory will be instantiated.
	 *
	 * @return The model node factory instance.
	 */
	public IFactory getFactory();
}
