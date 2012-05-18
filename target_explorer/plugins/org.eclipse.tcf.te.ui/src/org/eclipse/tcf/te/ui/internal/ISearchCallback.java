/*
 * ISearchCallback.java
 * Created on Feb 15, 2011
 *
 * Copyright 2008 Wind River Systems Inc. All rights reserved.
 */
package org.eclipse.tcf.te.ui.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.TreePath;

/**
 * This is callback handler invoked search processes to report searching results
 * or progress, including methods like @link ExecutionContextViewer#searchNext}, 
 * {@link ExecutionContextViewer#getExpensivePath},and {@link 
 * ExecutionContextViewer#getCheapPath}.
 * 
 * @author william.chen@windriver.com
 *
 */
public interface ISearchCallback {
	/**
	 * Callback handler method called when a search target found or a progress is
	 * made in the searching process.
	 *  
	 * @param status
	 * 				The resulting status of the searching.
	 * @param path
	 * 				The searching result or current searching position.
	 */
	void callback(IStatus status, TreePath path);
}
