package org.eclipse.tcf.te.tcf.filesystem.internal.adapters;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.ui.interfaces.IViewerInput;

public class ViewerInputAdapterFactory implements IAdapterFactory {
	private static final String PEER_PROPERTY_CHANGE_SOURCE_KEY = UIPlugin.getUniqueIdentifier()+".peer.propertySource"; //$NON-NLS-1$
	private Class<?>[] adapters = {IViewerInput.class};
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if(adaptableObject instanceof IPeerModel) {
			IPeerModel peerModel = (IPeerModel) adaptableObject;
			return getViewerInput(peerModel);
		}
		return null;
	}
	
	PeerModelViewerInput getViewerInput(final IPeerModel peerModel) {
		if (peerModel != null) {
			if (Protocol.isDispatchThread()) {
				PeerModelViewerInput model = (PeerModelViewerInput) peerModel.getProperty(PEER_PROPERTY_CHANGE_SOURCE_KEY);
				if (model == null) {
					model = new PeerModelViewerInput(peerModel);
					peerModel.setProperty(PEER_PROPERTY_CHANGE_SOURCE_KEY, model);
				}
				return model;
			}
			final AtomicReference<PeerModelViewerInput> reference = new AtomicReference<PeerModelViewerInput>();
			Protocol.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					reference.set(getViewerInput(peerModel));
				}
			});
			return reference.get();
		}
		return null;
	}
	@Override
	public Class[] getAdapterList() {
		return adapters;
	}

}
