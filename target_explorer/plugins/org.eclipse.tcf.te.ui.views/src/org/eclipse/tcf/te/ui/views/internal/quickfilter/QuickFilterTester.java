/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.internal.quickfilter;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.tcf.te.core.interfaces.IFilterable;
import org.eclipse.tcf.te.ui.utils.TreeViewerUtil;

/**
 * The property tester to test if the current tree viewer is filterable/filtering. 
 */
public class QuickFilterTester extends PropertyTester {

	public QuickFilterTester() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if(receiver instanceof TreeViewer) {
			TreeViewer viewer = (TreeViewer) receiver;
			if(property.equals("isFilterable")) { //$NON-NLS-1$
				return isFilterable(viewer);
			}
			if(property.equals("isFiltering")) { //$NON-NLS-1$
				return TreeViewerUtil.isFiltering(viewer);
			}
		}
		return false;
	}

	/**
	 * If the current the tree viewer is filterable.
	 * 
	 * @param viewer The tree viewer.
	 * @return true if it is filterable.
	 */
	private boolean isFilterable(TreeViewer viewer) {
	    ISelection obj = viewer.getSelection();
	    if(!obj.isEmpty() && obj instanceof TreeSelection) {
	    	TreeSelection selection = (TreeSelection) obj;
	    	if(selection.size() == 1) {
	    		TreePath[] paths = selection.getPaths();
	    		TreePath path = paths[0];
	    		Object element = path.getLastSegment();
	    		if(element != null && adaptFilterable(element) != null) {
	    			return isEligible(path, viewer);
	    		}
	    	}
	    }
	    return false;
    }
	
	/**
	 * Test if the root for the tree viewer is eligible as a root path 
	 * of a quick filter.
	 * 
	 * @param root The root path to be tested.
	 * @param viewer The tree viewer to be filtered.
	 * @return true if it is eligible as a root path or else false.
	 */
	private boolean isEligible(TreePath root, TreeViewer viewer) {
		if (viewer.getExpandedState(root)) {
			return hasChildren(root, viewer);
		}
		return false;
	}
	
	/**
	 * If the root path has children visible.
	 * 
	 * @param root The root path.
	 * @param viewer The tree viewer.
	 * @return true if it has children visible.
	 */
	private boolean hasChildren(TreePath root, TreeViewer viewer) {
		ITreeContentProvider contentProvider = (ITreeContentProvider) viewer.getContentProvider();
		Object rootElement = root.getLastSegment();
		Object[] children = contentProvider.getChildren(rootElement);
		if (children != null && children.length > 0) {
			ViewerFilter[] filters = viewer.getFilters();
			if (filters != null && filters.length > 0) {
				for (ViewerFilter filter : filters) {
					children = filter.filter(viewer, rootElement, children);
					if (children == null || children.length == 0) break;
				}
			}
			return children != null && children.length > 0;
		}
		return false;
	}
	
	private IFilterable adaptFilterable(Object element) {
		IFilterable decorator = null;
		if(element instanceof IFilterable) {
			decorator = (IFilterable) element;
		}
		if(decorator == null && element instanceof IAdaptable) {
			decorator = (IFilterable) ((IAdaptable)element).getAdapter(IFilterable.class);
		}
		if(decorator == null) {
			decorator = (IFilterable) Platform.getAdapterManager().getAdapter(element, IFilterable.class);
		}
		return decorator;
	}
}
