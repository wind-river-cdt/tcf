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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tcf.te.ui.interfaces.IViewerInput;

/**
 * The base tree content provider that defines several default methods.
 */
public abstract class TreeContentProvider implements ITreeContentProvider {

	/**
	 * Static reference to the return value representing no elements.
	 */
	protected final static Object[] NO_ELEMENTS = new Object[0];

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
	
	/**
	 * Install a property change listener to the specified element.
	 * 
	 * @param element The element node.
	 */
	protected void installPropertyChangeListener(Object element) {
	}

	/***
	 * Get the viewer input from the an element of the tree viewer.
	 * If the element is an instance of IViewerInput, then return
	 * the element. If the element can be adapted to a IViewerInput,
	 * then return the adapted object.
	 * 
	 * @param element The given element to get a viewer input from.
	 * @return A viewer input or null.
	 */
	protected IViewerInput getViewerInput(Object element) {
		IViewerInput viewerInput = null;
		if (element != null) {
			if (element instanceof IViewerInput) {
				viewerInput = (IViewerInput) element;
			}
			else {
				if (element instanceof IAdaptable) {
					viewerInput = (IViewerInput) ((IAdaptable) element).getAdapter(IViewerInput.class);
				}
				if (viewerInput == null) {
					viewerInput = (IViewerInput) Platform.getAdapterManager().getAdapter(element, IViewerInput.class);
				}
			}
		}
		return viewerInput;
    }

	/**
	 * If the root node of the tree is visible.
	 * 
	 * @return true if it is visible.
	 */
	protected boolean isRootNodeVisible() {
		return false;
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
