/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.controls.wire.network;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.tcf.te.ui.controls.net.RemoteHostAddressControl;
import org.eclipse.tcf.te.ui.controls.nls.Messages;
import org.eclipse.tcf.te.ui.jface.interfaces.IValidatingContainer;

/**
 * Network cable panel remote host address control implementation.
 */
public class NetworkAddressControl extends RemoteHostAddressControl {
	// Reference to the parent network cable panel
	private final NetworkCablePanel networkPanel;

	/**
	 * Constructor.
	 *
	 * @param networkPanel The parent network cable. Must not be <code>null</code>.
	 */
	public NetworkAddressControl(NetworkCablePanel networkPanel) {
		super(null);

		Assert.isNotNull(networkPanel);
		this.networkPanel = networkPanel;

		setEditFieldLabel(Messages.NetworkCablePanel_addressControl_label);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.BaseDialogPageControl#getValidatingContainer()
	 */
	@Override
	public IValidatingContainer getValidatingContainer() {
		return networkPanel.getParentControl().getValidatingContainer();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	@Override
	public void modifyText(ModifyEvent e) {
		super.modifyText(e);
		if (networkPanel.getParentControl() instanceof ModifyListener) {
			((ModifyListener)networkPanel.getParentControl()).modifyText(e);
		}
	}
}