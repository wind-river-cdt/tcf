/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.interfaces;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * The interface to define a loader for certain tree nodes which
 * are lazily loaded when they are expanded in the tree.
 */
public interface ILazyLoader {
	
	/**
	 * If this tree node is a leaf. 
	 * 
	 * @return true if it is.
	 */
	public boolean isLeaf();

	/**
	 * If the data of the tree node has been loaded.
	 * 
	 * @return true if it is already loaded or else false.
	 */
	public boolean isDataLoaded();
	
	/**
	 * Load the data of the tree node including its children.
     * A request to cancel the operation should be honored and acknowledged 
     * by throwing <code>InterruptedException</code>.
	 * 
	 * @param monitor The monitor to report the progress. Must not be null.
     * @exception InvocationTargetException if the run method must propagate a checked exception,
     * 	it should wrap it inside an <code>InvocationTargetException</code>; runtime exceptions are automatically
     *  wrapped in an <code>InvocationTargetException</code> by the calling context
     * @exception InterruptedException if the operation detects a request to cancel, 
     *  using <code>IProgressMonitor.isCanceled()</code>, it should exit by throwing 
     *  <code>InterruptedException</code>
	 */
	public void loadData(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException;
}
