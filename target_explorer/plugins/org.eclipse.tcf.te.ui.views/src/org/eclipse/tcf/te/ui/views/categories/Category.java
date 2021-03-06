/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.categories;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tcf.te.runtime.extensions.ExecutableExtension;
import org.eclipse.tcf.te.runtime.interfaces.IDisposable;
import org.eclipse.tcf.te.ui.views.Managers;
import org.eclipse.tcf.te.ui.views.interfaces.ICategory;
import org.eclipse.tcf.te.ui.views.interfaces.categories.ICategorizable;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Default category implementation.
 */
public class Category extends ExecutableExtension implements ICategory, IDisposable, IPersistableElement {
	// The category image / image descriptor
	private ImageDescriptor descriptor = null;
	private Image image = null;
	// The sorting rank
	private int rank = -1;

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.ExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		super.setInitializationData(config, propertyName, data);

		// Read the icon attribute and create the image
		String attrIcon = config.getAttribute("icon");//$NON-NLS-1$
		if (attrIcon != null) {
			descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(config.getNamespaceIdentifier(), attrIcon);
			if (descriptor != null) {
				image = JFaceResources.getResources().createImageWithDefault(descriptor);
			}
		}

		// Read the rank attribute
		String attrRank = config.getAttribute("rank"); //$NON-NLS-1$
		if (attrRank != null) {
			try {
				rank = Integer.valueOf(attrRank).intValue();
			} catch (NumberFormatException e) { /* ignored on purpose */ }
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if(adapter == IPersistableElement.class) {
			return this;
		}
		return super.getAdapter(adapter);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IPersistable#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void saveState(IMemento memento) {
		memento.putString("id", this.getId()); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IPersistableElement#getFactoryId()
	 */
	@Override
	public String getFactoryId() {
		return "org.eclipse.tcf.te.ui.views.categoryFactory"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.IDisposable#dispose()
	 */
	@Override
	public void dispose() {
		if (descriptor != null) {
			JFaceResources.getResources().destroyImage(descriptor);
			descriptor = null;
		}
		image = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.interfaces.ICategory#getImage()
	 */
	@Override
	public Image getImage() {
		return image;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.interfaces.ICategory#getRank()
	 */
	@Override
	public int getRank() {
		return rank;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.interfaces.ICategory#belongsTo(java.lang.Object)
	 */
	@Override
	public boolean belongsTo(Object element) {
		ICategorizable categorizable = null;
		if (element instanceof IAdaptable) {
			categorizable = (ICategorizable)((IAdaptable)element).getAdapter(ICategorizable.class);
		}
		if (categorizable == null) {
			categorizable = (ICategorizable)Platform.getAdapterManager().getAdapter(element, ICategorizable.class);
		}
		return categorizable != null ? Managers.getCategoryManager().belongsTo(getId(), categorizable.getId()) : false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer(getLabel());
		buffer.append(" ["); //$NON-NLS-1$
		buffer.append(getId());
		buffer.append("] {rank="); //$NON-NLS-1$
		buffer.append(getRank());
		buffer.append("}"); //$NON-NLS-1$
		return buffer.toString();
	}
}
