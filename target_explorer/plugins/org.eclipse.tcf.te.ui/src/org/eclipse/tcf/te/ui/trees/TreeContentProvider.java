/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.trees;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.tcf.te.ui.interfaces.IViewerInput;

/**
 * The base tree content provider that defines several default methods.
 */
public abstract class TreeContentProvider implements ITreeContentProvider {

	/**
	 * Static reference to the return value representing no elements.
	 */
	protected final static Object[] NO_ELEMENTS = new Object[0];

	// The listener to refresh the common viewer when properties change.
	private CommonViewerListener commonViewerListener;
	// The viewer inputs that have been added a property change listener.
	private Set<IViewerInput> viewerInputs = Collections.synchronizedSet(new HashSet<IViewerInput>());
	// The viewer
	protected TreeViewer viewer;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
    public void dispose() {
		for(IViewerInput viewerInput : viewerInputs) {
			viewerInput.removePropertyChangeListener(commonViewerListener);
		}
		commonViewerListener.cancel();
    }
	
	/**
	 * Get the filtered children of the parent using the
	 * filters registered in the viewer.
	 * 
	 * @param parent The parent element.
	 * @return The children after filtering.
	 */
	private Object[] getFilteredChildren(Object parent) {
		Object[] result = getChildren(parent);
		ViewerFilter[] filters = viewer.getFilters();
		if (filters != null) {
			for (ViewerFilter filter : filters) {
				Object[] filteredResult = filter.filter(viewer, parent, result);
				result = filteredResult;
			}
		}
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		Assert.isTrue(viewer instanceof TreeViewer);
		this.viewer = (TreeViewer) viewer; 
		commonViewerListener = new CommonViewerListener(this.viewer) {
			@Override
            protected Object getParent(Object node) {
	            return TreeContentProvider.this.getParent(node);
            }};
	}
	
	/**
	 * Install a property change listener to the specified element.
	 * 
	 * @param element The element node.
	 */
    protected void installPropertyChangeListener(Object element) {
		IViewerInput viewerInput = ViewerStateManager.getViewerInput(element);
		if(viewerInput != null && !viewerInputs.contains(viewerInput)) {
			viewerInput.addPropertyChangeListener(commonViewerListener);
			viewerInputs.add(viewerInput);
		}
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
	@Override
	public boolean hasChildren(Object element) {
		Object[] children = getFilteredChildren(element);
		return children != null && children.length > 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}
}
