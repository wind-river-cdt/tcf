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
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tcf.te.runtime.extensions.ExecutableExtension;
import org.eclipse.tcf.te.ui.views.interfaces.ICategory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Abstract main view category node implementation.
 */
public abstract class AbstractCategory extends ExecutableExtension implements ICategory {
	// The category image
	private Image image = null;

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.ExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
	    super.setInitializationData(config, propertyName, data);

        // Read the icon attribute and create the image
        String attrIcon = config.getAttribute("icon");//$NON-NLS-1$
        if (attrIcon != null) {
        	ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(config.getNamespaceIdentifier(), attrIcon);
        	if (descriptor != null) {
        		image = JFaceResources.getResources().createImageWithDefault(descriptor);
        	}
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.interfaces.ICategory#getImage()
	 */
	@Override
	public Image getImage() {
	    return image;
	}
}
