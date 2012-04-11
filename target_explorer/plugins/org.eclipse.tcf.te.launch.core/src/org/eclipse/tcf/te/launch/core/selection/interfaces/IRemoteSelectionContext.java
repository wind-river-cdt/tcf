/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.selection.interfaces;

import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;

/**
 * A selection context providing the remote context for the launch.
 */
public interface IRemoteSelectionContext extends ISelectionContext {

	/**
	 * Returns the remote context.
	 *
	 * @return The remote context or <code>null</code>.
	 */
	public IModelNode getRemoteCtx();
}
