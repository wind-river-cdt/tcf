/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.trees;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.tcf.te.core.interfaces.IPropertyChangeProvider;
import org.eclipse.tcf.te.core.interfaces.IViewerInput;

/**
 * The base tree content provider that defines several default methods.
 */
public abstract class TreeContentProvider implements ITreeContentProvider, PropertyChangeListener {

	/**
	 * Static reference to the return value representing no elements.
	 */
	protected final static Object[] NO_ELEMENTS = new Object[0];

	// The listener to refresh the common viewer when properties change.
	protected CommonViewerListener commonViewerListener;
	// The viewer inputs that have been added a property change listener.
	private Set<IPropertyChangeProvider> providers = Collections.synchronizedSet(new HashSet<IPropertyChangeProvider>());
	// The viewer
	protected TreeViewer viewer;
	// The pending nodes and their direct parents.
	private Map<Object, Pending> pendings;

	/**
	 * Create a tree content provider.
	 */
	public TreeContentProvider() {
		pendings = new HashMap<Object, Pending>();
	}

	/**
	 * Get the pending node for the specified parent.
	 * If it exists, then return it. If not, create one
	 * and save it and return it.
	 */
	protected Pending getPending(Object parent) {
		Pending pending = pendings.get(parent);
		if(pending == null && viewer != null) {
			pending = new Pending(viewer);
			pendings.put(parent, pending);
		}
		return pending;
	}

	/*
	 * (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String property = evt.getPropertyName();
		if(property.equals("query_started")) { //$NON-NLS-1$
			Object source = evt.getSource();
			Pending pending = pendings.get(source);
			if(pending != null) {
				pending.startAnimation();
			}
		}
		else if(property.equals("query_done")) { //$NON-NLS-1$
			Object source = evt.getSource();
			Pending pending = pendings.remove(source);
			if(pending != null) {
				pending.stopAnimation();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
		for(IPropertyChangeProvider provider : providers) {
			provider.removePropertyChangeListener(commonViewerListener);
			provider.removePropertyChangeListener(this);
		}
		commonViewerListener.cancel();
		providers.clear();
		for(Pending pending : pendings.values()) {
			pending.stopAnimation();
		}
		pendings.clear();
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
		if (viewer != null) {
			ViewerFilter[] filters = viewer.getFilters();
			if (filters != null) {
				for (ViewerFilter filter : filters) {
					Object[] filteredResult = filter.filter(viewer, parent, result);
					result = filteredResult;
				}
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		Assert.isNotNull(parentElement);

		if (parentElement instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) parentElement;
			IViewerInput viewerInput = (IViewerInput) adaptable.getAdapter(IViewerInput.class);
			if (viewerInput != null) {
				installPropertyChangeListener(viewerInput);
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		Assert.isTrue(viewer instanceof TreeViewer);
		this.viewer = (TreeViewer) viewer;
		this.commonViewerListener = new CommonViewerListener(this.viewer, this);
	}

	/**
	 * Install a property change listener to the specified element.
	 * 
	 * @param provider The element node.
	 */
	private void installPropertyChangeListener(IPropertyChangeProvider provider) {
		if(provider != null && !providers.contains(provider) && commonViewerListener != null) {
			provider.addPropertyChangeListener(commonViewerListener);
			provider.addPropertyChangeListener(this);
			providers.add(provider);
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
