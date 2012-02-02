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

import org.eclipse.core.runtime.IAdaptable;

/**
 * The launch specification interface describes a common API to handle launch configuration
 * attributes in a generic way. This allow to write compact generic launch configuration handling
 * code without the need to know what exact type of launch configuration it is.
 */
public interface ILaunchSpecification extends IAdaptable {

	/**
	 * Returns the unique launch configuration type id for the launch configuration described by
	 * this launch specification.
	 *
	 * @return The unique launch configuration type id.
	 */
	public String getLaunchConfigurationTypeId();

	/**
	 * Returns the launch mode. @see <code>org.eclipse.debug.core.ILaunchManager</code> for valid id's.
	 *
	 * @return The launch mode.
	 */
	public String getLaunchMode();

	/**
	 * Returns the name proposal for a new generated launch configuration. The name doesn't need to
	 * be unique! Can return <code>null</code>.
	 */
	public String getLaunchConfigName();

	/**
	 * Set the name for a new generated launch configuration.
	 *
	 * @param launchConfigName The name for a new generated launch configuration or <code>null</code>.
	 */
	public void setLaunchConfigName(String launchConfigName);

	/**
	 * Returns the label proposal for the launch action that uses this launch specification. The
	 * label should _NEVER_ start with the launch mode (Run/Debug <label>). Can return <code>null</code>.
	 */
	public String getLaunchActionLabel();

	/**
	 * Set the label for the launch action that uses this launch specification.
	 *
	 * @param launchActionLabel The label for the launch action. Can be <code>null</code>.
	 */
	public void setLaunchActionLabel(String launchActionLabel);

	/**
	 * Add the specified launch configuration attribute value to this specification using the
	 * specified launch configuration attribute key. If the launch specification has been locked,
	 * the method will do nothing. If the specified value is <code>
	 * null</code>, this method has the same effect as <code>removeAttribute(key)</code>.
	 *
	 * @param key The launch configuration attribute key. Must not be <code>null</code>!
	 * @param value The launch configuration attribute value or <code>null</code>.
	 * @param createOnly <code>true</code> if this attribute should only be used for create.
	 */
	public void addAttribute(String key, Object value, boolean createOnly);

	/**
	 * Add the specified launch configuration attribute value to this specification using the
	 * specified launch configuration attribute key. If the launch specification has been locked,
	 * the method will do nothing. If the specified value is <code>
	 * null</code>, this method has the same effect as <code>removeAttribute(key)</code>.
	 *
	 * @param key The launch configuration attribute key. Must not be <code>null</code>!
	 * @param value The launch configuration attribute value or <code>null</code>.
	 */
	public void addAttribute(String key, Object value);

	/**
	 * Checks if the launch configuration attribute with the specified key is known to this launch
	 * specification.
	 *
	 * @param key The launch configuration attribute key. Must not be <code>null</code>!
	 * @return <code>true</code> if the attribute is known, <code>false</code> otherwise.
	 */
	public boolean hasAttribute(String key);

	/**
	 * Removes the launch configuration attribute with the specified key. If the launch
	 * specification has been locked, the method will do nothing
	 *
	 * @param key The launch configuration attribute key. Must not be <code>null</code>!
	 * @return The attribute value stored for the specified key or <code>null</code>.
	 */
	public Object removeAttribute(String key);

	/**
	 * Returns the attribute value for the given attribute key. If the key is not known or the value
	 * is not set, the specified default value will be returned instead.
	 *
	 * @param key The launch configuration attribute key. Must not be <code>null</code>!
	 * @param defaultValue The default value.
	 * @return The launch configuration attribute value or the default value.
	 */
	public Object getAttribute(String key, Object defaultValue);

	/**
	 * Returns the launch attribute the given attribute key. If the key is not known or the value is
	 * not set, <code>null</code> will be returned.
	 *
	 * @param key The launch configuration attribute key. Must not be <code>null</code>!
	 * @return The launch configuration attribute or <code>null</code>.
	 */
	public ILaunchAttribute getAttribute(String key);

	/**
	 * Returns <code>true</code> if the attribute is set and the <code>createOnly</code> flag of the
	 * attribute key is set.
	 *
	 * @param key The launch configuration attribute key. Must not be <code>null</code>!
	 * @return The <code>createOnly</code> flag of the attribute key.
	 */
	public boolean isCreateOnlyAttribute(String key);

	/**
	 * Locks or unlocks the launch specification for modification. If the read only state is set to
	 * <code>true</code>, every modifying method call will return immediately and change nothing!
	 *
	 * @param readOnly <code>true</code> to lock the launch specification, <code>false</code> to unlock.
	 */
	public void setReadOnly(boolean readOnly);

	/**
	 * Returns the current lock state of the launch specification.
	 *
	 * @return <code>true</code> if launch specification is locked, <code>false</code> otherwise.
	 */
	public boolean isReadOnly();

	/**
	 * Remove all known launch attributes from this launch specification. If the launch
	 * specification has been locked, the method will do nothing.
	 */
	public void clear();

	/**
	 * Returns the number of launch attributes known to this launch specification.
	 */
	public int size();

	/**
	 * Returns <code>true</code> if this launch specification does not contain any attribute.
	 */
	public boolean isEmpty();

	/**
	 * Returns an array containing all launch configuration attributes known to this launch
	 * specification.
	 *
	 * @return An array with elements of type <code>ILaunchAttribute</code>. The returned array may
	 *         empty but never <code>null</code>.
	 */
	public ILaunchAttribute[] getAllAttributes();

	/**
	 * Returns <code>true</code> if this launch spec is valid.
	 */
	public boolean isValid();

	/**
	 * Sets the result of a validation. Validation is normally made through launch manager
	 * delegates.
	 *
	 * @param valid <code>true</code> if this launch spec is valid.
	 */
	public void setIsValid(boolean valid);

	/**
	 * Returns the error message when not valid, otherwise <code>null</code>.
	 */
	public String getErrorMessage();

	/**
	 * Sets the error message after validation fails.
	 *
	 * @param errorMessage The error message.
	 */
	public void setErrorMessage(String errorMessage);
}
