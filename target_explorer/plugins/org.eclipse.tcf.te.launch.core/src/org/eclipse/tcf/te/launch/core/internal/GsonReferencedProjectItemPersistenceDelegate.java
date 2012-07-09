/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.internal;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tcf.te.launch.core.interfaces.IReferencedProjectItem;
import org.eclipse.tcf.te.launch.core.persistence.projects.ReferencedProjectItem;
import org.eclipse.tcf.te.runtime.persistence.GsonMapPersistenceDelegate;

/**
 * Referenced project item to string delegate implementation.
 */
public class GsonReferencedProjectItemPersistenceDelegate extends GsonMapPersistenceDelegate {

	/**
	 * Constructor.
	 */
	public GsonReferencedProjectItemPersistenceDelegate() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate#getPersistedClass(java.lang.Object)
	 */
	@Override
	public Class<?> getPersistedClass(Object context) {
		return IReferencedProjectItem.class;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.AbstractPropertiesPersistenceDelegate#toMap(java.lang.Object)
	 */
	@Override
	protected Map<String, Object> toMap(final Object context) throws IOException {
		IReferencedProjectItem item = getReferencedProjectItem(context);
		if (item != null) {
			return super.toMap(item.getProperties());
		}

		return new HashMap<String, Object>();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.AbstractPropertiesPersistenceDelegate#fromMap(java.util.Map, java.lang.Object)
	 */
	@Override
	protected Object fromMap(Map<String, Object> map, Object context) throws IOException {
		IReferencedProjectItem item = new ReferencedProjectItem();
		item.setProperties(map);
		return item;
	}

	/**
	 * Get a referenced project item from the given context.
	 *
	 * @param context The context. Must not be <code>null</code>.
	 * @return The referenced project item or <code>null</code>.
	 */
	protected IReferencedProjectItem getReferencedProjectItem(Object context) {
		IReferencedProjectItem item = null;

		if (context instanceof IReferencedProjectItem) {
			item = (IReferencedProjectItem)context;
		}

		return item;
	}
}
