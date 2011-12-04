/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.stepper.stepper;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.stepper.StepperAttributeUtil;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IContext;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IContextManipulator;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IContextStepper;

/**
 * A stepper implementation capable of handling multiple contexts.
 * <p>
 * The multi context stepper will sequential loop all configured contexts and invoke a single
 * context stepper for each context.
 */
public class MultiContextStepper extends SingleContextStepper {

	private List<IContextStepper> steppers = new ArrayList<IContextStepper>();

	public static final String CONTEXT_COUNT = "contextCount"; //$NON-NLS-1$
	public static final String CONTEXT_INDEX = "contextIndex"; //$NON-NLS-1$

	/**
	 * Constructor.
	 */
	public MultiContextStepper() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.extensions.AbstractContextStepper#onInitialize(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void onInitialize(IPropertiesContainer data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor) {
	    super.onInitialize(data, fullQualifiedId, monitor);

	    IContext[] contexts = getContexts();
	    for (IContext context : contexts) {
	    	if (context == null) continue;

	    	// Get the stepper to be used for this context object
	    	IContextStepper stepper = getStepper(context);
	    	Assert.isNotNull(stepper);

	    	// Build up a new fully qualified id
			IFullQualifiedId id = fullQualifiedId.createChildId(ID_TYPE_STEPPER_ID,
									(stepper.getId() != null ? stepper.getId() : "org.eclipse.tcf.te.runtime.stepper.singleContext"), //$NON-NLS-1$
									"" + steppers.size()); //$NON-NLS-1$

			// Initialize the stepper
			stepper.initialize(data, id, monitor);

			// Associate the context and context id with the calculated id
			StepperAttributeUtil.setProperty(IContextManipulator.CONTEXT, id, data, context);
			StepperAttributeUtil.setProperty(IContextManipulator.CONTEXT_ID, id, data, context.getContextId());

			// Add the stepper to the list
			steppers.add(stepper);
	    }
		data.setProperty("MULTI_CONTEXT", Boolean.toString(steppers.size() > 1)); //$NON-NLS-1$
	}

	/**
	 * Returns the stepper to be used for the given target context.
	 * <p>
	 * <b>Note:</b> The default implementation does always return a new single context stepper
	 * instance.
	 *
	 * @param context The context. Must not be <code>null</code>.
	 * @return The stepper instance.
	 */
	protected IContextStepper getStepper(IContext context) {
		Assert.isNotNull(context);
		return new SingleContextStepper();
	}

	/* (non-Javadoc)
	 * @see com.windriver.ide.target.core.stepper.AbstractTargetContextStepper#internalExecute(java.util.List)
	 */
	@Override
	protected void internalExecute(List<IStatus> statusContainer) throws CoreException {
		int i = 0;
		StepperAttributeUtil.setProperty(CONTEXT_COUNT, null, getData(), steppers.size());
		for (IContextStepper stepper : steppers) {
			StepperAttributeUtil.setProperty(CONTEXT_INDEX, null, getData(), i++);
			try {
				stepper.execute();
			}
			catch (CoreException e) {
				if (isCancelable() && getMonitor() != null && getMonitor().isCanceled()) {
					throw e;
				}
				statusContainer.add(e.getStatus());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.extensions.AbstractContextStepper#cleanup()
	 */
	@Override
	public void cleanup() {
		super.cleanup();

		for (IContextStepper stepper : steppers) {
			stepper.cleanup();
		}
		steppers.clear();
	}
}
