/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.model.interfaces.factory;

import org.eclipse.tcf.te.runtime.interfaces.extensions.IExecutableExtension;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;

/**
 * Interface to be implemented by model node factory delegates.
 */
public interface IFactoryDelegate extends IExecutableExtension {

	/**
	 * Returns a new instance of an node object implementing the given node interface.
	 *
	 * @param nodeInterface The node interface to be implemented by the node object to be created.
	 *                      Must not be <code>null</code>.
	 * @return The node object implementing the specified node interface or <code>null</code>.
	 */
	public <V extends IModelNode> V newInstance(Class<V> nodeInterface);
}
