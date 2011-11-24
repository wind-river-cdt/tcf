/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.model.internal.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.runtime.activator.CoreBundleActivator;
import org.eclipse.tcf.te.runtime.extensions.ExecutableExtensionProxy;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.runtime.model.interfaces.factory.IFactoryDelegate;
import org.eclipse.tcf.te.runtime.model.nls.Messages;
import org.osgi.framework.Bundle;

/**
 * Model node factory delegate executable extension proxy implementation.
 */
public class FactoryDelegateProxy extends ExecutableExtensionProxy<IFactoryDelegate> {
	// The list of node types supported by the model node factory delegate
	private final List<Class<? extends IModelNode>> nodeTypes = new ArrayList<Class<? extends IModelNode>>();
	// Flag to mark if the node types has been loaded
	private boolean nodeTypesLoaded = false;

	/**
     * Constructor.
	 *
	 * @param element The configuration element. Must not be <code>null</code>.
	 * @throws CoreException In case the configuration element attribute <i>id</i> is <code>null</code> or empty.
     */
    public FactoryDelegateProxy(IConfigurationElement element) throws CoreException {
	    super(element);
    }

	/**
     * Constructor.
     *
	 * @param id The id for this instance.
	 * @param instance The instance to add to proxy.
     */
    public FactoryDelegateProxy(String id, IFactoryDelegate instance) {
	    super(id, instance);
    }

    /**
     * Returns the list of node types supported by the model node factory.
     *
     * @return The unmodifiable list of node types.
     */
    public List<Class<? extends IModelNode>> getNodeTypes() {
    	if (!nodeTypesLoaded) loadNodeTypes();
    	return Collections.unmodifiableList(nodeTypes);
    }

    /**
     * Load the node types.
     */
    protected void loadNodeTypes() {
    	IConfigurationElement element = getConfigurationElement();
    	Assert.isNotNull(element);

    	nodeTypes.clear();

		IConfigurationElement[] nodeTypeElements = element.getChildren("nodeType"); //$NON-NLS-1$
		if (nodeTypeElements != null && nodeTypeElements.length > 0) {
			for (IConfigurationElement nodeTypeElement : nodeTypeElements) {
				try {
					String type = nodeTypeElement.getAttribute("class"); //$NON-NLS-1$
					String bundleId = nodeTypeElement.getAttribute("bundleId"); //$NON-NLS-1$

					// If a bundle id got specified, use the specified bundle to load the node type class
					Bundle bundle = bundleId != null ? bundle = Platform.getBundle(bundleId) : null;
					// If we don't have a bundle to load from yet, fallback to the declaring bundle
					if (bundle == null) bundle = Platform.getBundle(element.getDeclaringExtension().getNamespaceIdentifier());
					// And finally, use our own bundle to load the class.
					// This fallback is expected to never be used.
					if (bundle == null) bundle = CoreBundleActivator.getContext().getBundle();

					// Try to load the node type class now.
					Class<? extends IModelNode> typeClass = (Class<? extends IModelNode>)(bundle != null ? bundle.loadClass(type) : Class.forName(type));
					this.nodeTypes.add(typeClass);
				}
				catch (Exception e) {
					IStatus status = new Status(IStatus.WARNING, CoreBundleActivator.getUniqueIdentifier(),
												NLS.bind(Messages.FactoryDelegateProxy_warning_failedToLoadNodeType, nodeTypeElement.getAttribute("class"), element.getDeclaringExtension().getUniqueIdentifier()), e); //$NON-NLS-1$
					Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
				}
			}
		}

    	nodeTypesLoaded = true;
    }
}
