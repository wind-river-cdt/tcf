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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.dialogs.WizardCollectionElement;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * New wizard category node implementation.
 */
@SuppressWarnings("restriction")
public class NewWizardCategory implements IWizardCategory, IWorkbenchAdapter {

	private String id;
	private String label;
	private IWizardCategory parent;

	private List<IWizardCategory> categories = new ArrayList<IWizardCategory>();
	private List<IWizardDescriptor> wizards = new ArrayList<IWizardDescriptor>();

	/**
	 * Constructor.
	 */
	public NewWizardCategory(String id, String label) {
		this.id = id;
		this.label = label;
	}

	/**
	 * Constructor.
	 */
	public NewWizardCategory(IWizardCategory baseCategory) {
		id = baseCategory.getId();
		label = baseCategory.getLabel();
		parent = baseCategory.getParent();

		IWizardCategory[] baseCategories = baseCategory.getCategories();
		if (baseCategories != null && baseCategories.length > 0) {
			categories = new ArrayList<IWizardCategory>(Arrays.asList(baseCategories));
		}

		IWizardDescriptor[] baseWizards = baseCategory.getWizards();
		if (baseWizards != null && baseWizards.length > 0) {
			wizards = new ArrayList<IWizardDescriptor>(Arrays.asList(baseWizards));
		}
	}

	/**
	 * Clear the list of wizards and sub categories.
	 */
	public void clear() {
		categories.clear();
		wizards.clear();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		if (other instanceof IWizardCategory) {
			return ((IWizardCategory)other).getId().equals(getId());
		}
		return super.equals(other);
	}

	/**
	 * Set the parent wizard category.
	 *
	 * @param parent The parent wizard category or <code>null</code>.
	 */
	public void setParent(IWizardCategory parent) {
		this.parent = parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.wizards.IWizardCategory#findCategory(org.eclipse.core.runtime.IPath)
	 */
	@Override
	public IWizardCategory findCategory(IPath path) {
		String searchString = path.segment(0);
		for (IWizardCategory category : getCategories()) {
			if (category.getId().equals(searchString)) {
				if (path.segmentCount() == 1) {
					return category;
				}

				return category.findCategory(path.removeFirstSegments(1));
			}
		}

		return null;
	}

	/**
	 * Find a category with the given id.
	 *
	 * @param id The category id.
	 * @return The category or <code>null</code>.
	 */
	public IWizardCategory findCategory(String id) {
		for (IWizardCategory category : getCategories()) {
			if (id.equals(category.getId())) {
				return category;
			}
		}
		for (IWizardCategory category : getCategories()) {
			IWizardCategory found = null;
			if (category instanceof NewWizardCategory) {
				found = ((NewWizardCategory)category).findCategory(id);
			}
			if (category instanceof WizardCollectionElement) {
				found = ((WizardCollectionElement)category).findCategory(id);
			}
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.wizards.IWizardCategory#findWizard(java.lang.String)
	 */
	@Override
	public IWizardDescriptor findWizard(String id) {
		for (IWizardDescriptor wizard : getWizards()) {
			if (wizard.getId().equals(id)) {
				return wizard;
			}
		}
		for (IWizardCategory category : getCategories()) {
			IWizardDescriptor wizard = category.findWizard(id);
			if (wizard != null) {
				return wizard;
			}
		}
		return null;
	}

	/**
	 * Add a sub category.
	 *
	 * @param category The category. Must not be <code>null</code>.
	 */
	public void addCategory(IWizardCategory category) {
		Assert.isNotNull(category);
		categories.add(category);
	}

	/**
	 * Removes a sub category.
	 *
	 * @param category The category. Must not be <code>null</code>.
	 */
	public void removeCategory(IWizardCategory category) {
		Assert.isNotNull(category);
		categories.remove(category);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.wizards.IWizardCategory#getCategories()
	 */
	@Override
	public IWizardCategory[] getCategories() {
		return categories.toArray(new IWizardCategory[categories.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.wizards.IWizardCategory#getId()
	 */
	@Override
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.wizards.IWizardCategory#getLabel()
	 */
	@Override
	public String getLabel() {
		return label;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.wizards.IWizardCategory#getParent()
	 */
	@Override
	public IWizardCategory getParent() {
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.wizards.IWizardCategory#getPath()
	 */
	@Override
	public IPath getPath() {
		return getParent() != null ? getParent().getPath().append(getId()) : new Path(getId());
	}

	/**
	 * Add a wizard.
	 * @param wizard The wizard.
	 */
	public void addWizard(IWizardDescriptor wizard) {
		wizards.add(wizard);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.wizards.IWizardCategory#getWizards()
	 */
	@Override
	public IWizardDescriptor[] getWizards() {
		return wizards.toArray(new IWizardDescriptor[wizards.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object o) {
		return new Object[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	@Override
    public ImageDescriptor getImageDescriptor(Object object) {
		return WorkbenchImages.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	@Override
	public String getLabel(Object o) {
		return getLabel();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object o) {
		return getParent();
	}
}
