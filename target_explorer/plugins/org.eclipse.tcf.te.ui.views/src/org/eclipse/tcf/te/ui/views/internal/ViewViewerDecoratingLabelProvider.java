/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.internal;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tcf.te.core.interfaces.IFilterable;
import org.eclipse.tcf.te.ui.utils.TreeViewerUtil;
import org.eclipse.ui.internal.navigator.NavigatorDecoratingLabelProvider;

/**
 * An wrapping decorating label provider to replace the default navigator decorating label provider
 * in order to provide the filtering decoration.
 */
@SuppressWarnings("restriction")
public class ViewViewerDecoratingLabelProvider extends NavigatorDecoratingLabelProvider {
	// The navigator's tree viewer to be decorated.
	private TreeViewer viewer;
	private TreePath path;
	/**
	 * Create an instance with the tree viewer and a common label provider.
	 * 
	 * @param viewer The navigator's tree viewer.
	 * @param commonLabelProvider The navigator's common label provider.
	 */
	public ViewViewerDecoratingLabelProvider(TreeViewer viewer, ILabelProvider commonLabelProvider) {
	    super(commonLabelProvider);
	    this.viewer = viewer;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider#update(org.eclipse.jface.viewers.ViewerCell)
	 */
	@Override
    public void update(ViewerCell cell) {
		path = cell.getViewerRow().getTreePath();
	    super.update(cell);
	    path = null;
    }

	/**
	 * Returns the element label with no decoration applied.
	 *
	 * @param element The element.
	 * @return The label.
	 */
	public String getTextNoDecoration(Object element) {
		IStyledLabelProvider provider = getStyledStringProvider();
		StyledString styledString = provider.getStyledText(element);
		String text = styledString != null ? styledString.toString() : super.getText(element);
		return text;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.internal.navigator.NavigatorDecoratingLabelProvider#getText(java.lang.Object)
	 */
	@Override
    public String getText(Object element) {
		StyledString styledString = super.getStyledText(element);
		String text = styledString != null ? styledString.toString() : super.getText(element);
		IFilterable decorator = adaptFilterable(element);
		if (text != null && decorator != null && path != null && TreeViewerUtil.isFiltering(viewer, path)) {
			return TreeViewerUtil.getDecoratedText(text, viewer, path);
		}
		return text;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider#getImage(java.lang.Object)
	 */
	@Override
    public Image getImage(Object element) {
		Image image = super.getImage(element);
		IFilterable decorator = adaptFilterable(element);
		if (image != null && decorator != null && path != null && TreeViewerUtil.isFiltering(viewer, path)) {
			return TreeViewerUtil.getDecoratedImage(image, viewer, path);
		}
		return image;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider#getStyledText(java.lang.Object)
	 */
	@Override
    protected StyledString getStyledText(Object element) {
		StyledString styledString = super.getStyledText(element);
		IFilterable decorator = adaptFilterable(element);
		String text = styledString.getString();
		if (text != null && decorator != null && path != null && TreeViewerUtil.isFiltering(viewer, path)) {
			String decorated = TreeViewerUtil.getDecoratedText(text, viewer, path);
			Styler style = getDecorationStyle(element);
			return StyledCellLabelProvider.styleDecoratedString(decorated, style, styledString);
		}
	    return styledString;
    }

	/**
	 * Get an adapter of IFilteringLabelProvider from the specified element.
	 * 
	 * @param element The element to get the adapter from.
	 * @return The element's adapter or null if does not adapt to IFilteringLabelProvider.
	 */
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.internal.navigator.NavigatorDecoratingLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	@Override
    public Image getColumnImage(Object element, int columnIndex) {
		Image image = super.getColumnImage(element, columnIndex);
		if (columnIndex == 0) {
			IFilterable decorator = adaptFilterable(element);
			if (image != null && decorator != null && path != null && TreeViewerUtil.isFiltering(viewer, path)) {
				return TreeViewerUtil.getDecoratedImage(image, viewer, path);
			}
		}
		return image;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.internal.navigator.NavigatorDecoratingLabelProvider#getColumnText(java.lang.Object, int)
	 */
	@Override
    public String getColumnText(Object element, int columnIndex) {
		String text = super.getColumnText(element, columnIndex);
		if (columnIndex == 0) {
			IFilterable decorator = adaptFilterable(element);
			if (text != null && decorator != null && path != null && TreeViewerUtil.isFiltering(viewer, path)) {
				return TreeViewerUtil.getDecoratedText(text, viewer, path);
			}
		}
		return text;
    }
}
