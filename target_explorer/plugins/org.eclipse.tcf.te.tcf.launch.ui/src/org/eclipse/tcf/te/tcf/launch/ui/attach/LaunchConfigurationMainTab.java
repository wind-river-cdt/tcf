/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.launch.ui.attach;

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.te.launch.ui.tabs.launchcontext.AbstractContextSelectorTab;
import org.eclipse.tcf.te.tcf.launch.core.interfaces.ILaunchTypes;
import org.eclipse.tcf.te.tcf.launch.ui.remote.app.launchcontext.ContextSelectorSection;
import org.eclipse.tcf.te.ui.forms.CustomFormToolkit;
import org.eclipse.ui.forms.IManagedForm;

/**
 * Remote application main launch tab implementation.
 */
public class LaunchConfigurationMainTab extends AbstractContextSelectorTab {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.tabs.launchcontext.AbstractContextSelectorTab#doCreateContextSelectorSection(org.eclipse.ui.forms.IManagedForm, org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected ContextSelectorSection doCreateContextSelectorSection(IManagedForm form, Composite panel) {
		return new ContextSelectorSection(form, panel);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.tabs.launchcontext.AbstractContextSelectorTab#doCreateAdditionalFormContent(org.eclipse.ui.forms.IManagedForm, org.eclipse.swt.widgets.Composite, org.eclipse.tcf.te.ui.forms.CustomFormToolkit)
	 */
	@Override
	protected void doCreateAdditionalFormContent(IManagedForm form, Composite parent, CustomFormToolkit toolkit) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
	 */
	@Override
	public Image getImage() {
		return DebugUITools.getImage(ILaunchTypes.ATTACH);
	}
}
