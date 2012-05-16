/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.launch.ui.remote.app;

import org.eclipse.jface.viewers.IFilter;
import org.eclipse.tcf.te.launch.ui.model.LaunchNode;
import org.eclipse.tcf.te.tcf.launch.core.interfaces.ILaunchTypes;

/**
 * The filter to filter out non launch configuration nodes.
 */
public class RemoteAppFilter implements IFilter {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IFilter#select(java.lang.Object)
	 */
	@Override
	public boolean select(Object toTest) {
		if (toTest instanceof LaunchNode) {
			LaunchNode node = (LaunchNode)toTest;
			return node.isType(LaunchNode.TYPE_LAUNCH_CONFIG) &&
							node.getLaunchConfigurationType().getIdentifier().equals(ILaunchTypes.REMOTE_APPLICATION);
		}
		return false;
	}

}
