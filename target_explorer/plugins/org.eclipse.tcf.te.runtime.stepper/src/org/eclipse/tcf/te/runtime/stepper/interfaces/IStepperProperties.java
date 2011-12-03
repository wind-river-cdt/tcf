/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.stepper.interfaces;

/**
 * Common stepper configuration property definitions.
 * <p>
 * Stepper are configured by setting properties within the properties container passed to the stepper via the
 * {@link IStepper#initialize(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor)}
 * call.
 */
public interface IStepperProperties {

	/**
	 * The id of the step group to execute by the stepper.
	 * <p>
	 * Type: String
	 */
	public static final String PROP_STEP_GROUP_ID = "stepGroupID"; //$NON-NLS-1$

	/**
	 * A name describing to the user what the stepper is executing.
	 * <p>
	 * Type: String
	 */
	public static final String PROP_NAME = "name"; //$NON-NLS-1$

	/**
	 * The context objects the stepper is working on.
	 * <p>
	 * Type: Array of {@link IContext}
	 */
	public static final String PROP_CONTEXTS = "contexts"; //$NON-NLS-1$
}
