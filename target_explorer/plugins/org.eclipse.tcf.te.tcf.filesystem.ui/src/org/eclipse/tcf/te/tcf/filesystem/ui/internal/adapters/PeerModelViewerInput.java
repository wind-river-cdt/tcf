/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.adapters;

import org.eclipse.tcf.te.core.interfaces.IViewerInput;
import org.eclipse.tcf.te.core.utils.PropertyChangeProvider;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;

/**
 * The viewer input of an IPeerModel instance.
 */
public class PeerModelViewerInput extends PropertyChangeProvider implements IViewerInput {
	// The peer model.
	private IPeerModel peerModel;
	
	/**
	 * Create an instance with a peer model.
	 * 
	 * @param peerModel The peer model.
	 */
	public PeerModelViewerInput(IPeerModel peerModel) {
		this.peerModel = peerModel;
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
