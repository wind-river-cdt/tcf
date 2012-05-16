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

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchAttribute;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification;
import org.eclipse.tcf.te.launch.core.nls.Messages;

/**
 * Default launch specification implementation.
 */
public class LaunchSpecification extends PlatformObject implements ILaunchSpecification {
	private final Map<String, ILaunchAttribute> attributes = new Hashtable<String, ILaunchAttribute>();
	private final String typeId;
	private final String mode;
	private String launchConfigName;
	private String launchActionLabel;
	private boolean readOnly;
	private boolean valid = true;
	private String errorMessage = null;

	/**
	 * Constructor.
	 * <p>
	 * Creates a new launch specification instance for the specified launch configuration type
	 * id and the specified launch mode. The launch specification is not locked against modifications
	 * by default!
	 *
	 * @param typeId The launch configuration type id of the described launch configuration.
	 * @param mode The launch mode. @see <code>org.eclipse.debug.core.ILaunchManager</code>!
	 */
	public LaunchSpecification(String typeId, String mode) {
		super();
		this.typeId = typeId;
		this.mode = mode;
		this.attributes.clear();
		setReadOnly(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification#getLaunchConfigurationTypeId()
	 */
	@Override
	public String getLaunchConfigurationTypeId() {
		return typeId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification#getLaunchMode()
	 */
	@Override
	public String getLaunchMode() {
		return mode;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification#isReadOnly()
	 */
	@Override
	public boolean isReadOnly() {
		return readOnly;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification#setReadOnly(boolean)
	 */
	@Override
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification#addAttribute(java.lang.String, java.lang.Object)
	 */
	@Override
	public void addAttribute(String key, Object value) {
		addAttribute(key, value, false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification#addAttribute(java.lang.String, java.lang.Object, boolean)
	 */
	@Override
	public void addAttribute(String key, Object value, boolean createOnly) {
		Assert.isNotNull(key);

		if (isReadOnly()) {
			return;
		}

		// Attention: If the value == null -> remove the key from the map!!!
		if (value != null) {
			attributes.put(key, new LaunchAttribute(key, value, createOnly));
		}
		else {
			attributes.remove(key);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification#hasAttribute(java.lang.String)
	 */
	@Override
	public boolean hasAttribute(String key) {
		Assert.isNotNull(key);
		return attributes.containsKey(key);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification#removeAttribute(java.lang.String)
	 */
	@Override
	public Object removeAttribute(String key) {
		Assert.isNotNull(key);
		if (isReadOnly()) {
			return null;
		}
		return attributes.remove(key);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification#isCreateOnlyAttribute(java.lang.String)
	 */
	@Override
	public boolean isCreateOnlyAttribute(String key) {
		Assert.isNotNull(key);
		ILaunchAttribute attribute = getAttribute(key);
		return attribute != null && attribute.isCreateOnlyAttribute();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification#getAttribute(java.lang.String, java.lang.Object)
	 */
	@Override
	public Object getAttribute(String key, Object defaultValue) {
		Assert.isNotNull(key);
		ILaunchAttribute attribute = getAttribute(key);
		return (attribute != null && attribute.getValue() != null) ? attribute.getValue() : defaultValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification#clear()
	 */
	@Override
	public void clear() {
		if (isReadOnly()) {
			return;
		}
		attributes.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification#size()
	 */
	@Override
	public int size() {
		return attributes.size();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return attributes.isEmpty();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification#getAttribute(java.lang.String)
	 */
	@Override
	public ILaunchAttribute getAttribute(String key) {
		Assert.isNotNull(key);
		return attributes.get(key);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification#getAllAttributes()
	 */
	@Override
	public ILaunchAttribute[] getAllAttributes() {
		return attributes.values().toArray(new ILaunchAttribute[attributes.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification#getLaunchActionLabel()
	 */
	@Override
	public String getLaunchActionLabel() {
		return launchActionLabel;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification#getLaunchConfigName()
	 */
	@Override
	public String getLaunchConfigName() {
		return launchConfigName != null ? launchConfigName : Messages.DefaultLaunchManagerDelegate_defaultLaunchName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification#setLaunchActionLabel(java.lang.String)
	 */
	@Override
	public void setLaunchActionLabel(String launchActionLabel) {
		this.launchActionLabel = launchActionLabel;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification#setLaunchConfigName(java.lang.String)
	 */
	@Override
	public void setLaunchConfigName(String launchConfigName) {
		this.launchConfigName = LaunchConfigHelper.getUniqueLaunchConfigName(launchConfigName);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification#isValid()
	 */
	@Override
	public boolean isValid() {
		return valid;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification#setIsValid(boolean)
	 */
	@Override
	public void setIsValid(boolean valid) {
		this.valid = valid;
		if (valid) {
			errorMessage = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification#getErrorMessage()
	 */
	@Override
	public String getErrorMessage() {
		if (!isValid()) {
			return errorMessage;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification#setErrorMessage(java.lang.String)
	 */
	@Override
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
