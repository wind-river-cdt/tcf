/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.persistence.interfaces;

/**
 * Interface to be implemented by persistable elements which requires a
 * custom persistence store naming schema.
 */
public interface IPersistableNameProvider {

	/**
	 * Returns the name to use to store the data object to the persistence storage.
	 * <p>
	 * <b>Note:</b> The name returned by this method is expected to be a valid name to the persistence
	 *              storage provider. The name will be passed on as returned.
	 *
	 * @param data The data object. Must not be <code>null</code>.
	 * @return The persistable name for the given data object or <code<null</code>.
	 */
	public String getName(Object data);
}
