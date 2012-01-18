/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.model.interfaces.services;

import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;

/**
 * Common interface to be implemented by a model refresh service.
 * <p>
 * <b>Note:</b> The refresh service API is designed to support asynchronous refresh operations. The
 * implementer of the service may however implement synchronous behavior. In any case, the method
 * callbacks must be invoked if given.
 */
public interface IModelRefreshService extends IModelService {

	/**
	 * Flag representing "no bits are set".
	 */
	public static final int NONE = 0x0;

	/**
	 * Flag to specify that only a refresh needs to be performed. No other operation besides the
	 * refresh, like activating a node, should be performed.
	 */
	public static final int REFRESH_ONLY = 0x1;

	/**
	 * Refresh the content of the model from the top.
	 *
	 * @param callback The callback to invoke once the refresh operation finished, or <code>null</code>.
	 */
	public void refresh(ICallback callback);

	/**
	 * Refresh the content of the model from the top using the specified flags.
	 *
	 * @param flags The flags. See the defined constants for details.
	 * @param callback The callback to invoke once the refresh operation finished, or <code>null</code>.
	 */
	public void refresh(int flags, ICallback callback);

	/**
	 * Full refresh the given node.
	 *
	 * @param node The node. Must not be <code>null</code>.
	 * @param callback The callback to invoke once the refresh operation finished, or <code>null</code>.
	 */
	public void refresh(IModelNode node, ICallback callback);

	/**
	 * Refresh the given node using the specified flags.
	 *
	 * @param node The node. Must not be <code>null</code>.
	 * @param flags The flags. See the defined constants for details.
	 * @param callback The callback to invoke once the refresh operation finished, or <code>null</code>.
	 */
	public void refresh(IModelNode node, int flags, ICallback callback);
}
