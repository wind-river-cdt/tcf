/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.bindings.interfaces;

/**
 * Interface to be implemented by a launch configuration type binding element
 * which is supporting launch mode variants.
 */
public interface IVaryableLaunchBinding extends ILaunchBinding {

	/**
	 * Returns if or if not the given launch mode is handled by this binding.
	 * <p>
	 * <b>Note:</b> The launch mode and launch mode variant are considered valid
	 *              if <code>null</code> or an empty string is passed.
	 *
	 * @param mode The launch mode. Must not be <code>null</code>.
	 * @param variant The launch mode variant to check or <code>null</code>.
	 *
	 * @return <code>True</code> if the launch mode is valid, <code>false</code> otherwise.
	 */
	public boolean isValidLaunchMode(String mode, String variant);
}
