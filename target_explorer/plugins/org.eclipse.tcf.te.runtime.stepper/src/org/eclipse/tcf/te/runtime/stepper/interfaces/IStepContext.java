/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.stepper.interfaces;

import java.io.IOException;

import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNodeProvider;

/**
 * Interface to be implemented by objects representing a context for a step.
 */
public interface IStepContext extends IModelNodeProvider {

	/**
	 * Returns the context id.
	 *
	 * @return The context id or <code>null</code>.
	 */
	public String getId();

	/**
	 * Returns a name/label to be used within the UI to represent this context
	 * to the user.
	 *
	 * @return The name or <code>null</code>.
	 */
	public String getName();

	/**
	 * Returns a possible multi-line string providing detail information
	 * about the context which shall be included in failure messages.
	 *
	 * @param data The step data. Must not be <code>null</code>.
	 * @return The context information or <code>null</code>.
	 */
	public String getInfo(IPropertiesContainer data);

	/**
	 * Returns if or if not the associated model node really exist.
	 *
	 * @return <code>True</code> if the associated model node really exists, <code>false</code> otherwise.
	 */
	public boolean exists();

	/**
	 * Encodes the context to an persistable representation.
	 * <p>
	 * <b>Note:</b> The persistable representation is expected to be a single line.
	 *
	 * @return The persistable representation of the context.
	 */
	public String encode();

	/**
	 * Returns the class name of the encoded context.
	 * <p>
	 * The class name is used to load the correct class to decode the persistable representation.
	 *
	 * @return The class name of the encoded context.
	 */
	public String getEncodedClassName();

	/**
	 * Decodes the given persistable representation and store the result
	 * in the context.
	 *
	 * @param value The persistable representation of the context. Must not be <code>null</code>.
	 * @throws IOException - if the decode operation fails
	 */
	public void decode(String value) throws IOException;
}
