/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.editor.sections;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
import org.eclipse.tcf.te.tcf.core.interfaces.ITransportTypes;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.ui.editor.controls.TransportSectionTypeControl;
import org.eclipse.tcf.te.tcf.ui.editor.controls.TransportSectionTypePanelControl;
import org.eclipse.tcf.te.tcf.ui.nls.Messages;
import org.eclipse.tcf.te.tcf.ui.wizards.controls.CustomTransportPanel;
import org.eclipse.tcf.te.tcf.ui.wizards.controls.PipeTransportPanel;
import org.eclipse.tcf.te.tcf.ui.wizards.controls.TcpTransportPanel;
import org.eclipse.tcf.te.tcf.ui.wizards.controls.TransportTypeControl;
import org.eclipse.tcf.te.ui.controls.interfaces.IWizardConfigurationPanel;
import org.eclipse.tcf.te.ui.forms.parts.AbstractSection;
import org.eclipse.tcf.te.ui.swt.SWTControlUtil;
import org.eclipse.tcf.te.ui.views.editor.AbstractEditorPage;
import org.eclipse.tcf.te.ui.wizards.interfaces.ISharedDataWizardPage;
import org.eclipse.tcf.te.ui.wizards.interfaces.IValidatableWizardPage;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Peer transport section implementation.
 */
public class TransportSection extends AbstractSection implements IValidatableWizardPage {
	// The section sub controls
	private TransportSectionTypeControl transportTypeControl = null;
	private TransportSectionTypePanelControl transportTypePanelControl = null;

	// Reference to the original data object
	/* default */ IPeerModel od;
	// Reference to a copy of the original data
	/* default */ final IPropertiesContainer odc = new PropertiesContainer();
	// Reference to the properties container representing the working copy for the section
	/* default */ final IPropertiesContainer wc = new PropertiesContainer();

	/**
	 * Constructor.
	 *
	 * @param form The parent managed form. Must not be <code>null</code>.
	 * @param parent The parent composite. Must not be <code>null</code>.
	 */
	public TransportSection(IManagedForm form, Composite parent) {
		super(form, parent, Section.DESCRIPTION);
		createClient(getSection(), form.getToolkit());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		if (transportTypeControl != null) { transportTypeControl.dispose(); transportTypeControl = null; }
		if (transportTypePanelControl != null) { transportTypePanelControl.dispose(); transportTypePanelControl = null; }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.forms.parts.AbstractSection#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (TransportSectionTypeControl.class.equals(adapter)) {
			return transportTypeControl;
		}
		if (TransportSectionTypePanelControl.class.equals(adapter)) {
			return transportTypePanelControl;
		}
	    return super.getAdapter(adapter);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.forms.parts.AbstractSection#createClient(org.eclipse.ui.forms.widgets.Section, org.eclipse.ui.forms.widgets.FormToolkit)
	 */
	@Override
	protected void createClient(Section section, FormToolkit toolkit) {
		Assert.isNotNull(section);
		Assert.isNotNull(toolkit);

		// Configure the section
		section.setText(Messages.TransportSection_title);
		section.setDescription(Messages.TransportSection_description);

		section.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// Create the section client
		Composite client = createClientContainer(section, 2, toolkit);
		Assert.isNotNull(client);
		section.setClient(client);

		// Create the transport type control
		transportTypeControl = new TransportSectionTypeControl(this);
		transportTypeControl.setFormToolkit(toolkit);
		transportTypeControl.setupPanel(client);

		createEmptySpace(client, 2, toolkit);

		// The transport type specific controls are placed into a stack
		transportTypePanelControl = new TransportSectionTypePanelControl(this);

		// Create and add the panels
		TcpTransportPanel tcpTransportPanel = new TcpTransportPanel(transportTypePanelControl);
		transportTypePanelControl.addConfigurationPanel(ITransportTypes.TRANSPORT_TYPE_TCP, tcpTransportPanel);
		transportTypePanelControl.addConfigurationPanel(ITransportTypes.TRANSPORT_TYPE_SSL, tcpTransportPanel);
		transportTypePanelControl.addConfigurationPanel(ITransportTypes.TRANSPORT_TYPE_PIPE, new PipeTransportPanel(transportTypePanelControl));
		transportTypePanelControl.addConfigurationPanel(ITransportTypes.TRANSPORT_TYPE_CUSTOM, new CustomTransportPanel(transportTypePanelControl));

		// Setup the panel control
		transportTypePanelControl.setupPanel(client, TransportTypeControl.TRANSPORT_TYPES, toolkit);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.horizontalSpan = 2;
		transportTypePanelControl.getPanel().setLayoutData(layoutData);
		toolkit.adapt(transportTypePanelControl.getPanel());

		transportTypePanelControl.showConfigurationPanel(transportTypeControl.getSelectedTransportType());

		// Adjust the control enablement
		updateEnablement();
	}

	/**
	 * Indicates whether the sections parent page has become the active in the editor.
	 *
	 * @param active <code>True</code> if the parent page should be visible, <code>false</code> otherwise.
	 */
	public void setActive(boolean active) {
		// If the parent page has become the active and it does not contain
		// unsaved data, than fill in the data from the selected node
		if (active) {
			// Leave everything unchanged if the page is in dirty state
			if (getManagedForm().getContainer() instanceof AbstractEditorPage
					&& !((AbstractEditorPage)getManagedForm().getContainer()).isDirty()) {
				Object node = ((AbstractEditorPage)getManagedForm().getContainer()).getEditorInputNode();
				if (node instanceof IPeerModel) {
					setupData((IPeerModel)node);
				}
			}
		}
	}

