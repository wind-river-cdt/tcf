/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.services.interfaces;

import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;

/**
 * Debug service.
 * <p>
 * Allow to start and control the debugger for a set of given debug contexts.
 */
public interface IDebugService extends IService {

	/**
	 * Launches a debug session for the given context and attaches to it. The attach
	 * can be parameterized via the data properties.
	 *
	 * @param context The debug context. Must not be <code>null</code>.
	 * @param data The data properties to parameterize the attach. Must not be <code>null</code>.
	 * @param callback The callback to invoke once the operation completed. Must not be <code>null</code>.
	 */
	public void attach(Object context, IPropertiesContainer data, ICallback callback);
}
