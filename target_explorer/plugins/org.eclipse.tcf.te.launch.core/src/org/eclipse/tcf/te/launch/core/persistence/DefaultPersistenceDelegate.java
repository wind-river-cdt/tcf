/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.persistence;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.tcf.te.runtime.events.EventManager;

/**
 * Launch framework default persistence delegate.
 */
public class DefaultPersistenceDelegate {

	// **** Interface methods to apply attributes only if changed.
	// ****
	// **** By default, calling a setAttribute method of the ILaunchConfigurationWorkingCopy
	// **** interface, marks the ILaunchConfiguration as dirty independent of the fact that the
	// **** real attribute value may not have changed at all. This basically leads to the UI
	// **** question if or if not to apply the changes made to a launch configuration by just
	// **** switching through the launch configuration tabs. However, it is desired functionality
	// **** to modify the launch configuration only if some attributes have really changed.
	// **** These methods here read and compare the attribute before really writing something
	// **** to the given ILaunchConfigurationWorkingCopy. All methods here are based on the
	// **** corresponding method declaration in ILaunchConfigurationWorkingCopy.

	/**
	 * Checks if the given attribute value under the given attribute id if the value
	 * has changed compared to the already stored value under the given attribute id or
	 * if the attribute id has not been stored yet. If the attribute value is <code>null</code>,
	 * the attribute id will be checked if it may need to get removed from the given launch
	 * configuration working copy.
	 *
	 * @param wc The launch configuration working copy instance to check the attributes against. Must not be <code>null</code>.
	 * @param attributeId The attribute id. Must not be <code>null</code>.
	 * @param attributeValue The attribute value.
	 *
	 * @return <code>true</code> if the attribute value has changed compared to it's old value, <code>false</code> otherwise.
	 */
	public final static boolean isAttributeChanged(ILaunchConfigurationWorkingCopy wc, String attributeId, Object attributeValue) {
		boolean changed = false;

		Assert.isNotNull(wc);
		Assert.isNotNull(attributeId);

		try {

			// Read the old attribute value from the launch configuration.
			Map<?,?> attributes = wc.getAttributes();
			// Case: If new attribute value == null and attribute id exists
			//       --> Remove the attribute from the launch configuration.
			if (attributeValue == null && attributes.containsKey(attributeId)) {
				changed = true;
			} else {
				// Case: If new attribute value != null and attribute id does not exists
				//       --> Store the attribute to the launch configuration.
				if (attributeValue != null && !attributes.containsKey(attributeId)) {
					changed = true;
				} else {
					// Case: If new attribute value != null and attribute id exists
					//       --> Compare new value with old value and if not equal,
					//           store the attribute to the launch configuration.
					if (attributeValue != null && attributes.containsKey(attributeId)) {
						Object oldAttributeValue = attributes.get(attributeId);
						if (! attributeValue.equals(oldAttributeValue)) {
							changed = true;
						}
					}
				}
			}
		} catch (CoreException e) { /* ignored */ }

		return changed;
	}

	/**
	 * Stores the given boolean attribute value under the given attribute id if the value
	 * has changed compared to the already stored value under the given attribute id or
	 * if the attribute id has not been stored yet.
	 *
	 * @param wc The launch configuration working copy instance to apply the attribute to. Must not be <code>null</code>.
	 * @param attributeId The attribute id to store the attribute value under. Must not be <code>null</code>.
	 * @param attributeValue The attribute value to store under the given attribute id.
	 */
	public final static void setAttribute(ILaunchConfigurationWorkingCopy wc, String attributeId, boolean attributeValue) {
		if (wc == null || attributeId == null) return;
		if (isAttributeChanged(wc, attributeId, Boolean.valueOf(attributeValue))) {
			// Determine the old attribute value
			Object oldValue = null;
			if (hasAttribute(wc, attributeId)) try { oldValue = wc.getAttributes().get(attributeId); } catch (CoreException e) { /* ignored on purpose */ }

			// Set the new value to the launch configuration
			wc.setAttribute(attributeId, attributeValue);

			// And fire an notification event
			EventManager.getInstance().fireEvent(new LaunchConfigurationChangedEvent(wc, attributeId, oldValue, Boolean.valueOf(attributeValue)));
		}
	}

	/**
	 * Stores the given integer attribute value under the given attribute id if the value
	 * has changed compared to the already stored value under the given attribute id or
	 * if the attribute id has not been stored yet.
	 *
	 * @param wc The launch configuration working copy instance to apply the attribute to. Must not be <code>null</code>.
	 * @param attributeId The attribute id to store the attribute value under. Must not be <code>null</code>.
	 * @param attributeValue The attribute value to store under the given attribute id.
	 */
	public final static void setAttribute(ILaunchConfigurationWorkingCopy wc, String attributeId, int attributeValue) {
		if (wc == null || attributeId == null) return;
		if (isAttributeChanged(wc, attributeId, new Integer(attributeValue))) {
			// Determine the old attribute value
			Object oldValue = null;
			if (hasAttribute(wc, attributeId)) try { oldValue = wc.getAttributes().get(attributeId); } catch (CoreException e) { /* ignored on purpose */ }

			// Set the new value to the launch configuration
			wc.setAttribute(attributeId, attributeValue);

			// And fire an notification event
			EventManager.getInstance().fireEvent(new LaunchConfigurationChangedEvent(wc, attributeId, oldValue, Integer.valueOf(attributeValue)));
		}
	}

