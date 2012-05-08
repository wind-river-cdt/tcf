/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.tcf.te.launch.ui.internal.handler;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.tcf.te.launch.core.lm.LaunchManager;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchManagerDelegate;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification;
import org.eclipse.tcf.te.launch.core.selection.interfaces.ILaunchSelection;
import org.eclipse.tcf.te.launch.ui.activator.UIPlugin;
import org.eclipse.tcf.te.launch.ui.selection.LaunchSelectionManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * LaunchShortcutHandler
 */
public class LaunchShortcutHandler extends AbstractHandler implements ILaunchShortcut, IExecutableExtension {

	private String mode = null;
	private String typeId = null;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchShortcut#launch(org.eclipse.jface.viewers.ISelection, java.lang.String)
	 */
	@Override
	public void launch(ISelection selection, String mode) {
		Assert.isNotNull(typeId);
		ILaunchConfigurationType launchConfigType =	DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(typeId);
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
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		if (data != null && data instanceof Map) {
			String launchMode = (String)((Map<?,?>)data).get("mode"); //$NON-NLS-1$
			if (launchMode != null) {
				mode = launchMode;
			}
			String launchTypeId = (String)((Map<?,?>)data).get("typeId"); //$NON-NLS-1$
			if (launchTypeId != null) {
				typeId = launchTypeId;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Assert.isNotNull(mode);
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		launch(selection, mode);
		return null;
	}
}
