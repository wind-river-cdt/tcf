/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.interfaces;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * A property change provider is an observable object that fires property
 * change events, when one of its properties has changed. It has two methods,
 * addPropertyChangeListener and removePropertyChangeListener to add
 * and remove property change listeners which are interested in the property
 * change.
 * 
 * @see IViewerInput
 * @see PropertyChangeProvider
 * @see ProcessTreeNode
 * @see FSTreeNode
 */
public interface IPropertyChangeProvider {

	/**
	 * Add a property change listener to the provider. When this listener
	 * is already added, then it will not be added again.
	 * 
	 * @param listener The property change listener
	 */
	void addPropertyChangeListener(IPropertyChangeListener listener);

	/**
	 * Remove a property change listener from the provider if it is already
	 * added to provider.
	 * 
	 * @param listener The property change listener
	 */
	void removePropertyChangeListener(IPropertyChangeListener listener);

	/**
	 * Fire the property change event to the property change listeners
	 * of the provider.
	 * 
	 * @param event the property change  event.
	 */
	void firePropertyChange(PropertyChangeEvent event);
}
