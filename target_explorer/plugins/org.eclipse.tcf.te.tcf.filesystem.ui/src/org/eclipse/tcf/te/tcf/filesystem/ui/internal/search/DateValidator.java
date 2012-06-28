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

import java.util.Calendar;
import java.util.StringTokenizer;

import org.eclipse.tcf.te.tcf.filesystem.ui.nls.Messages;
import org.eclipse.tcf.te.ui.controls.validator.RegexValidator;

/**
 * The validator used to validate the date entered in the search dialog. 
 */
public class DateValidator extends RegexValidator {
	// The regex that defines the format of the date, i.e., MM/DD/YYYY
	private static final String DATE_REGEX = "\\d{1,2}/\\d{1,2}/\\d{4}"; //$NON-NLS-1$

	/**
	 * Constructor
	 */
	public DateValidator() {
	    super(ATTR_MANDATORY, DATE_REGEX);
	    setMessageText(INFO_MISSING_VALUE, Messages.DateValidator_InfoPrompt);
	    setMessageText(ERROR_INVALID_VALUE, Messages.DateValidator_InfoFormat);
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.validator.RegexValidator#isValid(java.lang.String)
	 */
	@Override
    public boolean isValid(String newText) {
	    boolean valid = super.isValid(newText);
	    if(valid) {
	    	try {
	    		parseTimeInMillis(newText);
	    		return true;
	    	}
	    	catch(IllegalArgumentException e) {
	    		String error = e.getMessage();
	    		setMessage(error, ERROR);
	    	}
	    }
	    return false;
    }
	
	/**
	 * Parse a text string to a date expressed in milliseconds since 1/1/1970.
	 * If the format is not right, then throw an illegal argument exception containing
	 * the error message.
	 * 
	 * @param newText The text string to be parsed.
	 * @return a number in milliseconds since 1/1/1970
	 * @throws IllegalArgumentException when the format is not right.
	 */
	public static long parseTimeInMillis(String newText) throws IllegalArgumentException{
    	StringTokenizer tokenizer = new StringTokenizer(newText, "/"); //$NON-NLS-1$
    	String month_str = tokenizer.nextToken();
    	int month = 0;
    	try{
    		month = Integer.parseInt(month_str);
    	}
    	catch(NumberFormatException e){
    		throw new IllegalArgumentException(Messages.DateValidator_MonthInvalidNumber);
    	}
    	if(month <= 0 || month > 12) {
    		throw new IllegalArgumentException(Messages.DateValidator_MonthOutofRange);
    	}
    	String date_str = tokenizer.nextToken();
    	int date = 0;
    	try {
    		date = Integer.parseInt(date_str);
    	}
    	catch(NumberFormatException e) {
    		throw new IllegalArgumentException(Messages.DateValidator_DateInvalidNumber);
    	}
    	if(date <= 0 || date > 31) {
    		throw new IllegalArgumentException(Messages.DateValidator_DateOutofRange);
    	}
    	String year_str = tokenizer.nextToken();
    	int year = 0;
    	try {
    		year = Integer.parseInt(year_str);
    	}
    	catch(NumberFormatException e) {
    		throw new IllegalArgumentException(Messages.DateValidator_YearInvalidNumber);
    	}
    	if(year <= 0) {
    		throw new IllegalArgumentException(Messages.DateValidator_YearOutofRange);
    	}
    	Calendar calendar = Calendar.getInstance();
    	calendar.setLenient(false);
    	calendar.set(year, month-1, date);
    	try {
    		return calendar.getTimeInMillis();
    	}
    	catch(IllegalArgumentException e) {
    		throw new IllegalArgumentException(Messages.DateValidator_InvalidDate);
    	}
	}
}
