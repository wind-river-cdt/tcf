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
 * Launch attributes encapsulate an <code>String</code>-typed attribute key and an
 * <code>Object</code>-typed attribute value.
 */
public interface ILaunchAttribute extends IAdaptable {

	/**
	 * Returns the attribute key.
	 *
	 * @return The attribute key.
	 */
	public String getKey();

	/**
	 * Returns the attribute value.
	 *
	 * @return The attribute value.
	 */
	public Object getValue();

	/**
	 * Returns <code>true</code> if this attribute should be used only for creating launch
	 * configurations and not for finding them.
	 *
	 * @return <code>true</code> if only for create and not for find.
	 */
	public boolean isCreateOnlyAttribute();
}
