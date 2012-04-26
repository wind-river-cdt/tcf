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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.te.launch.ui.tabs.launchcontext.AbstractContextSelectorControl;
import org.eclipse.tcf.te.launch.ui.tabs.launchcontext.AbstractContextSelectorSection;
import org.eclipse.ui.forms.IManagedForm;

/**
 * Locator model context selector section implementation.
 */
public class ContextSelectorSection extends AbstractContextSelectorSection {

	/**
	 * Constructor.
	 * @param form The managed form.
	 * @param parent The parent composite.
	 */
	public ContextSelectorSection(IManagedForm form, Composite parent) {
		super(form, parent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.tabs.launchcontext.AbstractContextSelectorSection#doCreateContextSelector()
	 */
	@Override
	protected AbstractContextSelectorControl doCreateContextSelector() {
		return new ContextSelectorControl(null) {
			/* (non-Javadoc)
			 * @see org.eclipse.tcf.te.launch.ui.tabs.launchcontext.AbstractContextSelectorControl#onCheckStateChanged(java.lang.Object, boolean)
			 */
			@Override
			protected void onCheckStateChanged(Object element, boolean checked) {
				super.onCheckStateChanged(element, checked);
				getManagedForm().dirtyStateChanged();
			}
		};
	}
}
