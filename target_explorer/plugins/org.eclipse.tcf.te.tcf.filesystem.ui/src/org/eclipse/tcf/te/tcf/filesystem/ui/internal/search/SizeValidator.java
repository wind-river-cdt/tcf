/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.search;

import org.eclipse.tcf.te.tcf.filesystem.ui.nls.Messages;
import org.eclipse.tcf.te.ui.controls.validator.NumberValidator;
import org.eclipse.tcf.te.ui.controls.validator.Validator;

/**
 * The validator used to validate the size entered in the search dialog. 
 */
public class SizeValidator extends NumberValidator {
	/**
	 * Constructor
	 */
	public SizeValidator() {
		super(Validator.ATTR_MANDATORY, 0, -1);
		setMessageText(INFO_MISSING_VALUE, Messages.SizeValidator_InfoPrompt);
		setMessageText(ERROR_INVALID_VALUE, Messages.SizeValidator_ErrorIncorrectFormat);
		setMessageText(ERROR_INVALID_RANGE, Messages.SizeValidator_ErrorSizeOutofRange);
	}
}
