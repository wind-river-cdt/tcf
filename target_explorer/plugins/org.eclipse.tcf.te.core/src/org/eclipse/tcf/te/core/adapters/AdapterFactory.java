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

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistableURIProvider;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistableNameProvider;

/**
 * Adapter factory implementation.
 */
public class AdapterFactory implements IAdapterFactory {
	// Reference to the persistable adapter to use
	private final IPersistableURIProvider persistableAdapter = new ModelNodePersistableURIProvider();
	// Reference to the persistable name provider adapter to use
	private final IPersistableNameProvider persistableNameProvider = new ModelNodePersistableNameProvider();

	private static final Class<?>[] CLASSES = new Class[] {
		IPersistableURIProvider.class,
		IPersistableNameProvider.class
	};

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof IModelNode) {
			if (IPersistableURIProvider.class.equals(adapterType)) {
				return persistableAdapter;
			}
			if (IPersistableNameProvider.class.equals(adapterType)) {
				return persistableNameProvider;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	public Class[] getAdapterList() {
		return CLASSES;
	}

}
