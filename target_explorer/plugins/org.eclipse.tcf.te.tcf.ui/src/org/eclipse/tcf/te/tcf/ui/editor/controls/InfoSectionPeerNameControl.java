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
import org.eclipse.tcf.te.tcf.ui.controls.PeerNameControl;
import org.eclipse.tcf.te.tcf.ui.editor.sections.GeneralInformationSection;
import org.eclipse.tcf.te.ui.jface.interfaces.IValidatingContainer;

/**
 * General information section peer name control implementation.
 */
public class InfoSectionPeerNameControl extends PeerNameControl {
	// Reference to the parent general information section
	private final GeneralInformationSection infoSection;

	/**
	 * Constructor.
	 *
	 * @param infoSection The parent general information section. Must not be <code>null</code>.
	 */
	public InfoSectionPeerNameControl(GeneralInformationSection infoSection) {
		super(null);

		Assert.isNotNull(infoSection);
		this.infoSection = infoSection;
	}

    /* (non-Javadoc)
     * @see org.eclipse.tcf.te.ui.controls.BaseDialogPageControl#getValidatingContainer()
     */
    @Override
    public IValidatingContainer getValidatingContainer() {
		Object container = infoSection.getManagedForm().getContainer();
		return container instanceof IValidatingContainer ? (IValidatingContainer)container : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	@Override
	public void modifyText(ModifyEvent e) {
		super.modifyText(e);
		infoSection.dataChanged(e);
	}
}
