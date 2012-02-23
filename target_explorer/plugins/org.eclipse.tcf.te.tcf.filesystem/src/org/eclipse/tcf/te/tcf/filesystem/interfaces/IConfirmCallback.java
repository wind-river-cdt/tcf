/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.interfaces;

/**
 * A confirmation callback used to get  UI confirmation from user for a long process
 */
public interface IConfirmCallback {
	// Yes button ID.
	int YES = 0;
	// Yes to All button ID.
	int YES_TO_ALL = 1;
	// No to All button ID.
	int NO = 2;
	// Cancel button ID.
	int CANCEL = 3;
	// OK button ID.
	int OK = YES;
	// No to all button ID.
	int NO_TO_ALL = 4;
	/**
	 * Test if the given object requires confirmation.
	 * 
	 * @param object The object being tested.
	 * @return true if it requires confirmation.
	 */
	boolean requires(Object object);
	/**
	 * Confirm with the user weather the process should continue, continue for all, skip or cancel.
	 * 
	 * @param object The object being tested.
	 * @return a button ID the user selects during confirmation.
	 */
	int confirms(Object object);
}
