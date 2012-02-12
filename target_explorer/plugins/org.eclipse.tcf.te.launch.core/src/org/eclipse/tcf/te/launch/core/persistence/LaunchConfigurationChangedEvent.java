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

import java.util.EventObject;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.tcf.te.runtime.events.EventManager;

/**
 * Launch configuration changed event.
 * <p>
 * This event will be fired by the {@link DefaultPersistenceDelegate} if
 * an attribute of a launch configuration has changed.
 */
public class LaunchConfigurationChangedEvent extends EventObject {
    private static final long serialVersionUID = 1934509221613969948L;

	private final String attributeName;
	private final Object oldValue;
	private final Object newValue;

	/**
	 * Constructor.
	 *
	 * @param source The changed launch configuration. Must not be <code>null</code>.
	 * @param attributeName The name of the changed launch configuration attribute. Must not be <code>null</code>.
	 * @param oldValue The old attribute value or <code>null</code>.
	 * @param newValue The new attribute value or <code>null</code>.
	 */
	public LaunchConfigurationChangedEvent(ILaunchConfiguration source, String attributeName, Object oldValue, Object newValue) {
		super(source);

		Assert.isNotNull(attributeName);
		this.attributeName = attributeName;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	/**
	 * Returns the changed launch configuration.
	 *
	 * @return The changed launch configuration.
	 */
	public ILaunchConfiguration getLaunchConfiguration() {
		return (ILaunchConfiguration)getSource();
	}

	/**
	 * Returns the name of the changed launch configuration.
	 *
	 * @return The name of the changed launch configuration.
	 */
	public String getAttributeName() {
		return attributeName;
	}

	/**
	 * Returns the old attribute value.
	 *
	 * @return The old attribute value or <code>null</code>.
	 */
	public Object getOldValue() {
		return oldValue;
	}

	/**
	 * Returns the new attribute value.
	 *
	 * @return The new attribute value or <code>null</code>.
	 */
	public Object getNewValue() {
		return newValue;
	}

	/* (non-Javadoc)
	 * @see java.util.EventObject#toString()
	 */
	@Override
	public String toString() {
		StringBuffer toString = new StringBuffer(getClass().getName());

		String prefix = ""; //$NON-NLS-1$
		// if tracing the event, formating them a little bit better readable.
		if (EventManager.isTracingEnabled())
			prefix = "\n\t\t"; //$NON-NLS-1$

		toString.append(prefix + "{launch configuration="); //$NON-NLS-1$
		toString.append(getLaunchConfiguration().getName());
		toString.append("," + prefix + "attributeName="); //$NON-NLS-1$ //$NON-NLS-2$
		toString.append(getAttributeName());
		toString.append("," + prefix + "old value="); //$NON-NLS-1$ //$NON-NLS-2$
		toString.append(getOldValue());
		toString.append("," + prefix + "new value="); //$NON-NLS-1$ //$NON-NLS-2$
		toString.append(getNewValue());
		toString.append("}"); //$NON-NLS-1$

		return toString.toString();
	}
}
