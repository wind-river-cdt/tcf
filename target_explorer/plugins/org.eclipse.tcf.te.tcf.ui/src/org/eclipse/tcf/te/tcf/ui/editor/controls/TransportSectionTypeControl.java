/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.editor.controls;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.tcf.te.tcf.ui.controls.TransportTypeControl;
import org.eclipse.tcf.te.tcf.ui.editor.sections.TransportSection;
import org.eclipse.tcf.te.ui.jface.interfaces.IValidatingContainer;

/**
 * Transport section transport type control implementation.
 */
public class TransportSectionTypeControl extends TransportTypeControl {
	// Reference to the parent transport section
	private final TransportSection transportSection;

	/**
	 * Constructor.
	 *
	 * @param transportSection The parent transport section. Must not be <code>null</code>.
	 */
	public TransportSectionTypeControl(TransportSection transportSection) {
		super(null);

		Assert.isNotNull(transportSection);
		this.transportSection = transportSection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
		TransportSectionTypePanelControl transportTypePanelControl = (TransportSectionTypePanelControl)transportSection.getAdapter(TransportSectionTypePanelControl.class);

		if (transportTypePanelControl != null) {
			transportTypePanelControl.showConfigurationPanel(getSelectedTransportType());

			IValidatingContainer validatingContainer = getValidatingContainer();
			if (validatingContainer != null) validatingContainer.validate();
		}
	}

    /* (non-Javadoc)
     * @see org.eclipse.tcf.te.ui.controls.BaseDialogPageControl#getValidatingContainer()
     */
    @Override
    public IValidatingContainer getValidatingContainer() {
		Object container = transportSection.getManagedForm().getContainer();
		return container instanceof IValidatingContainer ? (IValidatingContainer)container : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	@Override
	public void modifyText(ModifyEvent e) {
		super.modifyText(e);
		transportSection.dataChanged(e);
	}
}
