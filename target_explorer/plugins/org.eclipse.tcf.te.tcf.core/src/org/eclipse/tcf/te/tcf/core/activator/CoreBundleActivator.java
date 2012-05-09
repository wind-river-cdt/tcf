/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.activator;

import java.util.Hashtable;

import org.eclipse.tcf.osgi.services.IValueAddService;
import org.eclipse.tcf.te.runtime.tracing.TraceHandler;
import org.eclipse.tcf.te.tcf.core.internal.Startup;
import org.eclipse.tcf.te.tcf.core.osgi.ValueAddService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;


/**
 * The activator class controls the plug-in life cycle
 */
public class CoreBundleActivator implements BundleActivator {
	// The bundle context
	private static BundleContext context;
	// The trace handler instance
	private static volatile TraceHandler traceHandler;
	// The value-add OSGi service registration
	private ServiceRegistration<IValueAddService> valueAddService;

	/**
	 * Returns the bundle context
	 *
	 * @return the bundle context
	 */
	public static BundleContext getContext() {
		return context;
	}

	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	public static String getUniqueIdentifier() {
		if (getContext() != null && getContext().getBundle() != null) {
			return getContext().getBundle().getSymbolicName();
		}
		return "org.eclipse.tcf.te.tcf.core"; //$NON-NLS-1$
	}

	/**
	 * Returns the bundles trace handler.
	 *
	 * @return The bundles trace handler.
	 */
	public static TraceHandler getTraceHandler() {
		if (traceHandler == null) {
			traceHandler = new TraceHandler(getUniqueIdentifier());
		}
		return traceHandler;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		CoreBundleActivator.context = bundleContext;

		// Register the value-add OSGi service instance
		valueAddService = bundleContext.registerService(IValueAddService.class, ValueAddService.getInstance(), new Hashtable<String, String>());
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		CoreBundleActivator.context = null;
		// Mark the core framework as not started anymore
		Startup.setStarted(false);
		traceHandler = null;
		if (valueAddService != null) { valueAddService.unregister(); valueAddService = null; }
	}

}
