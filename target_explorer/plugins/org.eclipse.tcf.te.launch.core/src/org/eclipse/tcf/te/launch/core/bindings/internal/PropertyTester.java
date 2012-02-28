/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.bindings.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;

/**
 * Launch property tester.
 */
public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
	public boolean test(final Object receiver, String property, Object[] args, Object expectedValue) {

		if (receiver instanceof ILaunch) {
			if ("launchMode".equals(property) && expectedValue instanceof String) { //$NON-NLS-1$
				return ((ILaunch)receiver).getLaunchMode().equalsIgnoreCase((String)expectedValue);
			}
			if ("launchConfigTypeid".equals(property) && expectedValue instanceof String) { //$NON-NLS-1$
				try {
					return ((ILaunch)receiver).getLaunchConfiguration().getType().getIdentifier().equalsIgnoreCase((String)expectedValue);
				}
				catch (CoreException e) {
				}
			}
		}
		return false;
	}
}
