/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.lm;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.launch.core.activator.CoreBundleActivator;
import org.eclipse.tcf.te.launch.core.bindings.LaunchConfigTypeBindingsManager;
import org.eclipse.tcf.te.launch.core.exceptions.LaunchServiceException;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchAttribute;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchManagerDelegate;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification;
import org.eclipse.tcf.te.launch.core.nls.Messages;

/**
 * The Launch Manager is the management interface for the launch configuration storage layer.
 * Through the launch manager, launch configuration of the several types can be accessed, searched
 * and initialized. For every possible launch configuration type, a corresponding launch manager
 * delegate must be registered which is responsible for handling the corresponding attributes of the
 * specific launch configuration type. In the case the no launch manager delegate is registered for
 * a launch configuration type, the launch configuration will not be initialized and therefore will
 * not have any default attribute. If more than one launch manager delegates are contributed for the
 * same launch configuration type, the first registered launch manager delegates will be used and the
 * registration of any other delegate for this type will fail!
 */
public class LaunchManager extends PlatformObject {

	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstanceHolder {
		public static LaunchManager instance = new LaunchManager();
	}

	/**
	 * Returns the singleton instance for the manager.
	 */
	public static LaunchManager getInstance() {
		return LazyInstanceHolder.instance;
	}

	/**
	 * Constructor.
	 */
	LaunchManager() {
		super();
	}

	/**
	 * Returns the corresponding launch manager delegate instance responsible for the specified
	 * launch configuration type. The method may return a default launch manager delegate if no
	 * specific launch manager delegate is registered for the specified launch configuration type.
	 *
	 * @param launchConfigType The launch configuration type to get the launch manager delegate for.
	 *            Must not be <code>null</code>!
	 * @param launchMode The launch mode to get the launch manager delegate for. Must not be
	 *            <code>null</code>.
	 * @return The corresponding launch manager delegate instance.
	 */
	public ILaunchManagerDelegate getLaunchManagerDelegate(ILaunchConfigurationType launchConfigType, String launchMode) {
		Assert.isNotNull(launchConfigType);
		Assert.isNotNull(launchMode);
		return LaunchConfigTypeBindingsManager.getInstance().getLaunchManagerDelegate(launchConfigType.getIdentifier(), launchMode);
	}

	/**
	 * Returns the corresponding launch configuration type for the given launch configuration type
	 * id and the given launch mode.
	 *
	 * @param launchConfigTypeId The unique id of the launch configuration type requested.
	 * @param launchMode The launch mode the launch configuration type must support. See
	 *            <code>org.eclipse.debug.core.ILaunchManager</code> for details.
	 * @return The corresponding launch configuration type instance or <code>null</code> if not
	 *         found or the specified mode is not supported.
	 */
	public ILaunchConfigurationType getLaunchConfigType(String launchConfigTypeId, String launchMode) {
		ILaunchConfigurationType launchConfigType = DebugPlugin.getDefault().getLaunchManager()
						.getLaunchConfigurationType(launchConfigTypeId);
		if (launchConfigType != null && !launchConfigType.supportsMode(launchMode)) {
			launchConfigType = null;
		}
		return launchConfigType;
	}

