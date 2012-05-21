/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.controls;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.tcf.te.tcf.ui.nls.Messages;
import org.eclipse.tcf.te.ui.controls.BaseDialogPageControl;
import org.eclipse.tcf.te.ui.controls.validator.NameOrIPValidator;
import org.eclipse.tcf.te.ui.controls.validator.Validator;
import org.eclipse.tcf.te.ui.controls.wire.network.NetworkAddressControl;
import org.eclipse.tcf.te.ui.controls.wire.network.NetworkCablePanel;
import org.eclipse.tcf.te.ui.jface.interfaces.IValidatingContainer;

/**
 * TCP transport type wizard configuration panel.
 */
public class TcpTransportPanel extends NetworkCablePanel {

	/**
	 * Local address control implementation.
	 */
	protected class MyNetworkAddressControl extends NetworkAddressControl {


		/**
		 * Constructor.
		 *
		 * @param networkPanel The parent network cable. Must not be <code>null</code>.
		 */
		public MyNetworkAddressControl(NetworkCablePanel networkPanel) {
			super(networkPanel);
			setEditFieldLabel(Messages.MyNetworkAddressControl_label);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tcf.te.ui.controls.net.RemoteHostAddressControl#configureEditFieldValidator(org.eclipse.tcf.te.ui.controls.validator.Validator)
		 */
		@Override
		protected void configureEditFieldValidator(Validator validator) {
			if (validator instanceof NameOrIPValidator) {
				validator.setMessageText(NameOrIPValidator.INFO_MISSING_NAME_OR_IP, Messages.MyNetworkAddressControl_information_missingTargetNameAddress);
				validator.setMessageText(NameOrIPValidator.ERROR_INVALID_NAME_OR_IP, Messages.MyNetworkAddressControl_error_invalidTargetNameAddress);
				validator.setMessageText(NameOrIPValidator.ERROR_INVALID_NAME, Messages.MyNetworkAddressControl_error_invalidTargetNameAddress);
				validator.setMessageText(NameOrIPValidator.ERROR_INVALID_IP, Messages.MyNetworkAddressControl_error_invalidTargetIpAddress);
				validator.setMessageText(NameOrIPValidator.INFO_CHECK_NAME, getUserInformationTextCheckNameAddress());
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tcf.te.ui.controls.net.RemoteHostAddressControl#getUserInformationTextCheckNameAddress()
		 */
		@Override
		protected String getUserInformationTextCheckNameAddress() {
			return Messages.MyNetworkAddressControl_information_checkNameAddressUserInformation;
		}

        /* (non-Javadoc)
         * @see org.eclipse.tcf.te.ui.controls.BaseDialogPageControl#getValidatingContainer()
         */
        @Override
        public IValidatingContainer getValidatingContainer() {
			return TcpTransportPanel.this.getParentControl().getValidatingContainer();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl#modifyText(org.eclipse.swt.events.ModifyEvent)
		 */
		@Override
		public void modifyText(ModifyEvent e) {
			super.modifyText(e);
			if (TcpTransportPanel.this.getParentControl() instanceof ModifyListener) {
				((ModifyListener)TcpTransportPanel.this.getParentControl()).modifyText(e);
			}
		}
	}

	/**
	 * Constructor.
	 *
	 * @param parentPageControl The parent control. Must not be <code>null</code>!
	 */
    public TcpTransportPanel(BaseDialogPageControl parentPageControl) {
	    super(parentPageControl);

    }

    /* (non-Javadoc)
     * @see org.eclipse.tcf.te.ui.controls.wire.network.NetworkCablePanel#doCreateAddressControl(org.eclipse.tcf.te.ui.controls.wire.network.NetworkCablePanel)
     */
    @Override
    protected NetworkAddressControl doCreateAddressControl(NetworkCablePanel parentPanel) {
        return new MyNetworkAddressControl(parentPanel);
    }

    /* (non-Javadoc)
     * @see org.eclipse.tcf.te.ui.controls.wire.network.NetworkCablePanel#getDefaultPort()
     */
    @Override
    protected String getDefaultPort() {
        return "1534"; //$NON-NLS-1$
    }
}
