/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.internal.viewer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ICommonLaunchAttributes;
import org.eclipse.tcf.te.launch.core.persistence.DefaultPersistenceDelegate;
import org.eclipse.tcf.te.launch.ui.model.LaunchNode;

/**
 * The label provider for the tree column "lastLaunched".
 */
public class LastLaunchedColumnLabelProvider extends LabelProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof LaunchNode && (LaunchNode.TYPE_LAUNCH_CONFIG.equals(((LaunchNode)element).getType()))) {
			try {
				String lastLaunched = DefaultPersistenceDelegate.getAttribute(((LaunchNode)element).getLaunchConfiguration(), ICommonLaunchAttributes.ATTR_LAST_LAUNCHED, (String)null);
				if (lastLaunched != null) {
					DateFormat format = new SimpleDateFormat();
					return format.format(new Date(Long.parseLong(lastLaunched)));
				}
			}
			catch (Exception e) {
			}
		}
		return ""; //$NON-NLS-1$
	}
}
