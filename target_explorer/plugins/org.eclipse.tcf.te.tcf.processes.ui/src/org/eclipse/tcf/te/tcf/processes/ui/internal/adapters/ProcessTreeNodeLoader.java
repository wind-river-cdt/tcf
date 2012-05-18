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

public class ProcessTreeNodeLoader implements ILazyLoader {
	private ProcessTreeNode node;
	public ProcessTreeNodeLoader(ProcessTreeNode node) {
		this.node = node;
    }

	@Override
	public boolean isDataLoaded() {
		return node.childrenQueried;
	}

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
