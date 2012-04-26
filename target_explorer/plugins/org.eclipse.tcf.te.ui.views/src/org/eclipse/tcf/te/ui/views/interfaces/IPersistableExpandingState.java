/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.interfaces;

import org.eclipse.ui.IMemento;

/**
 * The interface to define the API to save and restore
 * the persisted states of a common viewer.
 * @see ViewExpandingState
 */
public interface IPersistableExpandingState {
	
	/**
	 * Restore the expanding state of this common viewer using
	 * the state persisted in the memento.
	 * 
	 * @param memento The memento that persists the expanding state.
	 */
	public void restoreExpandingState(IMemento memento);
	
	/**
	 * Save the expanding state of this common viewer to
	 * the memento.
	 * 
	 * @param memento The memento to persist the expanding state.
	 */
	public void saveExpandingState(IMemento memento);
}
