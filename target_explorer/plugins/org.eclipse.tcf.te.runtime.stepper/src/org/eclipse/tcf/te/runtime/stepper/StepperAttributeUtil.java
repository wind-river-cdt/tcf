/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.stepper;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId;

/**
 * A stepper attributes utility provides a set of static methods
 * to access the attributes of a step.
 */
public class StepperAttributeUtil {
	/**
	 * Get the full qualified key to get or set data in the data.
	 *
	 * @param key The key for the value.
	 * @param fullQualifiedId The full qualified id for this step.
	 * @param data The data.
	 * @return The full qualified key.
	 */
	protected final static String getFullQualifiedKey(String key, IFullQualifiedId fullQualifiedId, IPropertiesContainer data) {
		Assert.isNotNull(key);
		Assert.isNotNull(data);

		return (fullQualifiedId != null ? fullQualifiedId.toString() : "") + key; //$NON-NLS-1$
	}

	/**
	 * Get a property from the data. If the value is not stored within the full qualified id, the
	 * value stored within the parent id will be returned.
	 *
	 * @param key The key for the value.
	 * @param fullQualifiedId The full qualified id for this step.
	 * @param data The data.
	 * @return The property value or <code>null</code> if either the data has no property container
	 *         or the property is not set.
	 */
	public final static Object getProperty(String key, IFullQualifiedId fullQualifiedId, IPropertiesContainer data) {
		Assert.isNotNull(key);
		Assert.isNotNull(data);

		if (fullQualifiedId == null || data.getProperty(getFullQualifiedKey(key, fullQualifiedId, data)) != null) {
			return data.getProperty(getFullQualifiedKey(key, fullQualifiedId, data));
		}
		return getProperty(key, fullQualifiedId.getParentId(), data);
	}

	/**
	 * Get a string property from the data. If the value is not stored within the full qualified id,
	 * the value stored within the parent id will be returned.
	 *
	 * @param key The key for the value.
	 * @param fullQualifiedId The full qualified id for this step.
	 * @param data The data.
	 * @return The string property value or <code>null</code> if either the data has no property
	 *         container or the property is not set.
	 */
	public final static String getStringProperty(String key, IFullQualifiedId fullQualifiedId, IPropertiesContainer data) {
		Assert.isNotNull(key);
		Assert.isNotNull(data);

		if (fullQualifiedId == null || data.getProperty(getFullQualifiedKey(key, fullQualifiedId, data)) != null) {
			return data.getStringProperty(getFullQualifiedKey(key, fullQualifiedId, data));
		}
		return getStringProperty(key, fullQualifiedId.getParentId(), data);
	}

	/**
	 * Get a boolean property from the data. If the value is not stored within the full qualified
	 * id, the value stored within the parent id will be returned.
	 *
	 * @param key The key for the value.
	 * @param fullQualifiedId The full qualified id for this step.
	 * @param data The data.
	 * @return The boolean property value or <code>false</code> if either the data has no property
	 *         container or the property is not set.
	 */
	public final static boolean getBooleanProperty(String key, IFullQualifiedId fullQualifiedId, IPropertiesContainer data) {
		Assert.isNotNull(key);
		Assert.isNotNull(data);

		if (fullQualifiedId == null || data.getProperty(getFullQualifiedKey(key, fullQualifiedId, data)) != null) {
			return data.getBooleanProperty(getFullQualifiedKey(key, fullQualifiedId, data));
		}
		return getBooleanProperty(key, fullQualifiedId.getParentId(), data);
	}

	/**
	 * Get a int property from the data.
	 *
	 * @param key The key for the value.
	 * @param fullQualifiedId The full qualified id for this step.
	 * @param data The data.
	 * @return The int property value or <code>-1</code> if either the data has no property
	 *         container or the property is not set.
	 */
	public final static int getIntProperty(String key, IFullQualifiedId fullQualifiedId, IPropertiesContainer data) {
		Assert.isNotNull(key);
		Assert.isNotNull(data);

		if (fullQualifiedId == null || data.getProperty(getFullQualifiedKey(key, fullQualifiedId, data)) != null) {
			return data.getIntProperty(getFullQualifiedKey(key, fullQualifiedId, data));
		}
		return getIntProperty(key, fullQualifiedId.getParentId(), data);
	}

