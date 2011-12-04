/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tests.stepper;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.stepper.extensions.AbstractContextStep;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IContext;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId;

/**
 * Empty test step contribution.
 */
public class TestStep extends AbstractContextStep {

	/* (non-Javadoc)
     * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IExtendedContextStep#validateExecute(org.eclipse.tcf.te.runtime.stepper.interfaces.IContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void validateExecute(IContext context, IPropertiesContainer data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor) throws CoreException {
    }

	/* (non-Javadoc)
     * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IContextStep#execute(org.eclipse.tcf.te.runtime.stepper.interfaces.IContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
     */
    @Override
    public void execute(IContext context, IPropertiesContainer data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor, ICallback callback) {
    }

}
