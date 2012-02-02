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

import org.eclipse.core.runtime.IAdaptable;

/**
 * A selection context providing the target context for the launch.
 */
public interface ITargetSelectionContext extends ISelectionContext {

	/**
	 * Returns the target context.
	 *
	 * @return The target context or <code>null</code>.
	 */
	public IAdaptable getTargetCtx();
}
