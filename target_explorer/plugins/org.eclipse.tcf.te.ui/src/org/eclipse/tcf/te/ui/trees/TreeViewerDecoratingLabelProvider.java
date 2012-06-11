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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelDecorator;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tcf.te.core.interfaces.IFilterable;
import org.eclipse.tcf.te.ui.utils.TreeViewerUtil;

/**
 * A subclass of DecoratingLabelProvider provides an FS Tree Viewer
 * with a label provider which combines a nested label provider and an optional
 * decorator. The decorator decorates the label text, image
 * provided by the nested label provider.
 *
 */
public class TreeViewerDecoratingLabelProvider extends DecoratingLabelProvider implements ITableLabelProvider {

	//The label provider for the execution context viewer.
	private TreeViewerLabelProvider fProvider;
	//The label decorator decorating the above label provider.
	private ILabelDecorator fDecorator;
	//Tree viewer that this label provider serves.
	private TreeViewer viewer;
	// The content provider of this viewer.
	private ITreeContentProvider contentProvider;

	/**
	 * Create a FSTreeDecoratingLabelProvider with an FSTreeLabelProvider and a decorator.
	 *
	 * @param provider The label provider to be decorated.
	 * @param decorator The label decorator.
	 */
	public TreeViewerDecoratingLabelProvider(TreeViewer viewer, TreeViewerLabelProvider provider, ILabelDecorator decorator) {
		super(provider, decorator);
		fProvider = provider;
		fDecorator = decorator;
		this.viewer = viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		Image image = fProvider.getColumnImage(element, columnIndex);
		if (columnIndex == 0) {
			if (fDecorator != null) {
				if (fDecorator instanceof LabelDecorator) {
					LabelDecorator ld2 = (LabelDecorator) fDecorator;
					Image decorated = ld2.decorateImage(image, element, getDecorationContext());
					if (decorated != null) {
						image = decorated;
					}
				}
				else {
					Image decorated = fDecorator.decorateImage(image, element);
					if (decorated != null) {
						image = decorated;
					}
				}
			}
			IFilterable decorator = adaptFilterable(element);
			TreePath path = getTreePath(element);
			if (image != null && decorator != null && path != null && TreeViewerUtil.isFiltering(viewer, path)) {
				image = TreeViewerUtil.getDecoratedImage(image, viewer, path);
			}
		}
		return image;
	}

	/**
	 * Get the tree path whose leaf is the element.
	 * 
	 * @param element The leaf element.
	 * @return The tree path.
	 */
	private TreePath getTreePath(Object element) {
		List<Object> elements = new ArrayList<Object>();
		while(element != null) {
			elements.add(0, element);
			element = getParent(element);
		}
		if(elements.isEmpty()) return TreePath.EMPTY;
		return new TreePath(elements.toArray());
	}

	/**
	 * get the parent path.
	 * 
	 * @param element The element whose parent is being retrieved.
	 * @return The parent element.
	 */
	private Object getParent(Object element) {
		if(this.contentProvider == null) {
			contentProvider = (ITreeContentProvider) viewer.getContentProvider();
		}
	    return contentProvider.getParent(element);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	@Override
	public String getColumnText(Object element, int columnIndex) {
		String text = fProvider.getColumnText(element, columnIndex);
		if (columnIndex == 0) {
			if (fDecorator != null) {
				if (fDecorator instanceof LabelDecorator) {
					LabelDecorator ld2 = (LabelDecorator) fDecorator;
					String decorated = ld2.decorateText(text, element, getDecorationContext());
					if (decorated != null) {
						text = decorated;
					}
				}
				else {
					String decorated = fDecorator.decorateText(text, element);
					if (decorated != null) {
						text = decorated;
					}
				}
			}
			IFilterable decorator = adaptFilterable(element);
			TreePath path = getTreePath(element);
			if (decorator != null && path != null && TreeViewerUtil.isFiltering(viewer, path)) {
				text = TreeViewerUtil.getDecoratedText(text, viewer, path);
			}
		}
		return text;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.DecoratingLabelProvider#getImage(java.lang.Object)
	 */
	@Override
    public Image getImage(Object element) {
	    Image image = super.getImage(element);
		IFilterable decorator = adaptFilterable(element);
		TreePath path = getTreePath(element);
		if (image != null && decorator != null && path != null && TreeViewerUtil.isFiltering(viewer, path)) {
			image = TreeViewerUtil.getDecoratedImage(image, viewer, path);
		}
		return image;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.DecoratingLabelProvider#getText(java.lang.Object)
	 */
	@Override
    public String getText(Object element) {
	    String text = super.getText(element);
		IFilterable decorator = adaptFilterable(element);
		TreePath path = getTreePath(element);
		if (text != null && decorator != null && path != null && TreeViewerUtil.isFiltering(viewer, path)) {
			text = TreeViewerUtil.getDecoratedText(text, viewer, path);
		}
		return text;
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
}
