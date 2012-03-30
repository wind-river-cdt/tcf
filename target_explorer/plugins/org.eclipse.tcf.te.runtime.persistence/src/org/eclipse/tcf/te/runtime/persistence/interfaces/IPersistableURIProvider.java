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

import java.net.URI;

/**
 * Interface to be implemented by persistable elements.
 */
public interface IPersistableURIProvider {

	/**
	 * Returns the URI reference to pass on to the associated persistence delegate to
	 * denote the given context object.
	 * <p>
	 * The interpretation of the URI reference is up to the persistence delegate, but
	 * the method is expected to return never <code>null</code>.
	 *
	 * @param context The context object. Must not be <code>null</code>.
	 *
	 * @return The URI.
	 */
	public URI getURI(Object context);

}
