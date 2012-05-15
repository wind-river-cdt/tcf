/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.expressions;

import org.eclipse.tcf.te.ui.views.editor.EditorInput;
import org.eclipse.tcf.te.ui.views.extensions.EditorPageBindingExtensionPointManager;
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

		return false;
	}

}
