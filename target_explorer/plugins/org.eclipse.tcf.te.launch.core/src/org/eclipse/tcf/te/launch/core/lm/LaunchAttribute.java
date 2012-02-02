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

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchAttribute;

/**
 * Default launch attribute implementation.
 */
public class LaunchAttribute extends PlatformObject implements ILaunchAttribute {
	private final String key;
	private final Object value;
	private final boolean createOnly;

	/**
	 * Constructor.
	 *
	 * @param key The launch attribute key.
	 * @param value The launch attribute value.
	 * @param createOnly <code>true</code> if only for create.
	 */
	public LaunchAttribute(String key, Object value, boolean createOnly) {
		super();
		this.key = key;
		this.value = value;
		this.createOnly = createOnly;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchAttribute#getKey()
	 */
	@Override
    public String getKey() {
		return key;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchAttribute#getValue()
	 */
	@Override
    public Object getValue() {
		return value;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchAttribute#isCreateOnlyAttribute()
	 */
	@Override
    public boolean isCreateOnlyAttribute() {
		return createOnly;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder(getClass().getSimpleName());
		buffer.append(createOnly ? " (create only): " : ": "); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append(key);
		buffer.append(" = "); //$NON-NLS-1$
		buffer.append(value);
		return buffer.toString();
	}
}
