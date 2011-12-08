/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.controls.wire.network;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.te.core.nodes.interfaces.wire.IWireTypeNetwork;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.ui.controls.BaseDialogPageControl;
import org.eclipse.tcf.te.ui.controls.nls.Messages;
import org.eclipse.tcf.te.ui.controls.panels.AbstractWizardConfigurationPanel;
import org.eclipse.tcf.te.ui.swt.SWTControlUtil;
import org.eclipse.tcf.te.ui.wizards.interfaces.ISharedDataWizardPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Network cable wire type wizard configuration panel.
 */
public class NetworkCablePanel extends AbstractWizardConfigurationPanel implements ISharedDataWizardPage {
	private NetworkAddressControl addressControl = null;
	private NetworkPortControl portControl = null;

	/**
	 * Constructor.
	 *
	 * @param parentPageControl The parent control. Must not be <code>null</code>!
	 */
	public NetworkCablePanel(BaseDialogPageControl parentPageControl) {
		super(parentPageControl);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.panels.AbstractWizardConfigurationPanel#dispose()
	 */
	@Override
	public void dispose() {
		if (addressControl != null) { addressControl.dispose(); addressControl = null; }
		if (portControl != null) { portControl.dispose(); portControl = null; }
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.interfaces.IWizardConfigurationPanel#setupPanel(org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
	 */
	@Override
    public void setupPanel(Composite parent, FormToolkit toolkit) {
		Assert.isNotNull(parent);
		Assert.isNotNull(toolkit);

		boolean adjustBackgroundColor = getParentControl().getParentPage() != null;

		Composite panel = toolkit.createComposite(parent);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0; layout.marginWidth = 0;
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		if (adjustBackgroundColor) panel.setBackground(parent.getBackground());

		setControl(panel);

		// Create the wire type section
		Section section = toolkit.createSection(panel, ExpandableComposite.TITLE_BAR);
		Assert.isNotNull(section);
		section.setText(Messages.NetworkCablePanel_section);
		section.setLayout(new GridLayout());
		section.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		if (adjustBackgroundColor) section.setBackground(panel.getBackground());

		Composite client = toolkit.createComposite(section);
		Assert.isNotNull(client);
		client.setLayout(new GridLayout());
		client.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		if (adjustBackgroundColor) client.setBackground(section.getBackground());
		section.setClient(client);

		addressControl = doCreateAddressControl(this);
		addressControl.setupPanel(client);

		portControl = doCreatePortControl(this);
		portControl.setParentControlIsInnerPanel(true);
		portControl.setupPanel(addressControl.getInnerPanelComposite());
	}

	/**
	 * Creates the address control instance.
	 *
	 * @param parentPanel The parent network cable panel. Must not be <code>null</code>.
	 * @return The address control instance.
	 */
	protected NetworkAddressControl doCreateAddressControl(NetworkCablePanel parentPanel) {
		Assert.isNotNull(parentPanel);
		return new NetworkAddressControl(parentPanel);
	}

	/**
	 * Creates the port control instance.
	 *
	 * @param parentPanel The parent network cable panel. Must not be <code>null</code>.
	 * @return The port control instance.
	 */
	protected NetworkPortControl doCreatePortControl(NetworkCablePanel parentPanel) {
		Assert.isNotNull(parentPanel);
		return new NetworkPortControl(parentPanel);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.panels.AbstractWizardConfigurationPanel#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
	    super.setEnabled(enabled);
	    if (addressControl != null) {
	    	if (keepLabelsAlwaysEnabled()) {
	    		SWTControlUtil.setEnabled(addressControl.getEditFieldControl(), enabled);
	    		SWTControlUtil.setEnabled(addressControl.getButtonControl(), enabled);
	    	} else {
	    		addressControl.setEnabled(enabled);
	    	}
	    }
	    if (portControl != null) {
	    	if (keepLabelsAlwaysEnabled()) {
	    		SWTControlUtil.setEnabled(portControl.getEditFieldControl(), enabled);
	    		SWTControlUtil.setEnabled(portControl.getButtonControl(), enabled);
	    	} else {
	    		portControl.setEnabled(enabled);
	    	}
	    }
	}

	/**
	 * Returns if or if not the control labels shall be kept enabled even
	 * if the state of the control is set to disabled.
	 *
	 * @return <code>True</code> to keep control labels enabled, <code>false</code> otherwise.
	 */
	protected boolean keepLabelsAlwaysEnabled() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.panels.AbstractWizardConfigurationPanel#isValid()
	 */
	@Override
	public boolean isValid() {
		boolean valid = super.isValid();
		if (!valid) return false;

		valid = addressControl.isValid();
		setMessage(addressControl.getMessage(), addressControl.getMessageType());

		valid &= portControl.isValid();
		if (portControl.getMessageType() > getMessageType()) {
			setMessage(portControl.getMessage(), portControl.getMessageType());
		}

		return valid;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.interfaces.IWizardConfigurationPanel#dataChanged(org.eclipse.tcf.te.runtime.interfaces.nodes.IPropertiesContainer, org.eclipse.swt.events.TypedEvent)
	 */
	@Override
    public boolean dataChanged(IPropertiesContainer data, TypedEvent e) {
		Assert.isNotNull(data);

		boolean isDirty = false;

        Map<String, Object> container = (Map<String, Object>)data.getProperty(IWireTypeNetwork.PROPERTY_CONTAINER_NAME);
		if (container == null) container = new HashMap<String, Object>();

		if (addressControl != null) {
			String address = addressControl.getEditFieldControlText();
			if (address != null) isDirty |= !address.equals(container.get(IWireTypeNetwork.PROPERTY_NETWORK_ADDRESS) != null ? container.get(IWireTypeNetwork.PROPERTY_NETWORK_ADDRESS) : ""); //$NON-NLS-1$
		}

		if (portControl != null) {
			String port = portControl.getEditFieldControlText();
			if (port != null) isDirty |= !port.equals(container.get(IWireTypeNetwork.PROPERTY_NETWORK_PORT) != null ? container.get(IWireTypeNetwork.PROPERTY_NETWORK_PORT) : ""); //$NON-NLS-1$
		}

		return isDirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.wizards.interfaces.ISharedDataWizardPage#setupData(org.eclipse.tcf.te.runtime.interfaces.nodes.IPropertiesContainer)
	 */
	@Override
    public void setupData(IPropertiesContainer data) {
		if (data == null) return;

        Map<String, Object> container = (Map<String, Object>)data.getProperty(IWireTypeNetwork.PROPERTY_CONTAINER_NAME);
		if (container == null) container = new HashMap<String, Object>();

		if (addressControl != null) {
			addressControl.setEditFieldControlText((String)container.get(IWireTypeNetwork.PROPERTY_NETWORK_ADDRESS));
		}

		if (portControl != null) {
			portControl.setEditFieldControlText((String)container.get(IWireTypeNetwork.PROPERTY_NETWORK_PORT));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.wizards.interfaces.ISharedDataWizardPage#extractData(org.eclipse.tcf.te.runtime.interfaces.nodes.IPropertiesContainer)
	 */
	@Override
    public void extractData(IPropertiesContainer data) {
		if (data == null) return;

        Map<String, Object> container = (Map<String, Object>)data.getProperty(IWireTypeNetwork.PROPERTY_CONTAINER_NAME);
		if (container == null) container = new HashMap<String, Object>();

		if (addressControl != null) {
			container.put(IWireTypeNetwork.PROPERTY_NETWORK_ADDRESS, addressControl.getEditFieldControlText());
		}

		if (portControl != null) {
			container.put(IWireTypeNetwork.PROPERTY_NETWORK_PORT, portControl.getEditFieldControlText());
		}

		data.setProperty(IWireTypeNetwork.PROPERTY_CONTAINER_NAME, !container.isEmpty() ? container : null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.wizards.interfaces.ISharedDataWizardPage#initializeData(org.eclipse.tcf.te.runtime.interfaces.nodes.IPropertiesContainer)
	 */
	@Override
    public void initializeData(IPropertiesContainer data) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.wizards.interfaces.ISharedDataWizardPage#removeData(org.eclipse.tcf.te.runtime.interfaces.nodes.IPropertiesContainer)
	 */
	@Override
    public void removeData(IPropertiesContainer data) {
		if (data == null) return;
		data.setProperty(IWireTypeNetwork.PROPERTY_CONTAINER_NAME, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.panels.AbstractWizardConfigurationPanel#doSaveWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
	 */
	@Override
	public void doSaveWidgetValues(IDialogSettings settings, String idPrefix) {
		super.doSaveWidgetValues(settings, idPrefix);
		if (addressControl != null) addressControl.doSaveWidgetValues(settings, idPrefix);
		if (portControl != null) portControl.doSaveWidgetValues(settings, idPrefix);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.panels.AbstractWizardConfigurationPanel#doRestoreWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
	 */
	@Override
	public void doRestoreWidgetValues(IDialogSettings settings, String idPrefix) {
		super.doRestoreWidgetValues(settings, idPrefix);
		if (addressControl != null) addressControl.doRestoreWidgetValues(settings, idPrefix);
		if (portControl != null) portControl.doRestoreWidgetValues(settings, idPrefix);
	}
}
