/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipe.tcf.te.launch.ui.tabs;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.tcf.te.launch.core.bindings.LaunchConfigTypeBindingsManager;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchManagerDelegate;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification;
import org.eclipse.tcf.te.launch.core.persistence.DefaultPersistenceDelegate;
import org.eclipse.tcf.te.launch.core.selection.interfaces.ILaunchSelection;

/**
 * Abstract base class of the Launch framework UI infrastructure.
 */
public abstract class AbstractLaunchConfigurationTab extends org.eclipse.debug.ui.AbstractLaunchConfigurationTab {

	/**
	 * Constructor.
	 */
	public AbstractLaunchConfigurationTab() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// defaults are set within the tab group using the default settings of the launch manager delegate.
	}

	/**
	 * Creates a launch specification from the given launch selection for the given launch
	 * configuration type and launch mode.
	 *
	 * @param typeId The launch configuration type id. Must not be <code>null</code>.
	 * @param mode The launch mode. Must not be <code>null</code>.
	 * @param selection The launch selection. Must not be <code>null</code>.
	 *
	 * @return The launch specification.
	 */
	protected ILaunchSpecification getLaunchSpecification(String typeId, String mode, ILaunchSelection selection) {
		Assert.isNotNull(typeId);
		Assert.isNotNull(mode);
		Assert.isNotNull(selection);

		ILaunchManagerDelegate delegate = LaunchConfigTypeBindingsManager.getInstance().getLaunchManagerDelegate(typeId, mode);
		return delegate.getLaunchSpecification(typeId, selection);
	}

	/**
	 * Stores the given boolean attribute value under the given attribute id if the value
	 * has changed compared to the already stored value under the given attribute id or
	 * if the attribute id has not been stored yet.
	 *
	 * @param wc The launch configuration working copy instance to apply the attribute to. Must be not <code>null</code>.
	 * @param attributeId The attribute id to store the attribute value under. Must be not <code>null</code>.
	 * @param attributeValue The attribute value to store under the given attribute id.
	 *
	 * @see DefaultPersistenceDelegate
	 */
	protected final void setAttribute(ILaunchConfigurationWorkingCopy wc, String attributeId, boolean attributeValue) {
		if (wc == null || attributeId == null) return;
		DefaultPersistenceDelegate.setAttribute(wc, attributeId, attributeValue);
	}

	/**
	 * Stores the given integer attribute value under the given attribute id if the value
	 * has changed compared to the already stored value under the given attribute id or
	 * if the attribute id has not been stored yet.
	 *
	 * @param wc The launch configuration working copy instance to apply the attribute to. Must be not <code>null</code>.
	 * @param attributeId The attribute id to store the attribute value under. Must be not <code>null</code>.
	 * @param attributeValue The attribute value to store under the given attribute id.
	 *
	 * @see DefaultPersistenceDelegate
	 */
	protected final void setAttribute(ILaunchConfigurationWorkingCopy wc, String attributeId, int attributeValue) {
		if (wc == null || attributeId == null) return;
		DefaultPersistenceDelegate.setAttribute(wc, attributeId, attributeValue);
	}

	/**
	 * Stores the given string attribute value under the given attribute id if the value
	 * has changed compared to the already stored value under the given attribute id or
	 * if the attribute id has not been stored yet. If the attribute value is <code>null</code>,
	 * the attribute id will be removed from the given launch configuration working copy.
	 *
	 * @param wc The launch configuration working copy instance to apply the attribute to. Must be not <code>null</code>.
	 * @param attributeId The attribute id to store the attribute value under. Must be not <code>null</code>.
	 * @param attributeValue The attribute value to store under the given attribute id.
	 *
	 * @see DefaultPersistenceDelegate
	 */
	protected final void setAttribute(ILaunchConfigurationWorkingCopy wc, String attributeId, String attributeValue) {
		if (wc == null || attributeId == null) return;
		DefaultPersistenceDelegate.setAttribute(wc, attributeId, attributeValue);
	}

	/**
	 * Stores the given list attribute value under the given attribute id if the value
	 * has changed compared to the already stored value under the given attribute id or
	 * if the attribute id has not been stored yet. If the attribute value is <code>null</code>,
	 * the attribute id will be removed from the given launch configuration working copy.
	 *
	 * @param wc The launch configuration working copy instance to apply the attribute to. Must be not <code>null</code>.
	 * @param attributeId The attribute id to store the attribute value under. Must be not <code>null</code>.
	 * @param attributeValue The attribute value to store under the given attribute id.
	 *
	 * @see DefaultPersistenceDelegate
	 */
	protected final void setAttribute(ILaunchConfigurationWorkingCopy wc, String attributeId, List<?> attributeValue) {
		if (wc == null || attributeId == null) return;
		DefaultPersistenceDelegate.setAttribute(wc, attributeId, attributeValue);
	}

	/**
	 * Stores the given map attribute value under the given attribute id if the value
	 * has changed compared to the already stored value under the given attribute id or
	 * if the attribute id has not been stored yet. If the attribute value is <code>null</code>,
	 * the attribute id will be removed from the given launch configuration working copy.
	 *
	 * @param wc The launch configuration working copy instance to apply the attribute to. Must be not <code>null</code>.
	 * @param attributeId The attribute id to store the attribute value under. Must be not <code>null</code>.
	 * @param attributeValue The attribute value to store under the given attribute id.
	 *
	 * @see DefaultPersistenceDelegate
	 */
	protected final void setAttribute(ILaunchConfigurationWorkingCopy wc, String attributeId, Map<?, ?> attributeValue) {
		if (wc == null || attributeId == null) return;
		DefaultPersistenceDelegate.setAttribute(wc, attributeId, attributeValue);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#setAttribute(java.lang.String, org.eclipse.debug.core.ILaunchConfigurationWorkingCopy, boolean, boolean)
	 */
	@Override
	protected final void setAttribute(String attribute, ILaunchConfigurationWorkingCopy configuration, boolean value, boolean defaultValue) {
		// Redirect the implementation to our internal implementation.
		if (value == defaultValue) {
			setAttribute(configuration, attribute, (String)null);
		} else {
			setAttribute(configuration, attribute, value);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getLaunchConfigurationDialog()
	 */
	@Override
	public final ILaunchConfigurationDialog getLaunchConfigurationDialog() {
		// Redeclare the getLaunchConfigurationDialog() to be public.
		return super.getLaunchConfigurationDialog();
	}

	/**
	 * Returns if or if not this launch configuration tab instance is the
	 * active tab within the open launch configuration dialog instance.
	 *
	 * @return <code>True</code> if the launch configuration tab instance is the active tab, <code>false</code> otherwise.
	 */
	public final boolean isActiveTab() {
		return getLaunchConfigurationDialog() != null && getLaunchConfigurationDialog().getActiveTab() == this;
	}

}
