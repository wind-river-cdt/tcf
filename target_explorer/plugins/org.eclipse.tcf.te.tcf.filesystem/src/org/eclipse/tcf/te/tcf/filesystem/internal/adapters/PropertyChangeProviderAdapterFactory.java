package org.eclipse.tcf.te.tcf.filesystem.internal.adapters;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.ui.interfaces.IPropertyChangeProvider;

public class PropertyChangeProviderAdapterFactory implements IAdapterFactory {
	private static final String PEER_PROPERTY_CHANGE_SOURCE_KEY = UIPlugin.getUniqueIdentifier()+".peer.propertySource"; //$NON-NLS-1$
	private Class<?>[] adapters = {IPropertyChangeProvider.class};
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if(adaptableObject instanceof IPeerModel) {
			IPeerModel peerModel = (IPeerModel) adaptableObject;
			return getPropertyChangeSource(peerModel);
		}
		return null;
	}
	PeerModelPropertyChangeProvider getPropertyChangeSource(final IPeerModel peerModel) {
		if (peerModel != null) {
			if (Protocol.isDispatchThread()) {
				PeerModelPropertyChangeProvider model = (PeerModelPropertyChangeProvider) peerModel.getProperty(PEER_PROPERTY_CHANGE_SOURCE_KEY);
				if (model == null) {
					model = new PeerModelPropertyChangeProvider();
					peerModel.setProperty(PEER_PROPERTY_CHANGE_SOURCE_KEY, model);
				}
				return model;
			}
			final AtomicReference<PeerModelPropertyChangeProvider> reference = new AtomicReference<PeerModelPropertyChangeProvider>();
			Protocol.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					reference.set(getPropertyChangeSource(peerModel));
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
