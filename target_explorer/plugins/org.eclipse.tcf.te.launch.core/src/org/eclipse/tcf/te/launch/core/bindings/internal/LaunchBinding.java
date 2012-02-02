/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.bindings.internal;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.te.launch.core.bindings.interfaces.ILaunchBinding;


/**
 * Launch configuration type binding element implementation.
*/
public class LaunchBinding implements ILaunchBinding {
	// The id of the bound element
	private String id;
	// The launch modes the binding applies to
	private final String[] modes;

	/**
	 * Constructor.
	 *
	 * @param id The id of the element to bind. Must not be <code>null</code>.
	 * @param modes The launch modes or <code>null</code>
	 */
	public LaunchBinding(String id, String modes) {
		Assert.isNotNull(id);
		this.id = id;

		if (modes != null) this.modes = modes.trim().split("( )*,( )*"); //$NON-NLS-1$
		else this.modes = new String[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.bindings.interfaces.ILaunchBinding#getId()
	 */
	@Override
    public final String getId() {
		return id;
	}

	/**
	 * Returns the list of the launch modes this binding applies to.
	 *
	 * @return The list of launch modes or an empty list.
	 */
	public final String[] getModes() {
		return modes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.bindings.interfaces.ILaunchBinding#isValidLaunchMode(java.lang.String)
	 */
	@Override
    public boolean isValidLaunchMode(String launchMode) {
		if (launchMode == null || launchMode.length() == 0)
			return true;

		for (String candidate : getModes())
			if (launchMode.equals(candidate)) return true;

		return getModes().length == 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer toString = new StringBuffer();

		toString.append("LaunchBinding("); //$NON-NLS-1$
		toString.append(id);
		toString.append(", launchModes"); //$NON-NLS-1$
		toString.append(toString(getModes()));
		toString.append(")"); //$NON-NLS-1$

		return toString.toString();
	}

	/**
	 * Creates a string representation of the given string array.
	 *
	 * @param strings The string array or <code>null</code>.
	 * @return The string representation of the array.
	 */
	protected String toString(String[] strings) {
		StringBuffer toString = new StringBuffer();
		toString.append('[');
		if (strings != null) {
			for (int i = 0; i < strings.length; i++) {
				if (i > 0) toString.append(',');
				toString.append(strings[i]);
			}
		}
		toString.append(']');
		return toString.toString();
	}
}
