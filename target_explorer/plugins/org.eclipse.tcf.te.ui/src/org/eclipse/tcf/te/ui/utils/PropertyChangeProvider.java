/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.tcf.te.ui.interfaces.IPropertyChangeProvider;

/**
 * The base property change provider implementation. Classes that want to implement
 * IPropertyChangeProvider should extend this class to facilitate the implementation.  
 */
public class PropertyChangeProvider extends PlatformObject implements IPropertyChangeProvider {

	/**
	 * The property change listeners added to this node.
	 */
	private List<IPropertyChangeListener> propertyChangeListeners = Collections.synchronizedList(new ArrayList<IPropertyChangeListener>());
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.IViewerInput#firePropertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
    public void firePropertyChange(PropertyChangeEvent event) {
		synchronized (propertyChangeListeners) {
			for(IPropertyChangeListener listener : propertyChangeListeners) {
				listener.propertyChange(event);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.IViewerInput#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	@Override
    public void addPropertyChangeListener(IPropertyChangeListener listener) {
		if(!propertyChangeListeners.contains(listener)) {
			propertyChangeListeners.add(listener);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.IViewerInput#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	@Override
    public void removePropertyChangeListener(IPropertyChangeListener listener) {
		if(propertyChangeListeners.contains(listener)) {
			propertyChangeListeners.remove(listener);
		}
	}
}