	/**
	 * Stores the given string attribute value under the given attribute id if the value
	 * has changed compared to the already stored value under the given attribute id or
	 * if the attribute id has not been stored yet. If the attribute value is <code>null</code>,
	 * the attribute id will be removed from the given launch configuration working copy.
	 *
	 * @param wc The launch configuration working copy instance to apply the attribute to. Must not be <code>null</code>.
	 * @param attributeId The attribute id to store the attribute value under. Must not be <code>null</code>.
	 * @param attributeValue The attribute value to store under the given attribute id.
	 */
	public final static void setAttribute(ILaunchConfigurationWorkingCopy wc, String attributeId, String attributeValue) {
		if (wc == null || attributeId == null) return;
		if (isAttributeChanged(wc, attributeId, attributeValue)) {
			// Determine the old attribute value
			Object oldValue = null;
			if (hasAttribute(wc, attributeId)) try { oldValue = wc.getAttributes().get(attributeId); } catch (CoreException e) { /* ignored on purpose */ }

			// Set the new value to the launch configuration
			wc.setAttribute(attributeId, attributeValue);

			// And fire an notification event
			EventManager.getInstance().fireEvent(new LaunchConfigurationChangedEvent(wc, attributeId, oldValue, attributeValue));
		}
	}

	/**
	 * Stores the given list attribute value under the given attribute id if the value
	 * has changed compared to the already stored value under the given attribute id or
	 * if the attribute id has not been stored yet. If the attribute value is <code>null</code>,
	 * the attribute id will be removed from the given launch configuration working copy.
	 *
	 * @param wc The launch configuration working copy instance to apply the attribute to. Must not be <code>null</code>.
	 * @param attributeId The attribute id to store the attribute value under. Must not be <code>null</code>.
	 * @param attributeValue The attribute value to store under the given attribute id.
	 */
	public final static void setAttribute(ILaunchConfigurationWorkingCopy wc, String attributeId, List<?> attributeValue) {
		if (wc == null || attributeId == null) return;
		if (isAttributeChanged(wc, attributeId, attributeValue)) {
			// Determine the old attribute value
			Object oldValue = null;
			if (hasAttribute(wc, attributeId)) try { oldValue = wc.getAttributes().get(attributeId); } catch (CoreException e) { /* ignored on purpose */ }

			// Set the new value to the launch configuration
			wc.setAttribute(attributeId, attributeValue);

			// And fire an notification event
			EventManager.getInstance().fireEvent(new LaunchConfigurationChangedEvent(wc, attributeId, oldValue, attributeValue));
		}
	}

	/**
	 * Stores the given map attribute value under the given attribute id if the value
	 * has changed compared to the already stored value under the given attribute id or
	 * if the attribute id has not been stored yet. If the attribute value is <code>null</code>,
	 * the attribute id will be removed from the given launch configuration working copy.
	 *
	 * @param wc The launch configuration working copy instance to apply the attribute to. Must not be <code>null</code>.
	 * @param attributeId The attribute id to store the attribute value under. Must not be <code>null</code>.
	 * @param attributeValue The attribute value to store under the given attribute id.
	 */
	public final static void setAttribute(ILaunchConfigurationWorkingCopy wc, String attributeId, Map<?, ?> attributeValue) {
		if (wc == null || attributeId == null) return;
		if (isAttributeChanged(wc, attributeId, attributeValue)) {
			// Determine the old attribute value
			Object oldValue = null;
			if (hasAttribute(wc, attributeId)) try { oldValue = wc.getAttributes().get(attributeId); } catch (CoreException e) { /* ignored on purpose */ }

			// Set the new value to the launch configuration
			wc.setAttribute(attributeId, attributeValue);

			// And fire an notification event
			EventManager.getInstance().fireEvent(new LaunchConfigurationChangedEvent(wc, attributeId, oldValue, attributeValue));
		}
	}

	/**
	 * Returns the boolean attribute stored under the given attribute name or
	 * the default value if the attribute does not exist or the read failed.
	 *
	 * @param lc The launch configuration to read the attribute from. Must not be <code>null</code>.
	 * @param attributeName The attribute name. Must not be <code>null</code>.
	 * @param defaultValue The default value.
	 *
	 * @return The boolean attribute or the default value.
	 */
	public final static boolean getAttribute(ILaunchConfiguration lc, String attributeName, boolean defaultValue) {
		Assert.isNotNull(lc);
		Assert.isNotNull(attributeName);

		boolean value = defaultValue;
		try { value = lc.getAttribute(attributeName, defaultValue); } catch (CoreException e) { /* ignored on purpose */ }
		return value;
	}

