/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.internal.adapters;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.tcf.te.ui.views.editor.Editor;
import org.eclipse.ui.forms.editor.IFormPage;

/**
 * The adapter factory that adapts the editor to its active tree viewer.
 */
public class EditorAdapterFactory implements IAdapterFactory {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if(adaptableObject instanceof Editor) {
			Editor editor = (Editor) adaptableObject;
			if(TreeViewer.class.equals(adapterType)) {
				IFormPage activePage = editor.getActivePageInstance();
				if(activePage != null) {
					return activePage.getAdapter(TreeViewer.class);
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	public Class[] getAdapterList() {
		return new Class[]{TreeViewer.class};
	}

}
