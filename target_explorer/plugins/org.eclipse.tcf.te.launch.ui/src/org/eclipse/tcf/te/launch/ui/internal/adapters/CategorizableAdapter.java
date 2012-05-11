/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.internal.adapters;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.te.launch.ui.model.LaunchModel;
import org.eclipse.tcf.te.launch.ui.model.LaunchNode;
import org.eclipse.tcf.te.ui.views.Managers;
import org.eclipse.tcf.te.ui.views.interfaces.ICategory;
import org.eclipse.tcf.te.ui.views.interfaces.IUIConstants;
import org.eclipse.tcf.te.ui.views.interfaces.categories.ICategorizable;

/**
 * Categorizable launch config node adapter implementation
 */
public class CategorizableAdapter implements ICategorizable {
	// Reference to the adapted element
	private final LaunchNode node;

	/**
	 * Constructor.
	 *
	 * @param node The adapted launch config node. Must not be <code>null</code>.
	 */
	public CategorizableAdapter(LaunchNode node) {
		Assert.isNotNull(node);
		this.node = node;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.interfaces.categories.ICategorizable#getId()
	 */
	@Override
	public String getId() {
		return LaunchModel.getCategoryId(node.getLaunchConfiguration());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.interfaces.categories.ICategorizable#isValid(org.eclipse.tcf.te.ui.views.interfaces.categories.ICategorizable.OPERATION, org.eclipse.tcf.te.ui.views.interfaces.ICategory, org.eclipse.tcf.te.ui.views.interfaces.ICategory)
	 */
	@Override
	public boolean isValid(OPERATION operation, ICategory parentCategory, ICategory category) {
		Assert.isNotNull(operation);
		Assert.isNotNull(category);

		if (OPERATION.REMOVE.equals(operation) && parentCategory != null && IUIConstants.ID_CAT_FAVORITES.equals(parentCategory.getId())) {
			return true;
		}
		if (OPERATION.ADD.equals(operation) && category != null && IUIConstants.ID_CAT_FAVORITES.equals(category.getId())) {
			return true;
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.interfaces.categories.ICategorizable#isEnabled(org.eclipse.tcf.te.ui.views.interfaces.categories.ICategorizable.OPERATION, org.eclipse.tcf.te.ui.views.interfaces.ICategory)
	 */
	@Override
	public boolean isEnabled(OPERATION operation, ICategory category) {
		Assert.isNotNull(operation);
		Assert.isNotNull(category);

		if (OPERATION.REMOVE.equals(operation)) {
			return Managers.getCategoryManager().belongsTo(IUIConstants.ID_CAT_FAVORITES, getId());
		}
		if (OPERATION.ADD.equals(operation)) {
			return !Managers.getCategoryManager().belongsTo(IUIConstants.ID_CAT_FAVORITES, getId());
		}
		return false;
	}
}