	/**
	 * Returns the int attribute stored under the given attribute name or
	 * the default value if the attribute does not exist or the read failed.
	 *
	 * @param lc The launch configuration to read the attribute from. Must not be <code>null</code>.
	 * @param attributeName The attribute name. Must not be <code>null</code>.
	 * @param defaultValue The default value.
	 *
	 * @return The int attribute or the default value.
	 */
	public final static int getAttribute(ILaunchConfiguration lc, String attributeName, int defaultValue) {
		Assert.isNotNull(lc);
		Assert.isNotNull(attributeName);

		int value = defaultValue;
		try { value = lc.getAttribute(attributeName, defaultValue); } catch (CoreException e) { /* ignored on purpose */ }
		return value;
	}

	/**
	 * Returns the list attribute stored under the given attribute name or
	 * the default value if the attribute does not exist or the read failed.
	 *
	 * @param lc The launch configuration to read the attribute from. Must not be <code>null</code>.
	 * @param attributeName The attribute name. Must not be <code>null</code>.
	 * @param defaultValue The default value.
	 *
	 * @return The list attribute or the default value.
	 */
	public final static List<?> getAttribute(ILaunchConfiguration lc, String attributeName, List<?> defaultValue) {
		Assert.isNotNull(lc);
		Assert.isNotNull(attributeName);

		List<?> value = defaultValue;
		try { value = lc.getAttribute(attributeName, defaultValue); } catch (CoreException e) { /* ignored on purpose */ }
		return value;
	}

	/**
	 * Returns the set attribute stored under the given attribute name or
	 * the default value if the attribute does not exist or the read failed.
	 *
	 * @param lc The launch configuration to read the attribute from. Must not be <code>null</code>.
	 * @param attributeName The attribute name. Must not be <code>null</code>.
	 * @param defaultValue The default value.
	 *
	 * @return The set attribute or the default value.
	 */
	public final static Set<?> getAttribute(ILaunchConfiguration lc, String attributeName, Set<?> defaultValue) {
		Assert.isNotNull(lc);
		Assert.isNotNull(attributeName);

		Set<?> value = defaultValue;
		try { value = lc.getAttribute(attributeName, defaultValue); } catch (CoreException e) { /* ignored on purpose */ }
		return value;
	}


	/**
	 * Returns the map attribute stored under the given attribute name or
	 * the default value if the attribute does not exist or the read failed.
	 *
	 * @param lc The launch configuration to read the attribute from. Must not be <code>null</code>.
	 * @param attributeName The attribute name. Must not be <code>null</code>.
	 * @param defaultValue The default value.
	 *
	 * @return The map attribute or the default value.
	 */
	public final static Map<?,?> getAttribute(ILaunchConfiguration lc, String attributeName, Map<?,?> defaultValue) {
		Assert.isNotNull(lc);
		Assert.isNotNull(attributeName);

		Map<?,?> value = defaultValue;
		try { value = lc.getAttribute(attributeName, defaultValue); } catch (CoreException e) { /* ignored on purpose */ }
		return value;
	}

	/**
	 * Returns the string attribute stored under the given attribute name or
	 * the default value if the attribute does not exist or the read failed.
	 *
	 * @param lc The launch configuration to read the attribute from. Must not be <code>null</code>.
	 * @param attributeName The attribute name. Must not be <code>null</code>.
	 * @param defaultValue The default value.
	 *
	 * @return The string attribute or the default value.
	 */
	public final static String getAttribute(ILaunchConfiguration lc, String attributeName, String defaultValue) {
		Assert.isNotNull(lc);
		Assert.isNotNull(attributeName);

		String value = defaultValue;
		try {
			value = lc.getAttribute(attributeName, defaultValue);
		}
		catch (CoreException e) {
			/* ignored on purpose */
		}
		return value;
	}

	/**
	 * Returns whether the given launch configuration contains an attribute of the given name.
	 *
	 * @param lc The launch configuration. Must not be <code>null</code>.
	 * @param attributeName The attribute name. Must not be <code>null</code>.
	 *
	 * @return <code>True</code> if the launch configuration contains the attribute, <code>false</code> otherwise.
	 */
	public final static boolean hasAttribute(ILaunchConfiguration lc, String attributeName) {
		Assert.isNotNull(lc);
		Assert.isNotNull(attributeName);

		boolean hasAttribute = false;
		try { hasAttribute = lc.hasAttribute(attributeName); } catch (CoreException e) { /* ignored on purpose */ }
		return hasAttribute;
	}
}
