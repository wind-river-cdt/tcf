package org.eclipse.tcf.te.tcf.filesystem.internal.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.ui.interfaces.IViewerInput;

public class PeerModelViewerInput implements IViewerInput {
	// Property Change Listeners
	private List<IPropertyChangeListener> propertyChangeListeners;
	private IPeerModel peerModel;
	public PeerModelViewerInput(IPeerModel peerModel) {
		this.peerModel = peerModel;
		this.propertyChangeListeners = Collections.synchronizedList(new ArrayList<IPropertyChangeListener>());
	}
	
	@Override
    public void firePropertyChange(PropertyChangeEvent event) {
		for(IPropertyChangeListener listener : propertyChangeListeners) {
			listener.propertyChange(event);
		}
	}
	
	@Override
    public void addPropertyChangeListener(IPropertyChangeListener listener) {
		if(!propertyChangeListeners.contains(listener)) {
			propertyChangeListeners.add(listener);
		}
	}
	
	@Override
    public void removePropertyChangeListener(IPropertyChangeListener listener) {
		if(propertyChangeListeners.contains(listener)) {
			propertyChangeListeners.remove(listener);
		}
	}

	@Override
    public String getInputId() {
	    return peerModel.getPeerId();
    }
}
