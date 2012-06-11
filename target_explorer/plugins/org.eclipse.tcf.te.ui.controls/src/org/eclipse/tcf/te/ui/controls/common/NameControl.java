/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.controls.common;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl;
import org.eclipse.tcf.te.ui.controls.nls.Messages;
import org.eclipse.tcf.te.ui.controls.validator.RegexValidator;
import org.eclipse.tcf.te.ui.controls.validator.Validator;

/**
 * Name control implementation.
 */
public class NameControl extends BaseEditBrowseTextControl {

	/**
	 * Constructor.
	 *
	 * @param parentPage The parent dialog page this control is embedded in.
	 *                   Might be <code>null</code> if the control is not associated with a page.
	 */
	public NameControl(IDialogPage parentPage) {
		super(parentPage);
		setIsGroup(false);
		setHideBrowseButton(true);
		setHasHistory(false);
		setEditFieldLabel(Messages.NameControl_label);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl#doCreateEditFieldValidator()
	 */
	@Override
	protected Validator doCreateEditFieldValidator() {
		return new RegexValidator(Validator.ATTR_MANDATORY, ".*"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl#configureEditFieldValidator(org.eclipse.tcf.te.ui.controls.validator.Validator)
	 */
	@Override
	protected void configureEditFieldValidator(Validator validator) {
		if (validator instanceof RegexValidator) {
			validator.setMessageText(RegexValidator.INFO_MISSING_VALUE, Messages.NameControl_info_missingValue);
		}
	}
}
