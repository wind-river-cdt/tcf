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

import java.io.IOException;

/**
 * Interface to be implemented by string persistable elements.
 */
public interface IPersistable2 extends IPersistable {

	/**
	 * Returns the class name of the encoded context.
	 * <p>
	 * The class name is used to load the correct class to decode the persistable representation.
	 *
	 * @param data The data object. Must not be <code>null</code>.
	 * @return The class name of the encoded context.
	 */
	public String getEncodedClassName(Object data);


	/**
	 * Exports the given data object to an external representation.
	 * <p>
	 * As a general guide line, it is expected that the external representation contains only base
	 * Java objects like maps, lists and Strings. Details about the valid object types can be taken
	 * from the referenced persistence delegate.
	 *
	 * @param data The data object. Must not be <code>null</code>.
	 * @return The external representation of the given data object.
	 *
	 * @throws IOException - if the operation fails.
	 */
	public String exportStringFrom(Object data) throws IOException;

	/**
	 * Imports the given external representation.
	 *
	 * @param external The external representation. Must not be <code>null</code>.
	 * @return The data object or <code>null</code>.
	 *
	 * @throws IOException - if the operation fails.
	 */
	public Object importFrom(String external) throws IOException;
}
