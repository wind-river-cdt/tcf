/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.services.internal;

import org.eclipse.tcf.te.runtime.services.ServiceManager;


/**
 * Services plug-in property tester implementation.
 */
public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

		// "hasService": Checks if a given service type is available for the given receiver.
		if ("hasService".equals(property)) { //$NON-NLS-1$
			// The service type class name is within the expected value
			String serviceTypeName = expectedValue instanceof String ? (String)expectedValue : null;
			if (serviceTypeName != null) {
				return ServiceManager.getInstance().hasService(receiver, serviceTypeName);
			}
		}

		return false;
	}

}
