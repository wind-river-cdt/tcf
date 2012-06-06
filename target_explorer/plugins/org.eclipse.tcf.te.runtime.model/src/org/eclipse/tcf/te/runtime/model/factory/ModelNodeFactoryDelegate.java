/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.model.factory;

import org.eclipse.tcf.te.runtime.model.ContainerModelNode;
import org.eclipse.tcf.te.runtime.model.ModelNode;
import org.eclipse.tcf.te.runtime.model.interfaces.IContainerModelNode;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;

/**
 * Default model node factory delegate implementation.
 */
public class ModelNodeFactoryDelegate extends AbstractFactoryDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.interfaces.factory.IFactoryDelegate#newInstance(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
    @Override
	public <V extends IModelNode> V newInstance(Class<V> nodeInterface) {
		if (IModelNode.class.equals(nodeInterface)) {
			return (V) new ModelNode();
		}
		if (IContainerModelNode.class.equals(nodeInterface)) {
			return (V) new ContainerModelNode();
		}
		return null;
	}

}
