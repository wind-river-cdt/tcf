/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.core.interfaces;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A class that implement this interface represents an file system operation,
 * which is an abstract of the action operated over files/folders. 
 * 
 * @since Target Explorer 1.0.0
 */
public interface IOperation {
    /**
     * Runs this operation.  Progress should be reported to the given progress monitor.
     * A request to cancel the operation should be honored and acknowledged 
     * by throwing <code>InterruptedException</code>.
     *
     * @param monitor the progress monitor to use to display progress and receive
     *   requests for cancelation
     * @exception InvocationTargetException if the run method must propagate a checked exception,
     * 	it should wrap it inside an <code>InvocationTargetException</code>; runtime exceptions are automatically
     *  wrapped in an <code>InvocationTargetException</code> by the calling context
     * @exception InterruptedException if the operation detects a request to cancel, 
     *  using <code>IProgressMonitor.isCanceled()</code>, it should exit by throwing 
     *  <code>InterruptedException</code>
     *
     */
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException;
    
    /**
     * Get the operation's name. This name will be used as the task name of
     * the given monitor.
     * 
     * @see IProgressMonitor#beginTask(String, int)
     * @return The name of the operation.
     */
    public String getName();
    
    /**
     * Get the total amount of work which will used by the progress 
     * monitor to set the total work.
     * 
     * @see IProgressMonitor#beginTask(String, int)
     * @return The total amount of work or UNKNOWN if it is in-determinant
     */
    public int getTotalWork();
}
