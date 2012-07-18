/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.launch.ui.editor;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.tcf.internal.debug.ui.launch.TCFMemoryMapTab;

/**
 * TCF memory map launch configuration tab container page implementation.
 */
public class MemoryMapEditorPage extends AbstractTcfLaunchTabContainerEditorPage {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.editor.AbstractLaunchTabContainerEditorPage#createLaunchConfigurationTab()
	 */
	@Override
	protected AbstractLaunchConfigurationTab createLaunchConfigurationTab() {
		return new TCFMemoryMapTab() {
			@Override
			protected void updateLaunchConfigurationDialog() {
				super.updateLaunchConfigurationDialog();
				performApply(getLaunchConfig(getPeerModel(getEditorInput())));
				checkLaunchConfigDirty();
			}
		};
	}
}
