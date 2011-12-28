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

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.ui.interfaces.IViewerInput;

/**
 * The adapter factory for IViewerInput.
 */
public class ViewerInputAdapterFactory implements IAdapterFactory {
	// The key to store and access the the viewer input object.
	private static final String VIEWER_INPUT_KEY = UIPlugin.getUniqueIdentifier()+".peer.viewerInput"; //$NON-NLS-1$
	// The adapters
	private Class<?>[] adapters = {IViewerInput.class};
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if(adaptableObject instanceof IPeerModel) {
			IPeerModel peerModel = (IPeerModel) adaptableObject;
			return getViewerInput(peerModel);
		}
		return null;
	}
	
	/**
	 * Get a viewer input from the specified peer model.
	 * 
	 * @param peerModel The peer model to get the viewer input from.
	 * @return The peer model's viewer input.
	 */
	PeerModelViewerInput getViewerInput(final IPeerModel peerModel) {
		if (peerModel != null) {
			if (Protocol.isDispatchThread()) {
				PeerModelViewerInput model = (PeerModelViewerInput) peerModel.getProperty(VIEWER_INPUT_KEY);
				if (model == null) {
					model = new PeerModelViewerInput(peerModel);
					peerModel.setProperty(VIEWER_INPUT_KEY, model);
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
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	public Class[] getAdapterList() {
		return adapters;
	}

}
