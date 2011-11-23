/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * William Chen (Wind River) - [361324] Add more file operations in the file 
 * 												system of Target Explorer.
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.tcf.te.ui.views.interfaces.IViewerConfigurator;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * This viewer configurator reads the extensions from the extension point
 * "org.eclipse.tcf.te.ui.views.configurators" and initialize the registered viewer
 * configurators.
 */
public class ViewViewerConfigurator implements IViewerConfigurator {
	private static final String EXTENSION_POINT_ID = "org.eclipse.tcf.te.ui.views.configurators"; //$NON-NLS-1$
	// The registered viewer configurators with the specified viewer id.
	List<IViewerConfigurator> configurators;

	/**
	 * Create an instance with the specified viewer id.
	 */
	public ViewViewerConfigurator(String viewerId) {
		Assert.isTrue(viewerId != null);
		configurators = Collections.synchronizedList(new ArrayList<IViewerConfigurator>());
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT_ID);
		IExtension[] extensions = extensionPoint.getExtensions();
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				String name = element.getName();
				String id = element.getAttribute("viewerId"); //$NON-NLS-1$
				if (name.equals("configurator") && viewerId.equals(id)) { //$NON-NLS-1$
					addConfigurator(element);
				}
			}
		}
	}

	/**
	 * Add the viewer configurator defined in the configuration element to the configurator list.
	 * 
	 * @param element The configuration element that defines the viewer configurator.
	 */
	private void addConfigurator(final IConfigurationElement element) {
		SafeRunner.run(new ISafeRunnable() {
			@Override
			public void handleException(Throwable exception) {
				// Ignore it.
			}

			@Override
			public void run() throws Exception {
				IViewerConfigurator configurator = (IViewerConfigurator) element.createExecutableExtension("class"); //$NON-NLS-1$
				configurators.add(configurator);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.interfaces.IViewerConfigurator#configure(org.eclipse.ui.navigator.CommonViewer)
	 */
	@Override
	public void configure(CommonViewer viewer) {
		for (IViewerConfigurator configurator : configurators) {
			configurator.configure(viewer);
		}
	}
}
