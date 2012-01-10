/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.wizards.newWizard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.eclipse.ui.wizards.IWizardRegistry;

/**
 * New wizard content provider implementation.
 */
public class NewWizardContentProvider implements ITreeContentProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		List<Object> children = new ArrayList<Object>();
		if (inputElement instanceof IWizardRegistry) {
			IWizardRegistry registry = (IWizardRegistry)inputElement;
			IWizardDescriptor[] primary = registry.getPrimaryWizards();
			if (primary != null && primary.length > 0) {
				children.addAll(Arrays.asList(primary));
			}
			children.addAll(Arrays.asList(getChildren(registry.getRootCategory())));

			if (children.size() == 1 && children.get(0) instanceof IWizardCategory) {
				IWizardCategory category = (IWizardCategory)children.get(0);
				children.clear();
				children.addAll(Arrays.asList(getChildren(category)));
			}
		}

		return children.toArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		List<Object> children = new ArrayList<Object>();
		if (parentElement instanceof IWizardCategory) {
			for (IWizardCategory category : ((IWizardCategory)parentElement).getCategories()) {
				if ((category.getCategories() != null && category.getCategories().length > 0) ||
					(category.getWizards() != null && category.getWizards().length > 0)) {
					children.add(category);
				}
			}
			children.addAll(Arrays.asList(((IWizardCategory)parentElement).getWizards()));

			return children.toArray();
		}
		return new Object[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object element) {
		if (element instanceof IWizardCategory) {
			return ((IWizardCategory)element).getParent();
		}
		else if (element instanceof IWizardDescriptor) {
			return ((IWizardDescriptor)element).getCategory();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

}
