/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.trees;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelDecorator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tcf.te.ui.interfaces.IFilteringLabelDecorator;

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
			if (image != null) {
				IFilteringLabelDecorator decorator = getFilteringDecorator(element);
				if (decorator != null && decorator.isEnabled(viewer, element)) {
					return decorator.decorateImage(image, element);
				}
			}
		}
		return image;
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
			IFilteringLabelDecorator decorator = getFilteringDecorator(element);
			if (decorator != null && decorator.isEnabled(viewer, element)) {
				text = decorator.decorateText(text, element);
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
		if (image != null) {
			IFilteringLabelDecorator decorator = getFilteringDecorator(element);
			if (decorator != null && decorator.isEnabled(viewer, element)) {
				return decorator.decorateImage(image, element);
			}
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
		IFilteringLabelDecorator decorator = getFilteringDecorator(element);
		if (decorator != null && decorator.isEnabled(viewer, element)) {
			return decorator.decorateText(text, element);
		}
		return text;
    }

	/**
	 * Get an adapter of IFilteringLabelProvider from the specified element.
	 * 
	 * @param element The element to get the adapter from.
	 * @return The element's adapter or null if does not adapt to IFilteringLabelProvider.
	 */
	private IFilteringLabelDecorator getFilteringDecorator(Object element) {
		IFilteringLabelDecorator decorator = null;
		if(element instanceof IFilteringLabelDecorator) {
			decorator = (IFilteringLabelDecorator) element;
		}
		if(decorator == null && element instanceof IAdaptable) {
			decorator = (IFilteringLabelDecorator) ((IAdaptable)element).getAdapter(IFilteringLabelDecorator.class);
		}
		if(decorator == null) {
			decorator = (IFilteringLabelDecorator) Platform.getAdapterManager().getAdapter(element, IFilteringLabelDecorator.class);
		}
		return decorator;
	}
	
}
