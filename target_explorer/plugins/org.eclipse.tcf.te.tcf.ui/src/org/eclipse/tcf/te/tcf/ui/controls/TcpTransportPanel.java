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

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.tcf.ui.nls.Messages;
import org.eclipse.tcf.te.ui.controls.BaseDialogPageControl;
import org.eclipse.tcf.te.ui.controls.validator.NameOrIPValidator;
import org.eclipse.tcf.te.ui.controls.validator.Validator;
import org.eclipse.tcf.te.ui.controls.wire.network.NetworkAddressControl;
import org.eclipse.tcf.te.ui.controls.wire.network.NetworkCablePanel;
import org.eclipse.tcf.te.ui.controls.wire.network.NetworkPortControl;
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

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.interfaces.IWizardConfigurationPanel#dataChanged(org.eclipse.tcf.te.runtime.interfaces.nodes.IPropertiesContainer, org.eclipse.swt.events.TypedEvent)
	 */
	@Override
    public boolean dataChanged(IPropertiesContainer data, TypedEvent e) {
		Assert.isNotNull(data);

		boolean isDirty = false;

		NetworkAddressControl addressControl = getAddressControl();
		if (addressControl != null) {
			String address = addressControl.getEditFieldControlText();
			if (address != null) isDirty |= !address.equals(data.getStringProperty(IPeer.ATTR_IP_HOST));
		}

		NetworkPortControl portControl = getPortControl();
		if (portControl != null) {
			String port = portControl.getEditFieldControlText();
			if (port != null) isDirty |= !port.equals(data.getStringProperty(IPeer.ATTR_IP_PORT));
		}

		return isDirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.wizards.interfaces.ISharedDataExchangeNode#setupData(org.eclipse.tcf.te.runtime.interfaces.nodes.IPropertiesContainer)
	 */
	@Override
    public void setupData(IPropertiesContainer data) {
		if (data == null) return;

		NetworkAddressControl addressControl = getAddressControl();
		if (addressControl != null) {
			addressControl.setEditFieldControlText(data.getStringProperty(IPeer.ATTR_IP_HOST));
		}

		NetworkPortControl portControl = getPortControl();
		if (portControl != null) {
			portControl.setEditFieldControlText(data.getStringProperty(IPeer.ATTR_IP_PORT));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.wizards.interfaces.ISharedDataExchangeNode#extractData(org.eclipse.tcf.te.runtime.interfaces.nodes.IPropertiesContainer)
	 */
	@Override
    public void extractData(IPropertiesContainer data) {
		if (data == null) return;

		NetworkAddressControl addressControl = getAddressControl();
		if (addressControl != null) {
			String host = addressControl.getEditFieldControlText();
			data.setProperty(IPeer.ATTR_IP_HOST, !"".equals(host) ? host : null); //$NON-NLS-1$
		}

		NetworkPortControl portControl = getPortControl();
		if (portControl != null) {
			String port = portControl.getEditFieldControlText();
			data.setProperty(IPeer.ATTR_IP_PORT, !"".equals(port) ? port : null); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.data.IDataExchangeNode3#removeData(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer)
	 */
	@Override
    public void removeData(IPropertiesContainer data) {
		if (data == null) return;
		data.setProperty(IPeer.ATTR_IP_HOST, null);
		data.setProperty(IPeer.ATTR_IP_PORT, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.data.IDataExchangeNode3#copyData(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer)
	 */
	@Override
	public void copyData(IPropertiesContainer src, IPropertiesContainer dst) {
		Assert.isNotNull(src);
		Assert.isNotNull(dst);
		dst.setProperty(IPeer.ATTR_IP_HOST, src.getStringProperty(IPeer.ATTR_IP_HOST));
		dst.setProperty(IPeer.ATTR_IP_PORT, src.getStringProperty(IPeer.ATTR_IP_PORT));
	}
}