	/**
	 * Initialize the page widgets based of the data from the given peer node.
	 * <p>
	 * This method may called multiple times during the lifetime of the page and
	 * the given configuration node might be even <code>null</code>.
	 *
	 * @param node The peer node or <code>null</code>.
	 */
	public void setupData(final IPeerModel node) {
		// If the passed in node is the same as the previous one,
		// no need for updating the section widgets.
		if ((node == null && od == null) || (node != null && node.equals(od))) {
			updateEnablement();
			return;
		}

		// Store a reference to the original data
		od = node;
		// Clean the original data copy
		odc.clearProperties();
		// Clean the working copy
		wc.clearProperties();

		// If no data is available, we are done
		if (node == null) return;

		// Thread access to the model is limited to the executors thread.
		// Copy the data over to the working copy to ease the access.
		Protocol.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				Map<String, String> properties = node.getPeer().getAttributes();
				for (String key : properties.keySet()) {
					wc.setProperty(key, properties.get(key));
					odc.setProperty(key, properties.get(key));
				}
			}
		});

		// From here on, work with the working copy only!

		if (transportTypeControl != null) {
			String transportType = wc.getStringProperty(IPeer.ATTR_TRANSPORT_NAME);
			if (transportType != null && !"".equals(transportType)) { //$NON-NLS-1$
				transportTypeControl.setSelectedTransportType(transportType);

				if (transportTypePanelControl != null) {
					transportTypePanelControl.showConfigurationPanel(transportType);
					IWizardConfigurationPanel panel = transportTypePanelControl.getConfigurationPanel(transportType);
					if (panel instanceof ISharedDataWizardPage) ((ISharedDataWizardPage)panel).setupData(wc);
				}
			}
		}

		// Adjust the control enablement
		updateEnablement();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.wizards.interfaces.IValidatableWizardPage#validatePage()
	 */
	@Override
	public void validatePage() {
		@SuppressWarnings("unused")
		boolean valid = true;

		if (transportTypeControl != null) {
			valid &= transportTypeControl.isValid();
//			if (transportTypeControl.getMessageType() > getMessageType()) {
//				setMessage(transportTypeControl.getMessage(), transportTypeControl.getMessageType());
//			}
		}

		if (transportTypePanelControl != null) {
			valid &= transportTypePanelControl.isValid();
//			if (transportTypePanelControl.getMessageType() > getMessageType()) {
//				setMessage(transportTypePanelControl.getMessage(), transportTypePanelControl.getMessageType());
//			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#commit(boolean)
	 */
	@Override
	public void commit(boolean onSave) {
		// Remember the current dirty state
		boolean needsSaving = isDirty();
		// Call the super implementation (resets the dirty state)
	    super.commit(onSave);

		// Nothing to do if not on save or saving is not needed
		if (!onSave || !needsSaving) return;
	}

	/**
	 * Called to signal that the data associated has been changed.
	 *
	 * @param e The event which triggered the invocation or <code>null</code>.
	 */
	public void dataChanged(TypedEvent e) {
		boolean isDirty = false;

		if (transportTypeControl != null) {
			String transportType = transportTypeControl.getSelectedTransportType();
			if ("".equals(transportType)) { //$NON-NLS-1$
				String value = odc.getStringProperty(IPeer.ATTR_TRANSPORT_NAME);
				isDirty |= value != null && !"".equals(value.trim()); //$NON-NLS-1$
			} else {
				isDirty |= !odc.isProperty(IPeer.ATTR_TRANSPORT_NAME, transportType);
			}

			if (transportTypePanelControl != null) {
				IWizardConfigurationPanel panel = transportTypePanelControl.getConfigurationPanel(transportType);
				if (panel != null) isDirty |= panel.dataChanged(odc, e);
			}
		}

		// If dirty, mark the form part dirty.
		// Otherwise call refresh() to reset the dirty (and stale) flag
		markDirty(isDirty);
	}

	/**
	 * Updates the control enablement.
	 */
	protected void updateEnablement() {
		// Determine the input
		final Object input = getManagedForm().getInput();

		// Determine if the peer is a static peer
		final AtomicBoolean isStatic = new AtomicBoolean();
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				if (input instanceof IPeerModel) {
					String value = ((IPeerModel)input).getPeer().getAttributes().get("static.transient"); //$NON-NLS-1$
					isStatic.set(value != null && Boolean.parseBoolean(value.trim()));
				}
			}
		};
		if (Protocol.isDispatchThread()) runnable.run();
		else Protocol.invokeAndWait(runnable);

		// The transport type control is enabled for static peers
		if (transportTypeControl != null) {
			SWTControlUtil.setEnabled(transportTypeControl.getEditFieldControl(), isStatic.get());
			if (transportTypePanelControl != null) {
				IWizardConfigurationPanel panel = transportTypePanelControl.getConfigurationPanel(transportTypeControl.getSelectedTransportType());
				if (panel != null) panel.setEnabled(isStatic.get());
			}
		}
	}
}
