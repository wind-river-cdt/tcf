/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.stepper.extensions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.tcf.te.runtime.extensions.ExecutableExtension;
import org.eclipse.tcf.te.runtime.extensions.ExecutableExtensionProxy;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepGroup;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepGroupIterator;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepGroupable;

/**
 * Abstract step group implementation.
 */
public abstract class AbstractStepGroup extends ExecutableExtension implements IStepGroup {

	private ExecutableExtensionProxy<IStepGroupIterator> iteratorProxy = null;

	/**
	 * Constant to be returned in case the step group contains no steps.
	 */
	protected final static IStepGroupable[] NO_STEPS = new IStepGroupable[0];

	/**
	 * Constructor.
	 */
	public AbstractStepGroup() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepGroup#isLocked()
	 */
	@Override
    public boolean isLocked() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.ExecutableExtension#doSetInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
	public void doSetInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
	    super.doSetInitializationData(config, propertyName, data);

		if (iteratorProxy == null) {
			iteratorProxy = new ExecutableExtensionProxy<IStepGroupIterator>(config) {
				@Override
				protected String getExecutableExtensionAttributeName() {
					return "iterator"; //$NON-NLS-1$
				}
			};
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepGroup#getStepGroupIterator()
	 */
	@Override
    public IStepGroupIterator getStepGroupIterator() {
		return iteratorProxy != null ? iteratorProxy.newInstance() : null;
	}
}
