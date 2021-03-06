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
 * Persistable node property constants.
 */
public interface IPersistableNodeProperties {

	/**
	 * The URI of the node in a persistence storage.
	 * <p>
	 * This property can be used by persistable implementations to store the URI to remember from
	 * where a node got restored or written to.
	 * <p>
	 * The property itself is a transient property.
	 */
	public static final String PROPERTY_URI = "URI.transient"; //$NON-NLS-1$
}
