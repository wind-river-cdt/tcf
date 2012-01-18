/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.model.services;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.tcf.te.tcf.core.model.interfaces.IModel;
import org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelService;

/**
 * Abstract model service implementation.
 */
public abstract class AbstractModelService<V extends IModel> extends PlatformObject implements IModelService {
	// Reference to the parent model
	private final V model;

	/**
	 * Constructor.
	 *
	 * @param model The parent model. Must not be <code>null</code>.
	 */
	public AbstractModelService(V model) {
		super();
		Assert.isNotNull(model);
		this.model = model;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelService#getModel()
	 */
	@Override
	public final V getModel() {
		return model;
	}

}
