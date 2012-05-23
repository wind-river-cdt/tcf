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

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreePath;
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
	public QuickFilter(TreeViewer viewer) {
		super((ILabelProvider) viewer.getLabelProvider());
		this.viewer = viewer;
	}

	/**
	 * Show the pop up dialog for the specified root path.
	 *  
	 * @param root The root path to filter from.
	 */
	public void showFilterPopup(Object root) {
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
	 * Reset the tree viewer to the original view by removing this filter.
	 */
	public void resetViewer() {
		viewer.removeFilter(this);
		root = null;
		setPattern(null);
	}

	/**
	 * If the current viewer is being filtered.
	 * 
	 * @return true if it has this filter.
	 */
	public boolean isFiltering() {
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
