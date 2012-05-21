/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.handler;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tcf.te.ui.views.nls.Messages;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentDescriptorManager;
import org.eclipse.ui.navigator.IDescriptionProvider;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;

/**
 * The label provider for navigator content descriptors providing labels, images and descriptive
 * texts for content descriptors.
 */
@SuppressWarnings("restriction")
class ContentDescriptorLabelProvider extends LabelProvider implements IDescriptionProvider {
	// The shared instance
	static ContentDescriptorLabelProvider instance = new ContentDescriptorLabelProvider();
	// Used to acquire images for specified navigator content descriptors.
	private static final NavigatorContentDescriptorManager CONTENT_DESCRIPTOR_REGISTRY = NavigatorContentDescriptorManager .getInstance();

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IDescriptionProvider#getDescription(java.lang.Object)
	 */
	@Override
	public String getDescription(Object element) {
		if (element instanceof INavigatorContentDescriptor) {
			INavigatorContentDescriptor ncd = (INavigatorContentDescriptor) element;
			String desc = NLS.bind(Messages.CommonFilterDescriptorLabelProvider_ContentExtensionDescription, new Object[] { ncd.getName() });
			return desc;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
    @Override
	public Image getImage(Object element) {
		if (element instanceof INavigatorContentDescriptor) {
			return CONTENT_DESCRIPTOR_REGISTRY.getImage(((INavigatorContentDescriptor) element).getId());
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof INavigatorContentDescriptor) {
			return ((INavigatorContentDescriptor) element).getName();
		}
		return null;
	}
}
