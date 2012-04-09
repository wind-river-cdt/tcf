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
}
