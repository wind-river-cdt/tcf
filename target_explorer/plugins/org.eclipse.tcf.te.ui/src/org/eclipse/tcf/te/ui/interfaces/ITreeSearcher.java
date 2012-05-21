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
import org.eclipse.jface.viewers.TreePath;

/**
 * The interface to define a search engine for the tree viewer.
 */
public interface ITreeSearcher {

	/**
	 * Set the path to start searching from.
	 * 
	 * @param path The path from which to start searching.
	 */
	public void setStartPath(TreePath path);
	
	/**
	 * Search the viewer for the next target which matches the condition defined by the matcher. 
     * A request to cancel the operation should be honored and acknowledged 
     * by throwing <code>InterruptedException</code>.
     * 
     * @param monitor the progress monitor used to report searching progress, must not be null.
     * @exception InvocationTargetException if the run method must propagate a checked exception,
     * 	it should wrap it inside an <code>InvocationTargetException</code>; runtime exceptions are automatically
     *  wrapped in an <code>InvocationTargetException</code> by the calling context
     * @exception InterruptedException if the operation detects a request to cancel, 
     *  using <code>IProgressMonitor.isCanceled()</code>, it should exit by throwing 
     *  <code>InterruptedException</code>
	 */
	public TreePath searchNext(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException;
}
