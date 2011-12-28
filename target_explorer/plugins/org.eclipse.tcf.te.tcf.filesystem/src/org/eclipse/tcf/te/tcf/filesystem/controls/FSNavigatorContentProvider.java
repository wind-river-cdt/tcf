/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.controls;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tcf.te.ui.interfaces.IViewerInput;
import org.eclipse.tcf.te.ui.trees.CommonViewerListener;
import org.eclipse.tcf.te.ui.trees.ViewerStateManager;

/**
 * File system content provider for the common navigator of Target Explorer.
 */
public class FSNavigatorContentProvider extends FSTreeContentProvider {

	// The listener to refresh the common viewer when properties change.
	private IPropertyChangeListener commonViewerListener;
	// The viewer inputs that have been added a property change listener.
	private Set<IViewerInput> viewerInputs;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.TreeContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		Assert.isTrue(viewer instanceof TreeViewer);
		commonViewerListener = new CommonViewerListener((TreeViewer) viewer);
		viewerInputs = Collections.synchronizedSet(new HashSet<IViewerInput>());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.TreeContentProvider#dispose()
	 */
	@Override
    public void dispose() {
		for(IViewerInput viewerInput : viewerInputs) {
			viewerInput.removePropertyChangeListener(commonViewerListener);
		}
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.TreeContentProvider#isRootNodeVisible()
	 */
	@Override
	protected boolean isRootNodeVisible() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.TreeContentProvider#installPropertyListener(java.lang.Object)
	 */
	@Override
    protected void installPropertyChangeListener(Object element) {
		IViewerInput viewerInput = ViewerStateManager.getViewerInput(element);
		if(viewerInput != null && !viewerInputs.contains(viewerInput)) {
			viewerInput.addPropertyChangeListener(commonViewerListener);
			viewerInputs.add(viewerInput);
		}
    }
}