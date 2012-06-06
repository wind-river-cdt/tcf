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

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tcf.te.ui.activator.UIPlugin;
import org.eclipse.tcf.te.ui.interfaces.IFilteringLabelDecorator;
import org.eclipse.tcf.te.ui.jface.images.AbstractImageDescriptor;
import org.eclipse.tcf.te.ui.utils.TreeViewerUtil;

/**
 * An adapter factory that adapt an object to IFilteringLabelDecorator used in
 * a tree viewer or a target explorer view to provide filtering decoration.
 */
public class AdapterFactory implements IAdapterFactory {
	
	static class FilteringLabelDecorator extends LabelProvider implements IFilteringLabelDecorator {
		private String pattern;
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateImage(org.eclipse.swt.graphics.Image, java.lang.Object)
		 */
		@Override
	    public Image decorateImage(Image image, Object element) {
			Image decoratedImage = image;
			if (image != null && element != null) {
				AbstractImageDescriptor descriptor = new FilteringImageDescriptor(UIPlugin.getDefault().getImageRegistry(), image);
				decoratedImage = UIPlugin.getSharedImage(descriptor);
			}
			return decoratedImage;
	    }

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(java.lang.String, java.lang.Object)
		 */
		@Override
	    public String decorateText(String text, Object element) {
			if(pattern != null) {
				return text + " /" + pattern + "/"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			return text;
	    }

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.tcf.te.ui.interfaces.IFilteringLabelDecorator#isEnabled(org.eclipse.jface.viewers.TreeViewer, java.lang.Object)
		 */
		@Override
	    public boolean isEnabled(TreeViewer viewer, Object element) {
		    if(TreeViewerUtil.isFiltering(viewer, element)) {
		    	pattern = TreeViewerUtil.getFilteringString(viewer);
		    	return true;
		    }
		    return false;
	    }
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if(adaptableObject != null && IFilteringLabelDecorator.class.equals(adapterType)) {
			return new FilteringLabelDecorator();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	public Class[] getAdapterList() {
		return new Class[]{IFilteringLabelDecorator.class};
	}
}
