/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.tcf.te.tcf.launch.ui.remote.app.launchcontext;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tcf.te.launch.ui.tabs.launchcontext.AbstractContextSelectorControl;
import org.eclipse.tcf.te.tcf.ui.navigator.ContentProviderDelegate;
import org.eclipse.tcf.te.tcf.ui.navigator.LabelProviderDelegate;
import org.eclipse.tcf.te.ui.views.interfaces.ICategory;
import org.eclipse.tcf.te.ui.views.interfaces.IRoot;
import org.eclipse.tcf.te.ui.views.internal.ViewRoot;

/**
 * Locator model launch context selector control.
 */
@SuppressWarnings("restriction")
public class ContextSelectorControl extends AbstractContextSelectorControl {

	protected static class ContentProvider extends ContentProviderDelegate {
		private ITreeContentProvider catContentProvider = new org.eclipse.tcf.te.ui.views.navigator.ContentProviderDelegate();
		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof IRoot) {
				return catContentProvider.getElements(inputElement);
			}
			return super.getElements(inputElement);
		}
	}

	protected static class LabelProvider extends LabelProviderDelegate {
		private ILabelProvider catLabelProvider = new org.eclipse.tcf.te.ui.views.navigator.LabelProviderDelegate();
		@Override
		public String getText(Object element) {
			if (element instanceof ICategory) {
				return catLabelProvider.getText(element);
			}
			return super.getText(element);
		}
		@Override
		public Image getImage(Object element) {
			if (element instanceof ICategory) {
				return catLabelProvider.getImage(element);
			}
			return super.getImage(element);
		}
	}

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
		return ViewRoot.getInstance();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.tabs.launchcontext.AbstractContextSelectorControl#doConfigureTreeContentAndLabelProvider(org.eclipse.jface.viewers.TreeViewer)
	 */
	@Override
	protected void doConfigureTreeContentAndLabelProvider(TreeViewer viewer) {
		viewer.setContentProvider(new ContentProvider());
		LabelProvider labelProvider = new LabelProvider();
		viewer.setLabelProvider(new DecoratingLabelProvider(labelProvider, labelProvider));
	}
}
