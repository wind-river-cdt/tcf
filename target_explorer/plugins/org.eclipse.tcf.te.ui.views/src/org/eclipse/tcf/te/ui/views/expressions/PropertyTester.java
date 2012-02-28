/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.expressions;

import org.eclipse.core.runtime.Platform;
import org.eclipse.tcf.te.ui.views.editor.EditorInput;
import org.eclipse.tcf.te.ui.views.extensions.EditorPageBindingExtensionPointManager;
import org.eclipse.tcf.te.ui.views.interfaces.ICategory;
import org.eclipse.tcf.te.ui.views.interfaces.IUIConstants;
import org.eclipse.tcf.te.ui.views.interfaces.handler.IDeleteHandlerDelegate;
import org.eclipse.tcf.te.ui.views.interfaces.handler.IRefreshHandlerDelegate;
import org.eclipse.ui.IEditorInput;


/**
 * Property tester implementation.
 */
public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

		if ("hasApplicableEditorBindings".equals(property)) { //$NON-NLS-1$
			// Create a fake editor input object
			IEditorInput input = new EditorInput(receiver);
			return expectedValue.equals(Boolean.valueOf(EditorPageBindingExtensionPointManager.getInstance().getApplicableEditorPageBindings(input).length > 0));
		}

		if ("isRefreshableElement".equals(property)) { //$NON-NLS-1$
			// An element is refreshable if it implements or adapt to IRefreshHandlerDelegate
			//
			// Note: This test will force the load of the adapter.

			boolean refreshable = receiver instanceof IRefreshHandlerDelegate;
			if (!refreshable) {
				IRefreshHandlerDelegate delegate = (IRefreshHandlerDelegate)Platform.getAdapterManager().loadAdapter(receiver, IRefreshHandlerDelegate.class.getName());
				refreshable = delegate != null;
			}

			return expectedValue.equals(Boolean.valueOf(refreshable));
		}

		if ("canRefresh".equals(property)) { //$NON-NLS-1$
			// Test if the receiver can be refreshed
			IRefreshHandlerDelegate delegate = receiver instanceof IRefreshHandlerDelegate ? (IRefreshHandlerDelegate)receiver : null;
			if (delegate == null) delegate = (IRefreshHandlerDelegate)Platform.getAdapterManager().loadAdapter(receiver, IRefreshHandlerDelegate.class.getName());

			boolean canRefresh = false;
			if (delegate != null) canRefresh = delegate.canRefresh(receiver);

			return expectedValue.equals(Boolean.valueOf(canRefresh));
		}


		if ("isDeletableElement".equals(property)) { //$NON-NLS-1$
			// An element is deletable if it implements or adapt to IDeleteHandlerDelegate
			//
			// Note: This test will force the load of the adapter.

			boolean deletable = receiver instanceof IDeleteHandlerDelegate;
			if (!deletable) {
				IDeleteHandlerDelegate delegate = (IDeleteHandlerDelegate)Platform.getAdapterManager().loadAdapter(receiver, IDeleteHandlerDelegate.class.getName());
				deletable = delegate != null;
			}

			return expectedValue.equals(Boolean.valueOf(deletable));
		}

		if ("canDelete".equals(property)) { //$NON-NLS-1$
			// Test if the receiver can be deleted
			IDeleteHandlerDelegate delegate = receiver instanceof IDeleteHandlerDelegate ? (IDeleteHandlerDelegate)receiver : null;
			if (delegate == null) delegate = (IDeleteHandlerDelegate)Platform.getAdapterManager().loadAdapter(receiver, IDeleteHandlerDelegate.class.getName());

			boolean canDelete = false;
			if (delegate != null) canDelete = delegate.canDelete(receiver);

			return expectedValue.equals(Boolean.valueOf(canDelete));
		}

		// ***** Categories related properties *****

		if ("isCategoryID".equals(property) && receiver instanceof ICategory) { //$NON-NLS-1$
			String id = ((ICategory)receiver).getId();
			return id.equals(expectedValue);
		}

		if ("isMyTargets".equals(property) && receiver instanceof ICategory) { //$NON-NLS-1$
			String id = ((ICategory)receiver).getId();
			return expectedValue.equals(Boolean.valueOf(id.equals(IUIConstants.ID_CAT_MY_TARGETS)));
		}

		if ("isFavorites".equals(property) && receiver instanceof ICategory) { //$NON-NLS-1$
			String id = ((ICategory)receiver).getId();
			return expectedValue.equals(Boolean.valueOf(id.equals(IUIConstants.ID_CAT_FAVORITES)));
		}

		if ("isNeighborhood".equals(property) && receiver instanceof ICategory) { //$NON-NLS-1$
			String id = ((ICategory)receiver).getId();
			return expectedValue.equals(Boolean.valueOf(id.equals(IUIConstants.ID_CAT_NEIGHBORHOOD)));
		}

		return false;
	}

}
