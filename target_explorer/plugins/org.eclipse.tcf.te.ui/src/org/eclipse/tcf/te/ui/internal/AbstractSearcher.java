/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.internal;

import java.lang.reflect.Method;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.te.ui.interfaces.ILazyLoader;
import org.eclipse.tcf.te.ui.interfaces.ISearchMatcher;
import org.eclipse.tcf.te.ui.interfaces.ITreeSearcher;
import org.eclipse.tcf.te.ui.trees.Pending;
import org.eclipse.ui.PlatformUI;

/**
 * The abstract implementation of ITreeSearcher which provides common utility methods
 * for traversing.
 */
public abstract class AbstractSearcher implements ITreeSearcher {
	// The method to access AbstractTreeViewer#getSortedChildren in order to the children visually on the tree.
	static Method methodGetSortedChildren;
	static {
		SafeRunner.run(new ISafeRunnable(){
			@Override
            public void handleException(Throwable exception) {
	            // Ignore on purpose.
            }
			@Override
            public void run() throws Exception {
				// Initialize the method object.
		        methodGetSortedChildren = AbstractTreeViewer.class.
		        				getDeclaredMethod("getSortedChildren", new Class[]{Object.class}); //$NON-NLS-1$
		        // Because "getSortedChildren" is a protected method, we need to make it accessible.
		        methodGetSortedChildren.setAccessible(true);
            }});
	}
	// The tree viewer to be searched.
	protected TreeViewer fViewer;
	// The label provider of the tree viewer.
	protected ILabelProvider fLabelProvider;
	// The matcher used to match eacho tree nodes.
	protected ISearchMatcher fMatcher;
	
	/**
	 * Create a searcher with the specified viewer and matcher.
	 * 
	 * @param viewer The tree viewer to be searched.
	 * @param matcher The matcher used to match tree nodes.
	 */
	protected AbstractSearcher(TreeViewer viewer, ISearchMatcher matcher) {
		fViewer = viewer;
		fLabelProvider = (ILabelProvider) fViewer.getLabelProvider();
		fMatcher = matcher;
	}
	
	/**
	 * Get the text representation of a element using the label provider
	 * of the tree viewer. 
	 * Note: this method could be called at any thread.
	 * 
	 * @param element The element.
	 * @return The text representation.
	 */
	protected String getElementText(final Object element) {
		if (Display.getCurrent() != null) {
			if(element == fViewer.getInput()) return "the root"; //$NON-NLS-1$
			if (fLabelProvider != null) {
				return fLabelProvider.getText(element);
			}
			return element == null ? "" : element.toString(); //$NON-NLS-1$
		}
		final String[] result = new String[1];
		fViewer.getTree().getDisplay().syncExec(new Runnable() {
        	@Override
        	public void run() {
        		result[0] = getElementText(element);
        	}
        });
		return result[0];
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
			if (!lazyLoader.isDataLoaded()) {
				try{
					lazyLoader.loadData(monitor);
				}catch(Exception e) {
					return new Object[0];
				}
			}
		}
		Object[] children = getSortedChildren(parent);
		return children;
	}

	/**
	 * Get the current visible/sorted children under the specified parent element or path
	 * by invoking the reflective method. This method is UI thread-safe.
	 * 
	 * @param parentElementOrTreePath The parent element or path.
	 * @return The current visible/sorted children of the parent path/element.
	 */
	protected Object[] getSortedChildren(final Object parentElementOrTreePath) {
		if (Display.getCurrent() != null) {
			try {
				if (methodGetSortedChildren != null) {
					return (Object[]) methodGetSortedChildren
					                .invoke(fViewer, parentElementOrTreePath);
				}
			}
			catch (Exception e) {
			}
			return new Object[0];
		}
		final Object[][] result = new Object[1][];
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				result[0] = getSortedChildren(parentElementOrTreePath);
			}
		});
		return result[0];
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
