/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.core.adapters;

import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistableNameProvider;

/**
 *  Model node persistable name provider implementation.
 */
public class ModelNodePersistableNameProvider implements IPersistableNameProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistableNameProvider#getName(java.lang.Object)
	 */
	@Override
	public String getName(Object data) {
		String name = null;

		// Only model nodes are supported
		if (data instanceof IModelNode) {
			IModelNode node = (IModelNode)data;

			// Check for the id property first.
			name = node.getStringProperty(IModelNode.PROPERTY_ID);
			// If the id is not set, check for the node name
			if (name == null || "".equals(name.trim())) { //$NON-NLS-1$
				name = node.getName();
			}
		}

		return name;
	}

}
