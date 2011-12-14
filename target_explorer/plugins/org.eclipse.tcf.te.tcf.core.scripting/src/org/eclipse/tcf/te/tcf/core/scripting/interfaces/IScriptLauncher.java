/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.scripting.interfaces;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;

/**
 * Interface to be implemented by script launcher implementations.
 */
public interface IScriptLauncher extends IAdaptable {

	/**
	 * Executes a script defined by the given properties at the specified peer.
	 *
	 * @param peer The peer. Must not be <code>null</code>.
	 * @param params The script properties. Must not be <code>null</code>.
	 * @param callback The callback or <code>null</code>.
	 */
	public void launch(IPeer peer, IPropertiesContainer properties, ICallback callback);

	/**
	 * Disposes the script launcher instance.
	 */
	public void dispose();
}
