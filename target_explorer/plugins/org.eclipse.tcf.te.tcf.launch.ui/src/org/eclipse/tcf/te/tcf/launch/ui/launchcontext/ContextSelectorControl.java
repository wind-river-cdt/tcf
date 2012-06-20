/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.tcf.te.tcf.launch.ui.launchcontext;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.tcf.te.launch.ui.tabs.launchcontext.AbstractContextSelectorControl;
import org.eclipse.tcf.te.tcf.locator.model.Model;
import org.eclipse.tcf.te.tcf.ui.navigator.ContentProviderDelegate;
import org.eclipse.tcf.te.tcf.ui.navigator.LabelProviderDelegate;

/**
 * Locator model launch context selector control.
 */
public class ContextSelectorControl extends AbstractContextSelectorControl {


	/**
	 * Constructor.
	 * @param parentPage
	 */
	public ContextSelectorControl(IDialogPage parentPage) {
		super(parentPage);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.tabs.launchcontext.AbstractContextSelectorControl#getInitialViewerInput()
	 */
	@Override
	protected Object getInitialViewerInput() {
		return Model.getModel();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.tabs.launchcontext.AbstractContextSelectorControl#doConfigureTreeContentAndLabelProvider(org.eclipse.jface.viewers.TreeViewer)
	 */
	@Override
	protected void doConfigureTreeContentAndLabelProvider(TreeViewer viewer) {
		viewer.setContentProvider(new ContentProviderDelegate());
		LabelProviderDelegate labelProvider = new LabelProviderDelegate();
		viewer.setLabelProvider(new DecoratingLabelProvider(labelProvider, labelProvider));
	}
}
