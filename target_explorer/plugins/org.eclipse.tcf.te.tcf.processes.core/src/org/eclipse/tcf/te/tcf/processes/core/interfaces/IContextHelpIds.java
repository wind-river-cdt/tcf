/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.core.interfaces;

import org.eclipse.tcf.te.tcf.processes.core.activator.CoreBundleActivator;

public interface IContextHelpIds {

	/**
	 * Common context help id prefix.
	 */
	public final static String PREFIX = CoreBundleActivator.getUniqueIdentifier() + "."; //$NON-NLS-1$

	/**
	 * Attach step: Operation failed.
	 */
	public final static String MESSAGE_ATTACH_FAILED = PREFIX + ".status.messageAttachFailed"; //$NON-NLS-1$

	/**
	 * Detach step: Operation failed.
	 */
	public final static String MESSAGE_DETACH_FAILED = PREFIX + ".status.messageDetachFailed"; //$NON-NLS-1$
}
