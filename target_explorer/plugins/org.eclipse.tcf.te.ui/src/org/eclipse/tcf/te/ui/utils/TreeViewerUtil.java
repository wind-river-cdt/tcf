/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.utils;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.tcf.te.ui.internal.utils.QuickFilter;
import org.eclipse.tcf.te.ui.internal.utils.SearchEngine;
import org.eclipse.tcf.te.ui.internal.utils.TreeViewerSearchDialog;
/**
 * The utilities to search and filter a tree viewer.
 */
public class TreeViewerUtil {

	/**
	 * Get and attach a quick filter for a viewer.
	 * 
	 * @param viewer The viewer to get quick filter from.
	 * @return A single quick filter for each viewer.
	 */
	private static QuickFilter getQuickFilter(TreeViewer viewer) {
		QuickFilter filter = (QuickFilter) viewer.getData("quick.filter"); //$NON-NLS-1$
		if (filter == null) {
			filter = new QuickFilter(viewer);
			viewer.setData("quick.filter", filter); //$NON-NLS-1$
		}
		return filter;
	}

	/**
	 * Get a singleton search engine for a tree viewer. If
	 * it does not exist then create one and store it.
	 * 
	 * @param viewer The tree viewer.
	 * @return A search engine.
	 */
	public static SearchEngine getSearchEngine(TreeViewer viewer) {
		SearchEngine searcher = (SearchEngine) viewer.getData("search.engine"); //$NON-NLS-1$
		if (searcher == null) {
			searcher = new SearchEngine(viewer);
			viewer.setData("search.engine", searcher); //$NON-NLS-1$
		}
		return searcher;
	}

	/**
	 * Reset the viewer to the original view.
	 * 
	 * @param viewer The viewer to be reset.
	 */
	public static void doReset(TreeViewer viewer) {
		if (getQuickFilter(viewer).isFiltering()) {
			getQuickFilter(viewer).resetViewer();
		}
	}

	/**
	 * Provide a pop up to enter filter to filter the tree viewer.
	 * 
	 * @param viewer The tree viewer to be filtered.
	 */
	public static void doFilter(TreeViewer viewer) {
		TreePath rootPath = getSelectedPath(viewer);
		Object root = getFilterRoot(viewer, rootPath);
		if (root == viewer.getInput() || viewer.getExpandedState(root)) {
			getQuickFilter(viewer).showFilterPopup(root);
		}
	}

	/**
	 * Search a tree viewer for specified name specified in the pop up dialog.
	 * 
	 * @param viewer The tree viewer to be searched.
	 */
	public static void doSearch(TreeViewer viewer) {
		TreePath rootPath = getSelectedPath(viewer);
		rootPath = getSearchRoot(viewer, rootPath);
		TreeViewerSearchDialog dialog = new TreeViewerSearchDialog(viewer);
		dialog.setStartPath(rootPath);
		dialog.open();
	}
	
	/**
	 * Search the next element in the tree viewer.
	 * 
	 * @param viewer The tree viewer to be searched.
	 */
	public static void doSearchNext(TreeViewer viewer) {
		getSearchEngine(viewer).startSearch(null, null);
	}
	
	/**
	 * If the specified element is being filtered.
	 * 
	 * @param element
	 * @return
	 */
	public static boolean isFiltering(TreeViewer viewer, Object element) {
		if(element != null) {
			QuickFilter filter = TreeViewerUtil.getQuickFilter(viewer);
			if(filter != null) {
				return filter.isFiltering() && filter.isFiltering(element);
			}
		}
		return false;
	}

	/**
	 * Get the filter root for the viewer based on the root path.
	 * 
	 * @param viewer The tree viewer.
	 * @param rootPath The root path of the filter.
	 * @return An adjust filter.
	 */
	private static Object getFilterRoot(TreeViewer viewer, TreePath rootPath) {
		if (rootPath != null) {
			if (!isEligibleRoot(rootPath, viewer)) {
				rootPath = rootPath.getParentPath();
				if (rootPath.getSegmentCount() == 0) {
					viewer.setSelection(StructuredSelection.EMPTY);
				}
				else {
					viewer.setSelection(new StructuredSelection(rootPath), true);
				}
			}
			if (rootPath.getSegmentCount() == 0) {
				viewer.setSelection(StructuredSelection.EMPTY);
				return viewer.getInput();
			}
			return rootPath;
		}
		viewer.setSelection(StructuredSelection.EMPTY);
		return viewer.getInput();
	}
	
	/**
	 * Reposition the starting search path.
	 */
	private static TreePath getSearchRoot(TreeViewer viewer, TreePath rootPath) {
		if (rootPath != null) {
			if (!hasChildren(rootPath, viewer)) {
				rootPath = rootPath.getParentPath();
			}
			if (rootPath.getSegmentCount() == 0) {
				return new TreePath(new Object[] { viewer.getInput() });
			}
			return rootPath;
		}
		return new TreePath(new Object[] { viewer.getInput() });
	}
	
	/**
	 * Test if the root for the tree viewer is eligible as a root path 
	 * of a quick filter.
	 * 
	 * @param root The root path to be tested.
	 * @param viewer The tree viewer to be filtered.
	 * @return true if it is eligible as a root path or else false.
	 */
	private static boolean isEligibleRoot(TreePath root, TreeViewer viewer) {
		if (viewer.getExpandedState(root)) {
			return hasChildren(root, viewer);
		}
		return false;
	}
	
	/**
	 * Judges if the specified root has children nodes.
	 */
	private static boolean hasChildren(TreePath root, TreeViewer viewer) {
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

	/**
	 * Get the selected path of the viewer.
	 * 
	 * @param viewer The tree viewer to get the selected path from.
	 * @return the first selected path or null if no selected path.
	 */
	private static TreePath getSelectedPath(TreeViewer viewer) {
		ISelection selection = viewer.getSelection();
		if (selection instanceof TreeSelection) {
			TreeSelection treeSelection = (TreeSelection) selection;
			TreePath[] paths = treeSelection.getPaths();
			if (paths != null && paths.length > 0) {
				return paths[0];
			}
		}
		return null;
	}

}
