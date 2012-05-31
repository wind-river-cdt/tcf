/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.internal.adapters;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.tcf.te.runtime.callback.Callback;
import org.eclipse.tcf.te.tcf.core.concurrent.Rendezvous;
import org.eclipse.tcf.te.tcf.processes.core.model.ProcessTreeNode;
import org.eclipse.tcf.te.ui.interfaces.ILazyLoader;

/**
 * The implementation of ILazyLoader for ProcessTreeNode check its data availability
 * and load its children if not ready.
 */
public class ProcessTreeNodeLoader implements ILazyLoader {
	// The node to be checked.
	private ProcessTreeNode node;
	
	/**
	 * Constructor
	 * 
	 * @param node the process node.
	 */
	public ProcessTreeNodeLoader(ProcessTreeNode node) {
		this.node = node;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ILazyLoader#isDataLoaded()
	 */
	@Override
	public boolean isDataLoaded() {
		return node.childrenQueried;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ILazyLoader#loadData(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void loadData(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		final Rendezvous rendezvous = new Rendezvous();
		node.queryChildren(new Callback(){
			@Override
            protected void internalDone(Object caller, IStatus status) {
				rendezvous.arrive();
            }
		});
		try {
	        rendezvous.waiting(10000L);
        }
        catch (TimeoutException e) {
        	throw new InvocationTargetException(e);
        }
	}
}
