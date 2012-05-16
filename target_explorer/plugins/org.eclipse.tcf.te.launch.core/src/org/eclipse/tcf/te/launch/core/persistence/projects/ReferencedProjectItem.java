/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.tcf.te.launch.core.persistence.projects;

import org.eclipse.tcf.te.launch.core.interfaces.IReferencedProjectItem;
import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;

/**
 * ReferencedProjectItem
 */
public class ReferencedProjectItem extends PropertiesContainer implements IReferencedProjectItem {

	/**
	 * Constructor.
	 */
	public ReferencedProjectItem() {
		setProperty(PROPERTY_ENABLED, true);
	}

	public ReferencedProjectItem(String projectName) {
		this();
		setProperty(PROPERTY_PROJECT_NAME, projectName);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.interfaces.IReferencedProjectItem#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return getBooleanProperty(PROPERTY_ENABLED);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.interfaces.IReferencedProjectItem#getProjectName()
	 */
	@Override
	public String getProjectName() {
		return getStringProperty(PROPERTY_PROJECT_NAME);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.properties.PropertiesContainer#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IReferencedProjectItem) {
			return getProjectName().equals(((IReferencedProjectItem)obj).getProjectName());
		}
		return super.equals(obj);
	}
}
