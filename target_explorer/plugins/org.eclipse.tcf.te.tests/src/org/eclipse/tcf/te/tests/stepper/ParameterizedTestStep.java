/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tests.stepper;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Parameterized test step implementation.
 */
public class ParameterizedTestStep extends TestStep {
	public Map<?,?> params = null;

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.extensions.AbstractStep#doSetInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
	public void doSetInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
	    super.doSetInitializationData(config, propertyName, data);

	    if (data instanceof Map<?,?>) params = (Map<?,?>)data;
	}
}
