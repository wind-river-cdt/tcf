/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.internal.quickfilter;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.tcf.te.core.interfaces.IFilterable;
import org.eclipse.tcf.te.ui.views.editor.Editor;
import org.eclipse.ui.forms.editor.IFormPage;

/**
 * The tester to test if the current editor page is filterable.
 *
 */
public class EditorQuickFilterTester extends PropertyTester {

	public EditorQuickFilterTester() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof Editor && "isEditorFilterable".equals(property)) { //$NON-NLS-1$
			Editor editor = (Editor) receiver;
			IFormPage page = editor.getActivePageInstance();
			if (page != null) {
				TreeViewer viewer = (TreeViewer) page.getAdapter(TreeViewer.class);
				if (viewer != null) {
					Object input = viewer.getInput();
					return adaptFilterable(input) != null;
				}
			}
		}
		return false;
	}

	private IFilterable adaptFilterable(Object element) {
		IFilterable decorator = null;
		if (element instanceof IFilterable) {
			decorator = (IFilterable) element;
		}
		if (decorator == null && element instanceof IAdaptable) {
			decorator = (IFilterable) ((IAdaptable) element).getAdapter(IFilterable.class);
		}
		if (decorator == null) {
			decorator = (IFilterable) Platform.getAdapterManager()
			                .getAdapter(element, IFilterable.class);
		}
		return decorator;
	}

}
