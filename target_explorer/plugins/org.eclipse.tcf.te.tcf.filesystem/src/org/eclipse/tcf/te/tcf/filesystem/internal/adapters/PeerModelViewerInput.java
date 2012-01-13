/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.ui.interfaces.IViewerInput;

/**
 * The viewer input of an IPeerModel instance.
 */
public class PeerModelViewerInput implements IViewerInput {
	// Property Change Listeners
	private List<IPropertyChangeListener> propertyChangeListeners;
	// The peer model.
	private IPeerModel peerModel;
	
	/**
	 * Create an instance with a peer model.
	 * 
	 * @param peerModel The peer model.
	 */
	public PeerModelViewerInput(IPeerModel peerModel) {
		this.peerModel = peerModel;
		this.propertyChangeListeners = Collections.synchronizedList(new ArrayList<IPropertyChangeListener>());
	}
	
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.IViewerInput#getInputId()
	 */
	@Override
    public String getInputId() {
	    return peerModel.getPeerId();
    }
}
