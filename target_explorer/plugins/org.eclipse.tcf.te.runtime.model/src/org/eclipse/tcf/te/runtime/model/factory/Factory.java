/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.model.factory;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.runtime.model.interfaces.factory.IFactory;
import org.eclipse.tcf.te.runtime.model.interfaces.factory.IFactoryDelegate;
import org.eclipse.tcf.te.runtime.model.internal.factory.FactoryDelegateManager;

/**
 * Model node factory implementation.
 */
public final class Factory extends PlatformObject implements IFactory {
	private final FactoryDelegateManager manager = new FactoryDelegateManager();

	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstance {
		public static Factory instance = new Factory();
	}

	/**
	 * Returns the singleton instance of the service manager.
	 */
	public static Factory getInstance() {
		return LazyInstance.instance;
	}

	/**
	 * Constructor.
	 */
	Factory() {
		super();
	}

	/**
	 * Creates an new instance of the model node object implementing
	 * the specified node interface.
	 *
	 * @param nodeInterface The node interface to be implemented by the model node object to create.
	 *                      Must not be <code>null</code>.
	 * @return The model not object implementing the specified node interface or <code>null</code>.
	 */
	@Override
	public <V extends IModelNode> V newInstance(Class<V> nodeInterface) {
		Assert.isNotNull(nodeInterface);

		// Determine the model node factory delegate to use
		IFactoryDelegate delegate = manager.getFactoryDelegate(nodeInterface);
		// Return the model node instance
		return delegate != null ? (V)delegate.newInstance(nodeInterface) : null;
	}
}
