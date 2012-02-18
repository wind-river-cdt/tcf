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
}