	/**
	 * Returns an fully initialized launch configuration. The launch configuration type and the
	 * launch mode required to create or look up the launch configuration, are specified through the
	 * given launch specification. Any launch configuration attribute of the certain type which is
	 * not explicitly overwritten by an attribute specified through the given launch specification
	 * will be initialized with default values.
	 * <p>
	 * If <code>forceNewConfig</code> is <code>false</code>, the method tries to find a matching
	 * existing launch configuration. If no existing launch configuration can be found, a new
	 * launch configuration will created instead.
	 *
	 * @param launchSpec A set of non default launch configuration attributes. Must not be
	 *            <code>null</code>, but the list of attributes may empty to get an launch
	 *            configuration with all attributes initialized to default values.
	 * @param createNew If <code>true</code>, a new launch configuration will be created if no
	 *            available is found.
	 * @return The launch configuration instance matching the given parameters.
	 * @throws <code>LaunchServiceException</code> in case the launch configuration instance
	 *         cannot be created, found and/or modified. The exception message describes the failure
	 *         details.
	 */
	public ILaunchConfiguration getLaunchConfiguration(ILaunchSpecification launchSpec, boolean createNew) throws LaunchServiceException {
		Assert.isNotNull(launchSpec);

		ILaunchConfiguration launchConfig = null;
		try {
			// get all launch configurations for launch configuration type id and launch mode
			String launchConfigTypeId = launchSpec.getLaunchConfigurationTypeId();
			String launchMode = launchSpec.getLaunchMode();
			ILaunchConfigurationType launchConfigType = getLaunchConfigType(launchConfigTypeId, launchMode);
			ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager()
							.getLaunchConfigurations(launchConfigType);

			// get list of fully and closest matching launch configurations
			ILaunchConfiguration[] matchingConfigs = getLaunchManagerDelegate(launchConfigType, launchMode)
							.getMatchingLaunchConfigurations(launchSpec, configs);

			// return best matching launch configuration
			if (matchingConfigs.length > 0) {
				launchConfig = matchingConfigs[0];
			}
		}
		catch (LaunchServiceException e) {
			if (e.getType() == LaunchServiceException.TYPE_MISSING_LAUNCH_SPEC_ATTR) {
				throw e;
			}
		}
		catch (CoreException e) {
			throw new LaunchServiceException(e.getMessage());
		}
		// return new launch configuration if no matching or best matching configuration is found
		if (createNew && launchConfig == null) {
			launchConfig = createOrUpdateLaunchConfiguration(null, launchSpec);
		}
		return launchConfig;
	}

	/**
	 * Create a new or updates an existing launch configuration of the requested type and initialize
	 * the configuration with the given launch specification. Attributes not listed by the given
	 * launch specification will be initialized with default values.
	 *
	 * @param launchConfig A launch configuration to update or <code>null</code> if a new launch
	 * 					   configuration should be created.
	 * @param launchSpec A set of non default launch configuration attributes.
	 * @return The newly create launch configuration instance.
	 *
	 * @throws <code>LaunchServiceException</code> in case the launch configuration instance
	 *         cannot be created or mandatory attributes are missing in the launch specification.
	 *         The exception message describes the failure details.
	 */
	public ILaunchConfiguration createOrUpdateLaunchConfiguration(ILaunchConfiguration launchConfig, ILaunchSpecification launchSpec) throws LaunchServiceException {
		return this.createOrUpdateLaunchConfiguration(launchConfig, launchSpec, true);
	}

	/**
	 * Create a new or updates an existing launch configuration of the requested type and initialize
	 * the configuration with the given launch specification. Attributes not listed by the given
	 * launch specification will be initialized with default values.
	 *
	 * @param launchConfig A launch configuration to update or <code>null</code> if a new launch
	 * 			  configuration should be created.
	 * @param launchSpec A set of non default launch configuration attributes.
	 * @param validateSpec Validate the launch specification in the <code>launchSpec</code>
	 *            parameter. If <code>false</code>, it will attempt to create the launch
	 *            configuration even if some of the mandatory parameters are missing.
	 * @return The newly create launch configuration instance.
	 *
	 * @throws <code>LaunchServiceException</code> in case the launch configuration instance
	 *         cannot be created or mandatory attributes are missing in the launch specification.
	 *         The exception message describes the failure details.
	 *
	 * @since 3.2
	 */
	public ILaunchConfiguration createOrUpdateLaunchConfiguration(ILaunchConfiguration launchConfig, ILaunchSpecification launchSpec, boolean validateSpec) throws LaunchServiceException {
		Assert.isNotNull(launchSpec);

		String launchConfigTypeId = launchSpec.getLaunchConfigurationTypeId();
		String launchMode = launchSpec.getLaunchMode();

		ILaunchConfigurationType launchConfigType = getLaunchConfigType(launchConfigTypeId, launchMode);
		try {
			if (launchConfigType != null) {
				// get the launch manager delegate instance for the requested launch configuration
				// type
				ILaunchManagerDelegate delegate = getLaunchManagerDelegate(launchConfigType, launchMode);
				if (validateSpec) {
					delegate.validate(launchSpec);
				}
				ILaunchConfigurationWorkingCopy wc = null;
				if (launchConfig == null || !launchConfig.getType().getIdentifier()
								.equals(launchConfigTypeId)) {
					try {
						// create the launch configuration working copy instance
						wc = launchConfigType.newInstance(null, DebugPlugin
										.getDefault()
										.getLaunchManager()
										.generateLaunchConfigurationName(launchSpec
														.getLaunchConfigName()));
						// initialize the launch configuration working copy
						delegate.initLaunchConfigAttributes(wc, launchSpec);
						// and save the launch configuration
						return wc.doSave();
					}
					catch (CoreException e) {
						throw new LaunchServiceException(Messages.LaunchManager_error_failedToCreateConfig);
					}
				}
				try {
					// get a launch configration working copy
					if (launchConfig instanceof ILaunchConfigurationWorkingCopy) {
						wc = (ILaunchConfigurationWorkingCopy) launchConfig;
					}
					else {
						wc = launchConfig.getWorkingCopy();
					}
					// update the launch configuration working copy
					delegate.updateLaunchConfigAttributes(wc, launchSpec);
					// and save the launch configuration
					return (wc.isDirty()) ? wc.doSave() : launchConfig;
				}
				catch (CoreException e) {
					throw new LaunchServiceException(NLS.bind(Messages.LaunchManager_error_failedToUpdateConfig, launchConfig.getName()));
				}
			}
		}
		catch (CoreException e) {
			// do nothing, because exception is thrown afterwards if this point is reached.
		}
		throw new LaunchServiceException(NLS.bind(Messages.LaunchManager_error_noLaunchConfigType, launchMode));
	}

