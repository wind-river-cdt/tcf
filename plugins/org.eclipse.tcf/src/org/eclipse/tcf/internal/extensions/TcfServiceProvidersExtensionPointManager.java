/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.extensions;

import java.util.Map;

import org.eclipse.tcf.extensions.TcfAbstractExtensionPointManager;
import org.eclipse.tcf.extensions.TcfExtensionProxy;
import org.eclipse.tcf.protocol.IServiceProvider;
import org.eclipse.tcf.protocol.Protocol;

/**
 * Extension point manager implementation for "org.eclipse.tcf.serviceProviders".
 */
public class TcfServiceProvidersExtensionPointManager extends TcfAbstractExtensionPointManager<IServiceProvider> {
    /*
     * Thread save singleton instance creation.
     */
    private static class LazyInstanceHolder {
        public static TcfServiceProvidersExtensionPointManager fInstance = new TcfServiceProvidersExtensionPointManager();
    }

    /**
     * Returns the singleton instance for the manager.
     */
    public static TcfServiceProvidersExtensionPointManager getInstance() {
        return LazyInstanceHolder.fInstance;
    }

    /* (non-Javadoc)
     * @see org.eclipse.tcf.extensions.TcfAbstractExtensionPointManager#getExtensionPointId()
     */
    @Override
    protected String getExtensionPointId() {
        return "org.eclipse.tcf.serviceProviders"; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.tcf.extensions.TcfAbstractExtensionPointManager#getConfigurationElementName()
     */
    @Override
    protected String getConfigurationElementName() {
        return "serviceProvider"; //$NON-NLS-1$
    }

    /**
     * Register the contributed service provider extensions with the framework.
     */
    public void registerServiceProviders() {
        // Load the extensions
        Map<String, TcfExtensionProxy<IServiceProvider>> extensions = getExtensions();
        // Loop the extensions and get the service provider instance.
        // This will activate the contributing plugin.
        for (TcfExtensionProxy<IServiceProvider> proxy : extensions.values()) {
            IServiceProvider provider = proxy.getInstance();
            if (provider != null) Protocol.addServiceProvider(provider);
        }
    }
}
