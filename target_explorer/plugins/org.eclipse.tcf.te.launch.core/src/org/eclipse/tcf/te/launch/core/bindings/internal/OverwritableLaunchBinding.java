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
import org.eclipse.tcf.te.launch.core.bindings.interfaces.IOverwritableLaunchBinding;

/**
 * Overwritable launch configuration type binding element implementation.
 */
public class OverwritableLaunchBinding extends VaryableLaunchBinding implements IOverwritableLaunchBinding {

	private String[] overwrites;

	/**
	 * Constructor.
	 *
	 * @param id The launch binding id. Must not be <code>null</code>.
	 * @param overwrites The overwritten launch binding id's or <code>null</code>.
	 * @param modes The launch modes or <code>null</code>
	 */
	public OverwritableLaunchBinding(String id, String overwrites, String modes) {
		this(id, overwrites, modes, null);
	}

	/**
	 * Constructor.
	 *
	 * @param id The launch binding id. Must not be <code>null</code>.
	 * @param overwrites The overwritten launch binding id's or <code>null</code>.
	 * @param modes The launch modes or <code>null</code>
	 * @param variants The launch mode variants or <code>null</code>.
	 */
	public OverwritableLaunchBinding(String id, String overwrites, String modes, String variants) {
		super(id, modes, variants);

		if (overwrites != null) this.overwrites = overwrites.trim().split("( )*,( )*"); //$NON-NLS-1$
		else this.overwrites = new String[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.bindings.interfaces.IOverwritableLaunchBinding#overwrites(java.lang.String)
	 */
	@Override
	public boolean overwrites(String id) {
		Assert.isNotNull(id);
		for (int i = 0; i < overwrites.length; i++) {
			if (id.equals(overwrites[i])) {
				return true;
			}
		}
		return overwrites.length == 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer toString = new StringBuffer();

		toString.append("OverwriteableLaunchBinding("); //$NON-NLS-1$
		toString.append(getId());
		toString.append(", launchModes"); //$NON-NLS-1$
		toString.append(toString(getModes()));
		if (getVariants().length > 0) {
			toString.append(", launchModeVariants"); //$NON-NLS-1$
			toString.append(toString(getVariants()));
		}
		toString.append(", overwrites"); //$NON-NLS-1$
		toString.append(toString(overwrites));
		toString.append(")"); //$NON-NLS-1$

		return toString.toString();
	}
}
