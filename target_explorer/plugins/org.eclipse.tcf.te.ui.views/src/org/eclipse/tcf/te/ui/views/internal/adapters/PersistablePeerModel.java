/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.internal.adapters;

import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

/**
 * The persistable element implementation for an IPeerModel.
 */
public class PersistablePeerModel implements IPersistableElement {
	// The peer model to be persisted.
	private IPeerModel peerModel;

	/**
	 * Constructor
	 */
	public PersistablePeerModel(IPeerModel peerModel) {
		this.peerModel = peerModel;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IPersistable#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void saveState(IMemento memento) {
		memento.putString("peerId", peerModel.getPeerId()); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IPersistableElement#getFactoryId()
	 */
	@Override
	public String getFactoryId() {
		return "org.eclipse.tcf.te.ui.views.peerFactory"; //$NON-NLS-1$
	}
}
