/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.concurrent.interfaces;

import java.lang.reflect.Method;

import org.eclipse.tcf.te.tcf.core.concurrent.BlockingCallProxy;
import org.eclipse.tcf.te.tcf.core.concurrent.internal.DefaultProxyDescriptor;

/**
 * The proxy descriptor to describe a proxy interface, providing which method should be
 * executed in the dispatching thread, which method is a proxy method that
 * should be handled by BlockingProxy and get the index of the callback of a specified
 * proxy method.
 *
 * @see BlockingCallProxy
 * @see DefaultProxyDescriptor
 */
public interface IProxyDescriptor {
	/**
	 * Check if the method should be called inside the dispatching thread. .
	 *
	 * @param method The method to be checked.
	 * @return true if it should be called inside the dispatching thread.
	 */
	boolean isDispatchMethod(Method method);

	/**
	 * Return if the method requires blocking invocation. The method requires blocking invocation if
	 * it has a callback parameter called in the dispatching thread.
	 *
	 * @param method The method to check.
	 * @return true if it requires a blocking invocation.
	 */
	boolean isProxyMethod(Method method);

	/**
	 * Get the index of the callback parameter.
	 *
	 * @param method The method to search the callback parameter.
	 * @return The callback paramter's index.
	 */
	int getCallbackIndex(Method method);
}
