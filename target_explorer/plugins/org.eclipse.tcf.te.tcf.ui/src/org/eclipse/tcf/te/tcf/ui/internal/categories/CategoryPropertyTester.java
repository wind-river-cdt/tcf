/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.internal.categories;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProperties;
import org.eclipse.tcf.te.tcf.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.ui.internal.preferences.IPreferenceConsts;
import org.eclipse.tcf.te.ui.views.Managers;
import org.eclipse.tcf.te.ui.views.interfaces.IUIConstants;

/**
 * Category property tester.
 */
public class CategoryPropertyTester extends PropertyTester {

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
	public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
		// The receiver is expected to be a peer model node
		if (receiver instanceof IPeerModel) {
			final AtomicBoolean result = new AtomicBoolean();

			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					result.set(testPeerModel((IPeerModel) receiver, property, args, expectedValue));
				}
			};

			if (Protocol.isDispatchThread()) runnable.run();
			else Protocol.invokeAndWait(runnable);

			return result.get();
		}
		return false;
	}

	/**
	 * Test the specific peer model node properties.
	 *
	 * @param node The model node. Must not be <code>null</code>.
	 * @param property The property to test.
	 * @param args The property arguments.
	 * @param expectedValue The expected value.
	 *
	 * @return <code>True</code> if the property to test has the expected value, <code>false</code>
	 *         otherwise.
	 */
	protected boolean testPeerModel(IPeerModel node, String property, Object[] args, Object expectedValue) {
		Assert.isNotNull(node);
		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$

		if ("parentCategoryId".equals(property)) { //$NON-NLS-1$
			String value = node.getStringProperty(IPeerModelProperties.PROP_PARENT_CATEGORY_ID);
			if (value == null && "NULL".equals(expectedValue)) { //$NON-NLS-1$
				return true;
			}

			// In copy mode, "Favorites" is special
			boolean isCopyMode = UIPlugin.getDefault().getPreferenceStore().getBoolean(IPreferenceConsts.PREF_FAVORITES_CATEGORY_MODE_COPY);
			if (isCopyMode && IUIConstants.ID_CAT_FAVORITES.equals(expectedValue)) {
				return Managers.getCategoryManager().belongsTo(IUIConstants.ID_CAT_FAVORITES, node.getPeerId());
			}

			return value != null && value.equals(expectedValue);
		}

		return false;
	}
}
