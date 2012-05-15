/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.jface.interfaces;

import org.eclipse.jface.dialogs.IMessageProvider;

/**
 * Interface to be implemented by validatable UI elements like
 * widgets, controls, sections or any other UI element with a
 * validatable state.
 */
public interface IValidatable extends IMessageProvider {

	/**
	 * Validates the state of the implementor.
	 * <p>
	 * On invocation, the method is expected to set the validatable
	 * elements message and message type ready to be queried immediately
	 * after the method returned.
	 *
	 * @return <code>True</code> if the validatable is valid, <code>false</code> otherwise.
	 */
	public boolean isValid();
}
