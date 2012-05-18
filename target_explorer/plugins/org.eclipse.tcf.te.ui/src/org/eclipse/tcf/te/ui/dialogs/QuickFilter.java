/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.dialogs;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.TreeItem;
/**
 * A quick filter is a viewer filter that selects elements, 
 * which has the specified name pattern, under a certain tree path.
 * Other elements outside of this tree path is ignored.
 */
public class QuickFilter extends TablePatternFilter {
	// The tree viewer to filter.
	private TreeViewer viewer;
	// The root path to select from.
	private Object root;

	/**
	 * Create a quick filter for the specified viewer.
	 */
	private QuickFilter(TreeViewer viewer) {
		super((ILabelProvider) viewer.getLabelProvider());
		this.viewer = viewer;
	}

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
	 * Reset the viewer to the original view.
	 * 
	 * @param viewer The viewer to be reset.
	 */
	public static void doReset(TreeViewer viewer) {
		if (QuickFilter.getQuickFilter(viewer).isFiltering()) {
			QuickFilter.getQuickFilter(viewer).resetViewer();
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
			QuickFilter.getQuickFilter(viewer).showFilterPopup(root);
		}
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
	 * Test if the root for the tree viewer is eligible as a root path 
	 * of a quick filter.
	 * 
	 * @param root The root path to be tested.
	 * @param viewer The tree viewer to be filtered.
	 * @return true if it is eligible as a root path or else false.
	 */
	private static boolean isEligibleRoot(TreePath root, TreeViewer viewer) {
		if (viewer.getExpandedState(root)) {
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

	/**
	 * Show the pop up dialog for the specified root path.
	 *  
	 * @param root The root path to filter from.
	 */
	private void showFilterPopup(Object root) {
		this.root = root;
		if (!isFiltering()) {
			viewer.addFilter(this);
		}
		QuickFilterPopup popup = new QuickFilterPopup(viewer, this);
		Point location = null;
		if (root != null) {
			TreeItem[] items = viewer.getTree().getSelection();
			if (items != null && items.length > 0) {
				Rectangle bounds = items[0].getBounds();
				location = new Point(bounds.x, bounds.y);
			}
			else {
				location = new Point(0, 0);
			}
		}
		else {
			location = new Point(0, 0);
		}
		location.y -= viewer.getTree().getItemHeight();
		location = viewer.getTree().toDisplay(location);
		popup.open();
		popup.getShell().setLocation(location);
	}

	/**
	 * Reset the tree viewer to the ogriginal view by removing this filter.
	 */
	void resetViewer() {
		viewer.removeFilter(this);
		root = null;
		setPattern(null);
	}

	/**
	 * If the current viewer is being filtered.
	 * 
	 * @return true if it has this filter.
	 */
	private boolean isFiltering() {
		ViewerFilter[] filters = viewer.getFilters();
		if (filters != null) {
			for (ViewerFilter filter : filters) {
				if (filter == this) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.dialogs.TablePatternFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return skipMatching(parentElement) || super.select(viewer, parentElement, element);
	}

	/**
	 * If the specified parent element should be skipped when matching elements.
	 * 
	 * @param parentElement The parent element.
	 * @return true if it should be skipped.
	 */
	private boolean skipMatching(Object parentElement) {
		if (root == null || parentElement == null) return true;
		if (parentElement instanceof TreePath) {
			if (root instanceof TreePath) {
				return !root.equals(parentElement);
			}
			Object parent = ((TreePath) parentElement).getLastSegment();
			return !root.equals(parent);
		}
		if (root instanceof TreePath) {
			Object rootElement = ((TreePath) root).getLastSegment();
			return !parentElement.equals(rootElement);
		}
		return !root.equals(parentElement);
	}
}
