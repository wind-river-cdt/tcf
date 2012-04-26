/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.tcf.te.tcf.launch.ui.remote.app;

import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.tcf.te.launch.core.lm.LaunchManager;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchManagerDelegate;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification;
import org.eclipse.tcf.te.launch.core.selection.interfaces.ILaunchSelection;
import org.eclipse.tcf.te.launch.ui.selection.LaunchSelectionManager;
import org.eclipse.tcf.te.tcf.launch.core.interfaces.ILaunchTypes;
import org.eclipse.tcf.te.tcf.launch.ui.activator.UIPlugin;
import org.eclipse.ui.IEditorPart;

/**
 * LaunchShortcut
 */
public class LaunchShortcut implements ILaunchShortcut2 {

	/**
	 * Constructor.
	 */
	public LaunchShortcut() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchShortcut#launch(org.eclipse.jface.viewers.ISelection, java.lang.String)
	 */
	@Override
	public void launch(ISelection selection, String mode) {
		ILaunchConfigurationType launchConfigType =	DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(ILaunchTypes.REMOTE_APPLICATION);
		try {
			ILaunchSelection launchSelection = LaunchSelectionManager.getInstance().getLaunchSelection(launchConfigType, mode, null);
			ILaunchManagerDelegate delegate = LaunchManager.getInstance().getLaunchManagerDelegate(launchConfigType, mode);
			if (delegate != null && launchSelection != null) {
				// create an empty launch configuration specification to initialize all attributes with their default defaults.
				ILaunchSpecification launchSpec = delegate.getLaunchSpecification(launchConfigType.getIdentifier(), launchSelection);
				// initialize the new launch config.
				// ignore validation result of launch spec - init as much attributes as possible
				if (launchSpec != null) {
					ILaunchConfiguration[] launchConfigs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(launchConfigType);
					launchConfigs = delegate.getMatchingLaunchConfigurations(launchSpec, launchConfigs);

					ILaunchConfiguration config = launchConfigs != null && launchConfigs.length > 0 ? launchConfigs[0] : null;
					config = LaunchManager.getInstance().createOrUpdateLaunchConfiguration(config, launchSpec);

					ILaunchGroup launchGroup = DebugUITools.getLaunchGroup(config, mode);
					DebugUITools.openLaunchConfigurationDialogOnGroup(UIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(), new StructuredSelection(config), launchGroup.getIdentifier());
				}
			}
		}
		catch (Exception e) {
			DebugUITools.openLaunchConfigurationDialogOnGroup(UIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(), null, null);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchShortcut#launch(org.eclipse.ui.IEditorPart, java.lang.String)
	 */
	@Override
	public void launch(IEditorPart editor, String mode) {
		launch((ISelection)null, mode);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchShortcut2#getLaunchConfigurations(org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(ISelection selection) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchShortcut2#getLaunchConfigurations(org.eclipse.ui.IEditorPart)
	 */
	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(IEditorPart editorpart) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchShortcut2#getLaunchableResource(org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public IResource getLaunchableResource(ISelection selection) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchShortcut2#getLaunchableResource(org.eclipse.ui.IEditorPart)
	 */
	@Override
	public IResource getLaunchableResource(IEditorPart editorpart) {
		return null;
	}

}
