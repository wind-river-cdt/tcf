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
			return (expectedValue != null ? expectedValue : Boolean.TRUE).equals(Boolean.valueOf(EditorPageBindingExtensionPointManager.getInstance().getApplicableEditorPageBindings(input).length > 0));
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

			return (expectedValue != null ? expectedValue : Boolean.TRUE).equals(Boolean.valueOf(refreshable));
		}

		if ("canRefresh".equals(property)) { //$NON-NLS-1$
			// Test if the receiver can be refreshed
			IRefreshHandlerDelegate delegate = receiver instanceof IRefreshHandlerDelegate ? (IRefreshHandlerDelegate)receiver : null;
			if (delegate == null) {
				delegate = (IRefreshHandlerDelegate)Platform.getAdapterManager().loadAdapter(receiver, IRefreshHandlerDelegate.class.getName());
			}

			boolean canRefresh = false;
			if (delegate != null) {
				canRefresh = delegate.canRefresh(receiver);
			}

			return (expectedValue != null ? expectedValue : Boolean.TRUE).equals(Boolean.valueOf(canRefresh));
		}

		return false;
	}

}