	/**
	 * Check if a property is set.
	 *
	 * @param key The key for the value.
	 * @param fullQualifiedId The full qualified id for this step.
	 * @param data The data.
	 * @return <code>true</code> if a property value is set.
	 */
	public final static boolean isPropertySet(String key, IFullQualifiedId fullQualifiedId, IPropertiesContainer data) {
		Assert.isNotNull(key);
		Assert.isNotNull(data);

		return data.getProperty(getFullQualifiedKey(key, fullQualifiedId, data)) != null;
	}

	/**
	 * Set a property value to the data.
	 *
	 * @param key The key for the value.
	 * @param fullQualifiedId The full qualified id for this step.
	 * @param data The data.
	 * @param value The new value.
	 * @return <code>true</code> if the value was set.
	 */
	public final static boolean setProperty(String key, IFullQualifiedId fullQualifiedId, IPropertiesContainer data, Object value) {
		return setProperty(key, fullQualifiedId, data, value, false);
	}

	/**
	 * Set a property value to the data and optional share it through the parent full qualified id.
	 *
	 * @param key The key for the value.
	 * @param fullQualifiedId The full qualified id for this step.
	 * @param data The data.
	 * @param value The new value.
	 * @param share When <code>true</code>, the value is also stored within the parent full
	 *            qualified id to share the value with other steps within the same parent (group).
	 * @return <code>true</code> if the value was set.
	 */
	public final static boolean setProperty(String key, IFullQualifiedId fullQualifiedId, IPropertiesContainer data, Object value, boolean share) {
		Assert.isNotNull(key);
		Assert.isNotNull(data);

		if (share && fullQualifiedId != null) {
			data.setProperty(getFullQualifiedKey(key, fullQualifiedId.getParentId(), data), value);
		}
		return data.setProperty(getFullQualifiedKey(key, fullQualifiedId, data), value);
	}

	/**
	 * Set a boolean property value to the data.
	 *
	 * @param key The key for the value.
	 * @param fullQualifiedId The full qualified id for this step.
	 * @param data The data.
	 * @param value The new boolean value.
	 * @return <code>true</code> if the value was set.
	 */
	public final static boolean setProperty(String key, IFullQualifiedId fullQualifiedId, IPropertiesContainer data, boolean value) {
		return setProperty(key, fullQualifiedId, data, value, false);
	}

	/**
	 * Set a boolean property value to the data and optional share it through the parent full
	 * qualified id.
	 *
	 * @param key The key for the value.
	 * @param fullQualifiedId The full qualified id for this step.
	 * @param data The data.
	 * @param value The new boolean value.
	 * @param share When <code>true</code>, the value is also stored within the parent full
	 *            qualified id to share the value with other steps within the same parent (group).
	 * @return <code>true</code> if the value was set.
	 */
	public final static boolean setProperty(String key, IFullQualifiedId fullQualifiedId, IPropertiesContainer data, boolean value, boolean share) {
		Assert.isNotNull(key);
		Assert.isNotNull(data);

		if (share && fullQualifiedId != null) {
			data.setProperty(getFullQualifiedKey(key, fullQualifiedId.getParentId(), data), value);
		}
		return data.setProperty(getFullQualifiedKey(key, fullQualifiedId, data), value);
	}

	/**
	 * Set a int property value to the data.
	 *
	 * @param key The key for the value.
	 * @param fullQualifiedId The full qualified id for this step.
	 * @param data The data.
	 * @param value The new int value.
	 * @return <code>true</code> if the value was set.
	 */
	public final static boolean setProperty(String key, IFullQualifiedId fullQualifiedId, IPropertiesContainer data, int value) {
		return setProperty(key, fullQualifiedId, data, value, false);
	}

	/**
	 * Set an int property value to the data and optional share it through the parent full qualified
	 * id.
	 *
	 * @param key The key for the value.
	 * @param fullQualifiedId The full qualified id for this step.
	 * @param data The data.
	 * @param value The new int value.
	 * @param share When <code>true</code>, the value is also stored within the parent full
	 *            qualified id to share the value with other steps within the same parent (group).
	 * @return <code>true</code> if the value was set.
	 */
	public final static boolean setProperty(String key, IFullQualifiedId fullQualifiedId, IPropertiesContainer data, int value, boolean share) {
		Assert.isNotNull(key);
		Assert.isNotNull(data);

		if (share && fullQualifiedId != null) {
			data.setProperty(getFullQualifiedKey(key, fullQualifiedId.getParentId(), data), value);
		}
		return data.setProperty(getFullQualifiedKey(key, fullQualifiedId, data), value);
	}
}
