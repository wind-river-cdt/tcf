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
import org.eclipse.tcf.te.runtime.interfaces.IDisposable;
import org.eclipse.tcf.te.ui.views.interfaces.ICategory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Abstract main view category node implementation.
 */
public abstract class AbstractCategory extends ExecutableExtension implements ICategory, IDisposable {
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
}
