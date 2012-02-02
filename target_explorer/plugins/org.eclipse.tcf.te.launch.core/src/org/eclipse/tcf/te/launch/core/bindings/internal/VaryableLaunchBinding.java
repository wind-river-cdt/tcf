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

import org.eclipse.tcf.te.launch.core.bindings.interfaces.IVaryableLaunchBinding;

/**
 * Launch binding supporting launch mode variants.
 */
public class VaryableLaunchBinding extends LaunchBinding implements IVaryableLaunchBinding {
	private final String[] variants;

	/**
	 * Constructor.
	 *
	 * @param id The launch binding id. Must not be <code>null</code>.
	 * @param modes The launch modes or <code>null</code>
	 * @param variants The launch mode variants or <code>null</code>.
	 */
	public VaryableLaunchBinding(String id, String modes, String variants) {
		super(id, modes);

		if (variants != null) this.variants = variants.trim().split("( )*,( )*"); //$NON-NLS-1$
		else this.variants = new String[0];
	}

	/**
	 * Returns the list of handled variants.
	 *
	 * @return The list of handled variants or an empty list.
	 */
	public final String[] getVariants() {
		return variants;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.bindings.interfaces.IVaryableLaunchBinding#isValidLaunchMode(java.lang.String, java.lang.String)
	 */
	@Override
    public boolean isValidLaunchMode(String launchMode, String variant) {
		boolean valid = isValidLaunchMode(launchMode);
		if (!valid) return false;

		if (variant == null || variant.length() == 0)
			return true;

		for (String candidate : getVariants())
			if (variant.equals(candidate)) return true;

		return getVariants().length == 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer toString = new StringBuffer();

		toString.append("LaunchBinding("); //$NON-NLS-1$
		toString.append(getId());
		toString.append(", launchModes"); //$NON-NLS-1$
		toString.append(toString(getModes()));
		toString.append(", launchModeVariants"); //$NON-NLS-1$
		toString.append(toString(getVariants()));
		toString.append(")"); //$NON-NLS-1$

		return toString.toString();
	}
}
