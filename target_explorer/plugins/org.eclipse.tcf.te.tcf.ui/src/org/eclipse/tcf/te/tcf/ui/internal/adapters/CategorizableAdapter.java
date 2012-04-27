/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.internal.adapters;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.ui.views.interfaces.categories.ICategorizable;

/**
 * Categorizable element adapter implementation
 */
public class CategorizableAdapter implements ICategorizable {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.interfaces.categories.ICategorizable#getId(java.lang.Object)
	 */
	@Override
	public String getId(Object element) {
		Assert.isNotNull(element);

		if (element instanceof IPeerModel) {
			return ((IPeerModel)element).getPeerId();
		}

	    return null;
	}
}
