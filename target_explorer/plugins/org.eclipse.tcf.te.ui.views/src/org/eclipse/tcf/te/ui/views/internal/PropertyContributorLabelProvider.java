/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * The label provider used by propertyContributor to provide a title bar.
 */
public class PropertyContributorLabelProvider extends LabelProvider {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
    public Image getImage(Object element) {
		if(element instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) element;
			Object object = selection.getFirstElement();
			ILabelProvider labelProvider = adapt(object);
			if(labelProvider != null) return labelProvider.getImage(object);
		}
		return super.getImage(element);
    }
	
	/**
	 * Adapt the object to a label provider if it is adaptable.
	 * 
	 * @param object The object to be adapted.
	 * @return The label provider for it.
	 */
	private ILabelProvider adapt(Object object) {
		ILabelProvider labelProvider = null;
		if(object instanceof ILabelProvider) {
			labelProvider = (ILabelProvider) object;
		} else if(object instanceof IAdaptable) {
			labelProvider = (ILabelProvider) ((IAdaptable)object).getAdapter(ILabelProvider.class);
		}
		if(object != null && labelProvider == null) {
			labelProvider = (ILabelProvider) Platform.getAdapterManager().getAdapter(object, ILabelProvider.class);
		}
		return labelProvider;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
    public String getText(Object element) {
		if(element instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) element;
			Object object = selection.getFirstElement();
			ILabelProvider labelProvider = adapt(object);
			if(labelProvider != null) return labelProvider.getText(object);
		}
		return super.getText(element);
    }
}
