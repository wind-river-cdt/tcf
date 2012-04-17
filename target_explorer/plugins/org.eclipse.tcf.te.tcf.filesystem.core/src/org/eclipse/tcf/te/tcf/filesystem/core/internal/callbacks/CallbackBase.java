/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.core.internal.callbacks;

import org.eclipse.tcf.protocol.IErrorReport;
/**
 * The base class for all callback classes.
 */
public class CallbackBase {

	/**
	 * Get the error message from the throwable error.
	 * 
	 * @param error The throwable error.
	 * @return The error message.
	 */
	protected String getErrorMessage(Throwable error) {
	    String message = null;
	    if(error instanceof IErrorReport) {
	    	IErrorReport report = (IErrorReport) error;
	    	message = (String)report.getAttributes().get(IErrorReport.ERROR_FORMAT);
	    }
	    else {
	    	message = error.getMessage();
	    }
	    return message;
    }
}
