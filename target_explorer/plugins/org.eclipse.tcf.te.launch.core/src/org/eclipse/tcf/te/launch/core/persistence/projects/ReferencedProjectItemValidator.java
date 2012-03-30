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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.te.launch.core.interfaces.IReferencedProjectItem;
import org.eclipse.tcf.te.launch.core.nls.Messages;

/**
 * ReferencedProjectItemValidator
 * @author tobias.schwarz@windriver.com
 */
public class ReferencedProjectItemValidator {

	public static final Map<String,String> validate(IReferencedProjectItem item) {
		Assert.isNotNull(item);

		Map<String,String> invalid = new HashMap<String,String>();

		String projectName = item.getStringProperty(IReferencedProjectItem.PROPERTY_PROJECT_NAME);

		if (projectName == null || projectName.trim().length() == 0) {
			invalid.put(IReferencedProjectItem.PROPERTY_PROJECT_NAME, Messages.ReferencedProjectItemValidator_missingProject);
		}
		else {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			if (project == null) {
				invalid.put(IReferencedProjectItem.PROPERTY_PROJECT_NAME, Messages.ReferencedProjectItemValidator_notExistingProject);
			}
			else if (!project.isOpen()) {
				invalid.put(IReferencedProjectItem.PROPERTY_PROJECT_NAME, Messages.ReferencedProjectItemValidator_closedProject);
			}
		}

		return invalid.isEmpty() ? null : invalid;
	}
}
