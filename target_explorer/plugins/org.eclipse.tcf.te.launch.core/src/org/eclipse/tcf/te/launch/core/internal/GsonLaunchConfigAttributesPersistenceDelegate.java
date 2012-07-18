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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.tcf.te.runtime.persistence.GsonMapPersistenceDelegate;

/**
 * Launch configuration to string delegate implementation.
 */
public class GsonLaunchConfigAttributesPersistenceDelegate extends GsonMapPersistenceDelegate {

	/**
	 * Constructor.
	 */
	public GsonLaunchConfigAttributesPersistenceDelegate() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate#getPersistedClass(java.lang.Object)
	 */
	@Override
	public Class<?> getPersistedClass(Object context) {
		return ILaunchConfiguration.class;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.AbstractPropertiesPersistenceDelegate#toMap(java.lang.Object)
	 */
	@Override
	protected Map<String, Object> toMap(final Object context) throws IOException {
		if (context instanceof ILaunchConfiguration) {
			try {
				return ((ILaunchConfiguration)context).getAttributes();
			}
			catch (CoreException e) {
			}
		}
		return new HashMap<String, Object>();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.AbstractPropertiesPersistenceDelegate#fromMap(java.util.Map, java.lang.Object)
	 */
	@Override
	protected Object fromMap(Map<String, Object> map, Object context) throws IOException {
		if (context instanceof ILaunchConfigurationWorkingCopy) {
			((ILaunchConfigurationWorkingCopy)context).setAttributes(map);
		}
		return context;
	}
}
