/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.lm.interfaces;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.tcf.te.launch.core.exceptions.LaunchServiceException;
import org.eclipse.tcf.te.launch.core.selection.interfaces.ILaunchSelection;
import org.eclipse.tcf.te.launch.core.selection.interfaces.ISelectionContext;
import org.eclipse.tcf.te.runtime.interfaces.extensions.IExecutableExtension;

/**
 * Describes the interface used by the <code>LaunchManager</code> to create
 * and manage the different types of possible launch configurations in a generalized,
 * simple and abstract way. A <code>ILaunchManagerDelegate</code> is strictly bound
 * to one specific <code>ILaunchConfigurationType</code> and is capable and responsible
 * to initializing and handling launch configurations of this type. A launch manager
 * delegate is contribute through the <code>org.eclipse.tcf.te.launch.core.launchManagerDelegates</code>
 * extension point!
 */
public interface ILaunchManagerDelegate extends IExecutableExtension {

	public static final int SITUATION_BEFORE_LAUNCH = 0;
	public static final int SITUATION_AFTER_LAUNCH_FAILED = 99;

	// Constants for signaling the type of found matches

	/**
	 * Constant to signal to ignore this matching.
	 */
	public final static int IGNORE = -1;

	/**
	 * Constant to signal a no match (no attributes are matching).
	 */
	public final static int NO_MATCH = 0;

	/**
	 * Constant to signal a partial match (some attributes are matching).
	 */
	public final static int PARTIAL_MATCH = 1;

	/**
	 * Constant to signal a full match (all attributes are matching).
	 */
	public final static int FULL_MATCH = 2;

	/**
	 * Initialize the launch configuration attributes based on the specified launch specification. If an launch
	 * configuration attribute is not listed within the specified launch specification, the corresponding attribute is
	 * initialized with an default.
	 *
	 * @param wc The launch configuration working copy to initialize. Must not be <code>null</code>!
	 * @param launchSpec The launch specification. Must not be <code>null</code>!
	 */
	public void initLaunchConfigAttributes(ILaunchConfigurationWorkingCopy wc, ILaunchSpecification launchSpec);

	/**
	 * Updates a found launch configuration based on the specified launch specification.
	 *
	 * @param wc The launch configuration working copy to initialize. Must not be <code>null</code>!
	 * @param launchSpec The launch specification. Must not be <code>null</code>!
	 */
	public void updateLaunchConfigAttributes(ILaunchConfigurationWorkingCopy wc, ILaunchSpecification launchSpec);

	/**
	 * Test the specified attribute if or if not the specified attribute value is an default value or not.
	 *
	 * @param attributeKey The attribute key/name. Must not be <code>null</code>.
	 * @param specValue The launch specification value.
	 * @param confValue The launch configuration value.
	 * @param launchSpec The launch specification which is the source of the <code>specValue</code>. Must not be <code>null</code>.
	 * @param launchConfig The launch configuration which is the source of the <code>confValue</code>. Must not be <code>null</code>.
	 * @param launchMode The launch mode. Default values may differ for different launch modes. Must not be
	 *            <code>null</code>!
	 * @return <code>true</code> if the specified attribute value is the default value, <code>false</code> otherwise.
	 */
	public boolean isDefaultAttribute(String attributeKey, Object specValue, Object confValue, ILaunchSpecification launchSpec, ILaunchConfiguration launchConfig, String launchMode);

	/**
	 * Returns a ranked list of launch configurations that best matches the given launch specification. In case of no
	 * results or a given empty or null launch configuration list an empty list should be returned. If no launch specification
	 * is given, the original list should be returned.
	 *
	 * @param launchSpec The launch specification the launch configurations should match.
	 * @param launchConfigs A full list of launch configurations to check for best matching.
	 * @return List of matching launch configurations starting with best match on first index.
	 * @throws LaunchServiceException
	 */
	public ILaunchConfiguration[] getMatchingLaunchConfigurations(ILaunchSpecification launchSpec, ILaunchConfiguration[] launchConfigs) throws LaunchServiceException;

	/**
	 * Get the default launch configuration name.
	 *
	 * @param launchSpec The launch specification to create a default name for the launch config. Must not be <code>null</code>.
	 * @return The default launch configuration name.
	 */
	public String getDefaultLaunchName(ILaunchSpecification launchSpec);

	/**
	 * Get a launch specification with all needed attributes for this delegate taken from the selection to find or create a new
	 * launch configuration.
	 *
	 * @param launchConfigTypeId The launch configuration type id.
	 * @param launchSelection The selected contexts.
	 * @return Launch specification with attributes set from selected contexts.
	 * @throws LaunchServiceException
	 */
	public ILaunchSpecification getLaunchSpecification(String launchConfigTypeId, ILaunchSelection launchSelection);

	/**
	 * Validates a launch specification.
	 *
	 * @param launchSpec The launch specification to validate.
	 * @throws LaunchServiceException If validation fails with further information about the reason (e.q. missing
	 *             attributes)
	 */
	public void validate(ILaunchSpecification launchSpec) throws LaunchServiceException;

	/**
	 * Validates a launch configuration.
	 *
	 * @param launchConfig The launch configuration to validate.
	 * @param launchMode The launch mode.
	 * @throws LaunchServiceException If validation fails with further information about the reason (e.q. missing
	 *             attributes)
	 */
	public void validate(String launchMode, ILaunchConfiguration launchConfig) throws LaunchServiceException;

	/**
	 * Returns true, if the launch dialog should be shown before launch.
	 * @param situation The situation (before launch, after launch, ..)
	 */
	public boolean showLaunchDialog(int situation);

	/**
	 * Returns true, if the launch dialog should only show the given launch configuration (no launch configuration type tree).
	 */
	public boolean showLaunchConfigOnly();

	/**
	 * Returns <code>true</code> if a dialog should pop up when at least one matching
	 * launch configuration was found.
	 * If <code>false</code> is returned, the first matching launch configuration will be used.
	 *
	 * @param type The launch configuration type to check.
	 */
	public boolean showLaunchConfigSelectionDialog(ILaunchConfigurationType type, ILaunchConfiguration[] launchConfigs);

	/**
	 * Returns the error message when not valid, otherwise <code>null</code>.
	 */
	public String getErrorMessage();

	/**
	 * Return <code>true</code> if the two selection contexts are equal
	 * for this launch configuration type.
	 *
	 * @param ctx1 The first launch selection context or <code>null</code>.
	 * @param ctx2 The second launch selection context or <code>null</code>.
	 *
	 * @return <code>True</code> if the two selection contexts are equal for this launch configuration type, <code>false</code> otherwise.
	 */
	public boolean equals(ISelectionContext ctx1, ISelectionContext ctx2);

	/**
	 * Return <code>true</code> if a default connection should be used when the connection selection is empty.
	 */
	public boolean useDefaultConnection();

	/**
	 * Get a short description for the given launch configuration.
	 * @param config The launch configuration.
	 * @return The description.
	 */
	public String getDescription(ILaunchConfiguration config);
}
