/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.internal.properties;

import org.eclipse.jface.viewers.IFilter;
import org.eclipse.tcf.te.launch.core.lm.interfaces.IReferencedProjectLaunchAttributes;
import org.eclipse.tcf.te.launch.ui.model.LaunchNode;

/**
 * The filter to filter out non launch configuration nodes.
 */
public class RefProjectsFilter implements IFilter {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IFilter#select(java.lang.Object)
	 */
	@Override
	public boolean select(Object toTest) {
		if (toTest instanceof LaunchNode) {
			LaunchNode node = (LaunchNode)toTest;
			try {
				return LaunchNode.TYPE_LAUNCH_CONFIG.equals(node.getType()) &&
								node.getLaunchConfiguration().getAttribute(IReferencedProjectLaunchAttributes.ATTR_REFERENCED_PROJECTS, (String)null) != null;
			}
			catch (Exception e) {
			}
		}
		return false;
	}

}