	/**
	 * Delete the specified launch configuration.
	 * <p>
	 * In case any error occurs during the delete, the exception is logged to the Eclipse error log.
	 *
	 * @param launchConfig The launch configuration to delete.
	 */
	public void deleteLaunchConfiguration(ILaunchConfiguration launchConfig) {
		if (launchConfig != null) {
			try {
				launchConfig.delete();
			}
			catch (CoreException e) {
				IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
								Messages.LaunchManager_error_deleteLaunchConfig, e);
				Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
			}
		}
	}

	/**
	 * Creates an exact copy of the given launch specification.
	 * <p>
	 * <b>Note:</b> The method returns always an launch specification which has not locked out
	 * modifications. The corresponding read-only flag from the original is not duplicated to the
	 * copy!
	 *
	 * @param launchSpec The launch specification to duplication.
	 * @return A new <code>ILaunchSpecification</code> instance containing the same data as the
	 *         original, or <code>null</code>.
	 */
	public ILaunchSpecification duplicate(ILaunchSpecification launchSpec) {
		if (launchSpec != null) {
			ILaunchSpecification newLaunchSpec = new LaunchSpecification(launchSpec.getLaunchConfigurationTypeId(), launchSpec.getLaunchMode());
			if (!launchSpec.isEmpty()) {
				ILaunchAttribute[] attributes = launchSpec.getAllAttributes();
				for (ILaunchAttribute attribute : attributes) {
					newLaunchSpec.addAttribute(attribute.getKey(), attribute.getValue());
				}
			}
			return newLaunchSpec;
		}
		return null;
	}

	/**
	 * Validates a launch configuration.
	 *
	 * @param launchConfig The launch configuration to validate.
	 * @param launchMode The launch mode. Can be <code>null</code>, in this case the launch configuration
	 *            is valid when valid for all supported modes.
	 * @return <code>true</code>, if the launch configuration is valid and can be executed (launched).
	 */
	public boolean validate(ILaunchConfiguration launchConfig, String launchMode) {
		try {
			if (launchMode == null) {
				boolean valid = false;
				for (String mode : LaunchConfigHelper.getLaunchConfigTypeModes(launchConfig.getType(), false)) {
					if (launchConfig.supportsMode(mode) && validate(launchConfig, mode)) {
						valid = true;
					}
				}
				return valid;
			}
			ILaunchManagerDelegate delegate = getLaunchManagerDelegate(launchConfig.getType(), launchMode);
			try {
				delegate.validate(launchMode, launchConfig);
			}
			catch (LaunchServiceException e) {
				return false;
			}

		}
		catch (CoreException e) {
		}
		return true;
	}

	/**
	 * Transform the specified launch configuration into a corresponding launch specification
	 * object. If <code>withDefaultAttributes</code> is not set, the corresponding launch manager
	 * delegate is called to determine if an attribute has a default value or not. If the attribute
	 * has an default value, this attribute is not copied to the launch specification. If
	 * <code>withDefaultAttributes</code> is set, all attributes are copied to the launch
	 * specification object.
	 *
	 * @param launchConfig The launch configuration. Must not be <code>null</code>.
	 * @param launchMode The launch mode the launch specification should be for. See
	 *            <code>ILaunchManager</code> for details.
	 * @param withDefaultAttributes Set to <code>true</code> to copy attributes with default value
	 *            as well, <code>false</code> otherwise.
	 *
	 * @return The corresponding launch specification object or <code>null</code>.
	 *
	 * @throws <code>LaunchServiceException</code> in case the launch configuration could not be
	 *         transformed to a launch specification. The exception message describes the failure
	 *         details.
	 */
	public ILaunchSpecification createSpecFromConfig(ILaunchConfiguration launchConfig, String launchMode, boolean withDefaultAttributes) throws LaunchServiceException {
		Assert.isNotNull(launchConfig);

		ILaunchSpecification spec = null;
		try {
			// extract the launch configuration type
			ILaunchConfigurationType type = launchConfig.getType();
			spec = new LaunchSpecification(type.getIdentifier(), launchMode);
			// get the launch manager delegate for the specific type
			ILaunchManagerDelegate delegate = getLaunchManagerDelegate(type, launchMode);
			// get all the launch configuration attributes
			Map<String, Object> attributes = launchConfig.getAttributes();
			// loop over all listed attributes and copy them to the specification
			Iterator<Entry<String, Object>> iterator = attributes.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, Object> entry = iterator.next();
				if (withDefaultAttributes) {
					// include the default attributes. So, just copy the stuff over.
					spec.addAttribute(entry.getKey(), entry.getValue());
				}
				else {
					// exclude the default attributes. We have to find out if the attribute is
					// set with default value.
					Object attributeValue = entry.getValue();
					if (!delegate.isDefaultAttribute(entry.getKey(), attributeValue, launchConfig, launchMode)) {
						spec.addAttribute(entry.getKey(), attributeValue);
					}
				}
			}
		}
		catch (CoreException e) {
			spec = null;
			throw new LaunchServiceException(e);
		}
		return spec;
	}

	/**
	 * Launch the specified launch configuration using the corresponding delegate for the specified
	 * launch mode. If <code>buildBeforeLaunch</code> is set to <code>true</code>, the workspace
	 * will be build before the launch.
	 *
	 * @param launchConfig The launch configuration to launch. Must not be <code>null</code>!
	 * @param launchMode The launch mode (@see <code>org.eclipse.debug.core.ILaunchManager</code>.
	 *            Must not be <code>null</code>!
	 * @param buildBeforeLaunch Specify <code>true</code> to build the workspace before launch,
	 *            <code>false</code> otherwise.
	 *
	 * @return The corresponding <code>ILaunch</code> object associated with this launch.
	 *
	 * @throws <code>LaunchServiceException</code> in case of any problem occurs during the launch sequence.
	 */
	public ILaunch launch(ILaunchConfiguration launchConfig, String launchMode, boolean buildBeforeLaunch, IProgressMonitor monitor) throws LaunchServiceException {
		Assert.isNotNull(launchConfig);
		Assert.isNotNull(launchMode);

		try {
			ILaunchManagerDelegate delegate = getLaunchManagerDelegate(launchConfig.getType(), launchMode);
			delegate.validate(launchMode, launchConfig);
			return launchConfig.launch(launchMode, monitor, buildBeforeLaunch);
		}
		catch (CoreException e) {
			// re-pack into a launch service exception
			throw new LaunchServiceException(e);
		}
	}

	/**
	 * Remove all illegal characters in the launch configuration name candidate.
	 *
	 * @param candidate The launch configuration name candidate.
	 * @return The unified launch configuration name.
	 */
	public static String getUnifiedLaunchConfigName(String candidate) {
		if (candidate != null) {
			candidate = candidate.replaceAll("[/\\\"&?:@*]", "_"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return candidate;
	}

}
