/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.adapters;

import org.eclipse.tcf.te.tcf.filesystem.ui.nls.Messages;
import org.eclipse.tcf.te.ui.controls.validator.Validator;

/**
 * The validator used to validate the name entered in the search dialog. 
 */
public class NameValidator extends Validator {
	/**
	 * Constructor
	 */
	public NameValidator() {
	    super(ATTR_MANDATORY);
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.validator.Validator#isValid(java.lang.String)
	 */
	@Override
	public boolean isValid(String newText) {
		init();
		boolean valid = newText != null && newText.trim().length() > 0;
		if(!valid) {
			if (isAttribute(ATTR_MANDATORY)) {
				setMessage(Messages.NameValidator_InfoPrompt, INFORMATION);
			}
		}
		return valid;
	}
}
