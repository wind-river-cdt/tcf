/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.editor;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.te.tcf.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.ui.editor.sections.AttributesSection;
import org.eclipse.tcf.te.tcf.ui.editor.sections.GeneralInformationSection;
import org.eclipse.tcf.te.tcf.ui.editor.sections.ServicesSection;
import org.eclipse.tcf.te.tcf.ui.editor.sections.TransportSection;
import org.eclipse.tcf.te.tcf.ui.help.IContextHelpIds;
import org.eclipse.tcf.te.tcf.ui.internal.ImageConsts;
import org.eclipse.tcf.te.tcf.ui.nls.Messages;
import org.eclipse.tcf.te.ui.forms.CustomFormToolkit;
import org.eclipse.tcf.te.ui.jface.interfaces.IValidatingContainer;
import org.eclipse.tcf.te.ui.views.editor.pages.AbstractCustomFormToolkitEditorPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * Peer overview page implementation.
 */
public class OverviewEditorPage extends AbstractCustomFormToolkitEditorPage implements IValidatingContainer {
	// References to the page sub sections
	private GeneralInformationSection infoSection;
	private TransportSection transportSection;
	private ServicesSection servicesSection;
	private AttributesSection attributesSection;

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractCustomFormToolkitEditorPage#dispose()
	 */
	@Override
	public void dispose() {
		if (infoSection != null) { infoSection.dispose(); infoSection = null; }
		if (transportSection != null) { transportSection.dispose(); transportSection = null; }
		if (servicesSection != null) { servicesSection.dispose(); servicesSection = null; }
		if (attributesSection != null) { attributesSection.dispose(); attributesSection = null; }
	    super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractCustomFormToolkitEditorPage#getContextHelpId()
	 */
	@Override
	protected String getContextHelpId() {
	    return IContextHelpIds.OVERVIEW_EDITOR_PAGE;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractCustomFormToolkitEditorPage#getFormTitle()
	 */
	@Override
    protected String getFormTitle() {
		return Messages.OverviewEditorPage_title;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractCustomFormToolkitEditorPage#getFormImage()
	 */
	@Override
	protected Image getFormImage() {
	    return UIPlugin.getImage(ImageConsts.PEER);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractCustomFormToolkitEditorPage#doCreateFormContent(org.eclipse.swt.widgets.Composite, org.eclipse.tcf.te.ui.forms.CustomFormToolkit)
	 */
	@Override
    protected void doCreateFormContent(Composite parent, CustomFormToolkit toolkit) {
		Assert.isNotNull(parent);
		Assert.isNotNull(toolkit);

		// Setup the main panel (using the table wrap layout)
		Composite panel = toolkit.getFormToolkit().createComposite(parent);
		TableWrapLayout layout = new TableWrapLayout();
		layout.makeColumnsEqualWidth = true;
		layout.numColumns = 2;
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		infoSection = new GeneralInformationSection(getManagedForm(), panel);
		infoSection.getSection().setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));
		getManagedForm().addPart(infoSection);

		transportSection = new TransportSection(getManagedForm(), (Composite)infoSection.getSection().getClient());
		((GridData)transportSection.getSection().getLayoutData()).horizontalSpan = 2;
		getManagedForm().addPart(transportSection);

		servicesSection = new ServicesSection(getManagedForm(), panel);
		TableWrapData layoutData = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP);
		servicesSection.getSection().setLayoutData(layoutData);
		getManagedForm().addPart(servicesSection);

		attributesSection = new AttributesSection(getManagedForm(), panel);
		layoutData = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB);
		layoutData.colspan = 2;
		attributesSection.getSection().setLayoutData(layoutData);
		getManagedForm().addPart(attributesSection);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormPage#setActive(boolean)
	 */
	@Override
	public void setActive(boolean active) {
	    super.setActive(active);
	    if (infoSection != null) infoSection.setActive(active);
	    if (transportSection != null) transportSection.setActive(active);
	    if (servicesSection != null) servicesSection.setActive(active);
	    if (attributesSection != null) attributesSection.setActive(active);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.jface.interfaces.IValidatingContainer#validate()
	 */
	@Override
	public void validate() {
		// Get the scrolled form
		ScrolledForm form = getManagedForm().getForm();

		String message = null;
		int messageType = IMessageProvider.NONE;

		if (infoSection != null) {
			infoSection.isValid();
			if (infoSection.getMessageType() > messageType) {
				message = infoSection.getMessage();
				messageType = infoSection.getMessageType();
			}
		}

		if (transportSection != null) {
			transportSection.isValid();
			if (transportSection.getMessageType() > messageType) {
				message = transportSection.getMessage();
				messageType = transportSection.getMessageType();
			}
		}

		if (servicesSection != null) {
			servicesSection.isValid();
			if (servicesSection.getMessageType() > messageType) {
				message = servicesSection.getMessage();
				messageType = servicesSection.getMessageType();
			}
		}

		if (attributesSection != null) {
			attributesSection.isValid();
			if (attributesSection.getMessageType() > messageType) {
				message = attributesSection.getMessage();
				messageType = attributesSection.getMessageType();
			}
		}

		// Apply the message to the form
		form.setMessage(message, messageType);
	}
}
