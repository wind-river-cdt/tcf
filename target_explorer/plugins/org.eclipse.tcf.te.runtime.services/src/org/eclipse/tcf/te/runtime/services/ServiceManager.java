/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.services;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.runtime.activator.CoreBundleActivator;
import org.eclipse.tcf.te.runtime.services.interfaces.IService;
import org.eclipse.tcf.te.runtime.services.nls.Messages;
import org.osgi.framework.Bundle;

/**
 * Common service manager implementation, handling the extension point
 * <code>org.eclipse.tcf.te.runtime.services</code>.
 */
public class ServiceManager extends AbstractServiceManager {
	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstance {
		public static ServiceManager instance = new ServiceManager();
	}

	/**
	 * Constructor.
	 */
	ServiceManager() {
		super();
	}

	/**
	 * Returns the singleton instance of the service manager.
	 */
	public static ServiceManager getInstance() {
		return LazyInstance.instance;
	}

	/**
	 * Get a global unbound service that implements at least the needed service type.
	 *
	 * If an interface type is given, the service with the highest implementation is returned.
	 * This may result in a random selection depending on the extension registration order,
	 * especially when a service interface is implemented two times in different hierarchy paths.
	 *
	 * If a class type is given, if available, the service of exactly that class is returned.
	 * Otherwise the highest implementation is returned.
	 *
	 * @param serviceType The service type the service should at least implement or extend.
	 * @return The service or <code>null</code>.
	 */
	public <V extends IService> V getService(Class<? extends V> serviceType, boolean unique) {
		return super.getService(null, serviceType, unique);
	}

	/**
	 * Get a global unbound service that implements at least the needed service type.
	 *
	 * If an interface type is given, the service with the highest implementation is returned.
	 * This may result in a random selection depending on the extension registration order,
	 * especially when a service interface is implemented two times in different hierarchy paths.
	 *
	 * If a class type is given, if available, the service of exactly that class is returned.
	 * Otherwise the highest implementation is returned.
	 *
	 * @param serviceType The service type the service should at least implement or extend.
	 * @return The service or <code>null</code>.
	 */
	public <V extends IService> V getService(Class<? extends V> serviceType) {
		return super.getService(null, serviceType);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.services.AbstractServiceManager#loadServices()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void loadServices() {
		IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.tcf.te.runtime.services.services"); //$NON-NLS-1$
		if (ep != null) {
			IExtension[] extensions = ep.getExtensions();
			if (extensions != null) {
				for (IExtension extension : extensions) {
					IConfigurationElement[] configElements = extension.getConfigurationElements();
					if (configElements != null) {
						for (IConfigurationElement configElement : configElements) {
							if ("service".equals(configElement.getName())) { //$NON-NLS-1$
								ServiceProxy proxy = getServiceProxy(configElement);
								IConfigurationElement[] serviceTypes = configElement.getChildren("serviceType"); //$NON-NLS-1$
								if (serviceTypes != null && serviceTypes.length > 0) {
									for (IConfigurationElement serviceType : serviceTypes) {
										try {
											String type = serviceType.getAttribute("class"); //$NON-NLS-1$
											String bundleId = serviceType.getAttribute("bundleId"); //$NON-NLS-1$

											// If a bundle id got specified, use the specified bundle to load the service class
											Bundle bundle = bundleId != null ? bundle = Platform.getBundle(bundleId) : null;
											// If we don't have a bundle to load from yet, fallback to the declaring bundle
											if (bundle == null) bundle = Platform.getBundle(configElement.getDeclaringExtension().getNamespaceIdentifier());
											// And finally, use our own bundle to load the class.
											// This fallback is expected to never be used.
											if (bundle == null) bundle = CoreBundleActivator.getContext().getBundle();

											// Try to load the service type class now.
											Class<?> typeClass = bundle != null ? bundle.loadClass(type) : Class.forName(type);
											proxy.addType((Class<IService>)typeClass);
										}
										catch (Exception e) {
											IStatus status = new Status(IStatus.WARNING, CoreBundleActivator.getUniqueIdentifier(),
																		NLS.bind(Messages.ServiceManager_warning_failedToLoadServiceType, serviceType.getAttribute("class"), configElement.getAttribute("class")), e); //$NON-NLS-1$ //$NON-NLS-2$
											Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
										}
									}
								}
								addService(proxy);
							}
						}
					}
				}
			}
		}
	}

}
