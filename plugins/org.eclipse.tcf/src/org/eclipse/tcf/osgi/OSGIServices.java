/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.osgi;

import org.eclipse.tcf.osgi.services.IValueAddService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * OSGi service manager implementation.
 */
public class OSGIServices implements BundleActivator {
    // Reference to the value-add service tracker
    private ServiceTracker<IValueAddService, IValueAddService> valueAddServiceTracker = null;

    /*
     * Thread save singleton instance creation.
     */
    private static class LazyInstance {
        public static OSGIServices instance = new OSGIServices();
    }

    /**
     * Constructor.
     */
    /* default */ OSGIServices() {
        super();
    }

    /**
     * Returns the singleton instance of the extension point manager.
     */
    public static OSGIServices getInstance() {
        return LazyInstance.instance;
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        /*
         * Register the service tracker for the value-add service.
         */
        valueAddServiceTracker = new ServiceTracker<IValueAddService, IValueAddService>(context, IValueAddService.class, null);
        valueAddServiceTracker.open();
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        valueAddServiceTracker.close();
        valueAddServiceTracker = null;
    }

    /**
     * Returns the value-add service if registered.
     *
     * @return The value-add service instance or <code>null</code>.
     */
    public static IValueAddService getValueAddService() {
        return getInstance().valueAddServiceTracker != null ? getInstance().valueAddServiceTracker.getService() : null;
    }
}
