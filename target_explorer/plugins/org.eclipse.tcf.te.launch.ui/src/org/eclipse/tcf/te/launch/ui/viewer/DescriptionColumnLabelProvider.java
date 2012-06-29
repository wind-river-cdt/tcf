/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.viewer;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.tcf.te.launch.core.lm.LaunchConfigHelper;
import org.eclipse.tcf.te.launch.core.lm.LaunchManager;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchManagerDelegate;
import org.eclipse.tcf.te.launch.ui.model.LaunchNode;

/**
 * The label provider for the tree column "description".
 */
public class DescriptionColumnLabelProvider extends LabelProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof LaunchNode && (((LaunchNode)element).isType(LaunchNode.TYPE_LAUNCH_CONFIG))) {
			String[] modes = LaunchConfigHelper.getLaunchConfigTypeModes(((LaunchNode)element).getLaunchConfigurationType(), false);
			if (modes != null && modes.length > 0) {
				ILaunchManagerDelegate delegate = LaunchManager.getInstance().getLaunchManagerDelegate(((LaunchNode)element).getLaunchConfigurationType(), modes[0]);
				return delegate.getDescription(((LaunchNode)element).getLaunchConfiguration());
			}
		}
		return ""; //$NON-NLS-1$
	}
}
