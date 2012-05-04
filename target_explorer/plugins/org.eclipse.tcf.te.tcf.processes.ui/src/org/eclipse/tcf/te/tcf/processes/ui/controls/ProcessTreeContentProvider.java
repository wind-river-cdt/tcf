/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.controls;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.processes.core.model.ProcessModel;


/**
 * Process tree control content provider implementation.
 */
public class ProcessTreeContentProvider extends ProcessNavigatorContentProvider {
	// The target's peer model.
	private IPeerModel peerModel;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.TreeContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	    super.inputChanged(viewer, oldInput, newInput);
	    if(newInput instanceof IPeerModel) {
	    	peerModel = (IPeerModel) newInput;
	    }
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.TreeContentProvider#dispose()
	 */
	@Override
    public void dispose() {
	    super.dispose();
	    if(peerModel != null) {
	    	ProcessModel model = ProcessModel.getProcessModel(peerModel);
	    	if(!model.isRefreshStopped()) {
	    		// If the model is auto refreshing, then stop it when the editor is disposed.
	    		model.setInterval(0);
	    	}
	    }
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.processes.ui.controls.ProcessNavigatorContentProvider#isRootNodeVisible()
	 */
	@Override
    protected boolean isRootNodeVisible() {
	    return false;
    }
}
