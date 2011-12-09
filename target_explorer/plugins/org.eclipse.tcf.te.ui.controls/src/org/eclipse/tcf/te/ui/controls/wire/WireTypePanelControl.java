/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.controls.wire;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.tcf.te.ui.controls.BaseWizardConfigurationPanelControl;

/**
 * Custom wire type wizard panel control.
 */
public class WireTypePanelControl extends BaseWizardConfigurationPanelControl {

	/**
	 * Constructor.
	 *
	 * @param parentPage The parent dialog page this control is embedded in.
	 *                   Might be <code>null</code> if the control is not associated with a page.
	 */
	public WireTypePanelControl(IDialogPage parentPage) {
		super(parentPage);
	}

}