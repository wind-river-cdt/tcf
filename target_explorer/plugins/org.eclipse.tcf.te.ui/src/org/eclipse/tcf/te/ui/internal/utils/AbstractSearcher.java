/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.internal.utils;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.tcf.te.ui.interfaces.ILazyLoader;
import org.eclipse.tcf.te.ui.interfaces.ISearchable;
import org.eclipse.tcf.te.ui.interfaces.ITreeSearcher;
import org.eclipse.tcf.te.ui.trees.Pending;
import org.eclipse.tcf.te.ui.utils.TreeViewerUtil;

/**
 * The abstract implementation of ITreeSearcher which provides common utility methods
 * for traversing.
 */
public abstract class AbstractSearcher implements ITreeSearcher {
	// The tree viewer to be searched.
	protected TreeViewer fViewer;
	// The label provider of the tree viewer.
	protected ILabelProvider fLabelProvider;
	// The searchable element.
	protected ISearchable fSearchable;

	/**
	 * Create a searcher with the specified viewer and matcher.
	 *
	 * @param viewer The tree viewer to be searched.
	 * @param searchable The matcher used to match tree nodes.
	 */
	protected AbstractSearcher(TreeViewer viewer, ISearchable searchable) {
		fViewer = viewer;
		fLabelProvider = (ILabelProvider) fViewer.getLabelProvider();
		this.fSearchable = searchable;
	}

	/**
	 * Update the children of the specified parent. If the data of the parent
	 * is lazily loaded and not loaded yet, then load the data first, before getting
	 * the children.
	 *
	 * @param parent The parent node to get the updated children from.
	 * @param monitor The progress monitor used while loading data.
	 * @return The updated children of the parent node.
	 */
	protected Object[] getUpdatedChildren(final Object parent, final IProgressMonitor monitor) {
		if (parent instanceof Pending) return new Object[0];
		final ILazyLoader lazyLoader = getLazyLoader(parent);
		if (lazyLoader != null) {
			if(lazyLoader.isLeaf()) {
				return new Object[0];
			}
			if (!lazyLoader.isDataLoaded()) {
				try{
					lazyLoader.loadData(monitor);
				}catch(Exception e) {
					return new Object[0];
				}
			}
		}
		Object[] children = TreeViewerUtil.getSortedChildren(fViewer, parent);
		return children;
	}

	/**
	 * Get a lazy loader from the specified element if it could be
	 * adapted to a lazy loader.
	 *
	 * @param element The element to get the lazy loader from.
	 * @return A lazy loader or null if it is not adapted to a lazy loader.
	 */
	private ILazyLoader getLazyLoader(Object element) {
		ILazyLoader loader = null;
		if(element instanceof ILazyLoader) {
			loader = (ILazyLoader) element;
		}
		if(loader == null && element instanceof IAdaptable) {
			loader = (ILazyLoader)((IAdaptable)element).getAdapter(ILazyLoader.class);
		}
		if(loader == null) {
			loader = (ILazyLoader) Platform.getAdapterManager().getAdapter(element, ILazyLoader.class);
		}
	    return loader;
    }
}
