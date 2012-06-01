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

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * New wizard descriptor implementation.
 */
public class NewWizardDescriptor implements IWizardDescriptor, IWorkbenchAdapter {

	private String id;
	private String label;
	private String description;
	private String helpRef;
	private ImageDescriptor imageDescriptor;
	private IWizardCategory category;
	private IWorkbenchWizard wizard;
	private String[] tags;
	private boolean canFinishEarly = false;
	private boolean hasPages = true;

	/**
	 * Constructor.
	 */
	public NewWizardDescriptor(IWizardCategory category, String id, String label, String description, String helpRef, ImageDescriptor imageDescriptor) {
		this.category = category;
		this.id = id;
		this.label = label;
		this.description = description;
		this.helpRef = helpRef;
		this.imageDescriptor = imageDescriptor;
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
		if (other instanceof IWizardDescriptor) {
			return ((IWizardDescriptor)other).getId().equals(getId());
		}
		return super.equals(other);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartDescriptor#getId()
	 */
	@Override
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartDescriptor#getImageDescriptor()
	 */
	@Override
	public ImageDescriptor getImageDescriptor() {
		return imageDescriptor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPartDescriptor#getLabel()
	 */
	@Override
	public String getLabel() {
		return label;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.wizards.IWizardDescriptor#adaptedSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public IStructuredSelection adaptedSelection(IStructuredSelection selection) {
		return selection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.wizards.IWizardDescriptor#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * Set tags for this wizard.
	 * @param tags The tags.
	 */
	public void setTags(String[] tags) {
		this.tags = tags != null ? Arrays.copyOf(tags, tags.length) : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.wizards.IWizardDescriptor#getTags()
	 */
	@Override
	public String[] getTags() {
		return tags != null ? Arrays.copyOf(tags, tags.length) : new String[0];
	}

	/**
	 * Set the wizard.
	 * @param wizard The wizard.
	 */
	public void setWizard(IWorkbenchWizard wizard) {
		this.wizard = wizard;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.wizards.IWizardDescriptor#createWizard()
	 */
	@Override
	public IWorkbenchWizard createWizard() throws CoreException {
		return wizard;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.wizards.IWizardDescriptor#getDescriptionImage()
	 */
	@Override
	public ImageDescriptor getDescriptionImage() {
		return imageDescriptor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.wizards.IWizardDescriptor#getHelpHref()
	 */
	@Override
	public String getHelpHref() {
		return helpRef;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.wizards.IWizardDescriptor#getCategory()
	 */
	@Override
	public IWizardCategory getCategory() {
		return category;
	}

	/**
	 * Set the canFinishEarly flag.
	 * @param canFinishEarly
	 */
	public void setCanFinishEarly(boolean canFinishEarly) {
		this.canFinishEarly = canFinishEarly;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.wizards.IWizardDescriptor#canFinishEarly()
	 */
	@Override
	public boolean canFinishEarly() {
		return canFinishEarly;
	}

	/**
	 * Set the hasPages flag.
	 * @param hasPages
	 */
	public void setHasPages(boolean hasPages) {
		this.hasPages = hasPages;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.wizards.IWizardDescriptor#hasPages()
	 */
	@Override
	public boolean hasPages() {
		return hasPages;
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
		return getImageDescriptor();
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
		return getCategory();
	}
}
