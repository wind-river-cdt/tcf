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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.TreeItem;

public class QuickFilter extends TablePatternFilter {
	private TreeViewer viewer;
	private Object root;

	private QuickFilter(TreeViewer viewer) {
		super((ILabelProvider) viewer.getLabelProvider());
		this.viewer = viewer;
	}

	public static QuickFilter getQuickFilter(TreeViewer viewer) {
		QuickFilter filter = (QuickFilter) viewer.getData("quick.filter"); //$NON-NLS-1$
		if (filter == null) {
			filter = new QuickFilter(viewer);
			viewer.setData("quick.filter", filter); //$NON-NLS-1$
		}
		return filter;
	}

	public void showFilter(Object root) {
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

	public void resetViewer() {
		viewer.removeFilter(this);
		root = null;
		setPattern(null);
	}

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

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (root != null && root.equals(parentElement)) {
			return super.select(viewer, parentElement, element);
		}
		return true;
	}
}
