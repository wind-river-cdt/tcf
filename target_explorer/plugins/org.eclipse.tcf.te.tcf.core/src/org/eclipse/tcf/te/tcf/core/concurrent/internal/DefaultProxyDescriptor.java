/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.concurrent.internal;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tcf.te.tcf.core.concurrent.BlockingCallProxy;
import org.eclipse.tcf.te.tcf.core.concurrent.interfaces.IProxyDescriptor;

/**
 * The default proxy descriptor for proxy interfaces. It assumes the method with a parameter
 * whose type is an interface, and whose name has a segment of "$Done", is the proxy method.
 *
 * @see BlockingCallProxy
 * @see IProxyDescriptor
 */
public class DefaultProxyDescriptor implements IProxyDescriptor {
	// Descriptor cache to store known proxy descriptors.
	private static Map<Class<?>, IProxyDescriptor> descriptorCache;

	/**
	 * Get a default proxy descriptor from the cache using the class as the key.
	 * If no such proxy descriptor is found, then create a default proxy descriptor for it
	 * and cache it.
	 *
	 * @param proxyInterface The proxy interface.
	 * @return A default proxy descriptor for the proxy interface.
	 */
	public static IProxyDescriptor getProxyDescriptor(Class<?> proxyInterface) {
		if(descriptorCache == null) {
			descriptorCache = Collections.synchronizedMap(new HashMap<Class<?>, IProxyDescriptor>());
		}
		IProxyDescriptor descriptor = descriptorCache.get(proxyInterface);
		if(descriptor == null) {
			descriptor = new DefaultProxyDescriptor(proxyInterface);
			descriptorCache.put(proxyInterface, descriptor);
		}
		return descriptor;
	}

	// The callback map used to identify the proxy methods.
	private Map<Method, Integer> callbacks;
	/**
	 * Constructor using the proxy interface.
	 *
	 * @param proxyInterface The proxy interface.
	 */
	private DefaultProxyDescriptor(Class<?> proxyInterface) {
		callbacks = Collections.synchronizedMap(new HashMap<Method, Integer>());
		Method[] methods = proxyInterface.getDeclaredMethods();
		if(methods != null && methods.length > 0) {
			for(Method method : methods) {
				int index = findCallbackIndex(method);
				if(index != -1) {
					callbacks.put(method, Integer.valueOf(index));
				}
			}
		}
	}

	/**
	 * Find the index of the callback parameter in the specified method.
	 *
	 * @param method The method to be checked.
	 * @return the index of the callback parameter or -1 if there's no callback parameter.
	 */
	private int findCallbackIndex(Method method) {
		Class<?>[] pTypes = method.getParameterTypes();
		if (pTypes != null && pTypes.length > 0) {
			for (int i = 0; i < pTypes.length; i++) {
				Class<?> type = pTypes[i];
				if (type.isInterface() && type.getName().indexOf("$Done") != -1) return i; //$NON-NLS-1$
			}
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.core.util.IProxyDescriptor#isDispatchMethod(java.lang.reflect.Method)
	 */
	@Override
    public boolean isDispatchMethod(Method method) {
        return isProxyMethod(method);
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.core.util.IProxyDescriptor#isProxyMethod(java.lang.reflect.Method)
	 */
	@Override
    public boolean isProxyMethod(Method method) {
        return getCallbackIndex(method) != -1;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.core.util.IProxyDescriptor#getCallbackIndex(java.lang.reflect.Method)
	 */
	@Override
    public int getCallbackIndex(Method method) {
		Integer integer = callbacks.get(method);
        return integer == null ? -1 : integer.intValue();
    }
}
