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


/**
 * Interface to be implemented by container managing the validation
 * of contained validatable sub elements.
 */
public interface IValidatingContainer {

	/**
	 * Validates the container status.
	 * <p>
	 * If necessary, set the corresponding messages and message types to signal when some sub
	 * elements of the container needs user attention.
	 */
	public void validate();
}
