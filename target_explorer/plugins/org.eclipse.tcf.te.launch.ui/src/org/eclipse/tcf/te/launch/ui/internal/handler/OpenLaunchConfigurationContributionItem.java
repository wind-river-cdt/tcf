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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.tcf.te.launch.core.lm.LaunchConfigHelper;
import org.eclipse.tcf.te.launch.ui.activator.UIPlugin;
import org.eclipse.tcf.te.launch.ui.model.LaunchNode;
import org.eclipse.ui.ISources;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

/**
 * LaunchLaunchConfigurationContributionItem
 */
public class OpenLaunchConfigurationContributionItem extends CompoundContributionItem implements IWorkbenchContribution {

	private boolean enabled = true;

	// Service locator to located the handler service.
	private IServiceLocator serviceLocator;

	/**
	 * Constructor.
	 */
	public OpenLaunchConfigurationContributionItem() {
		super();
	}

	/**
	 * Constructor.
	 * @param id
	 */
	public OpenLaunchConfigurationContributionItem(String id) {
		super(id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.menus.IWorkbenchContribution#initialize(org.eclipse.ui.services.IServiceLocator)
	 */
	@Override
	public void initialize(IServiceLocator serviceLocator) {
		this.serviceLocator = serviceLocator;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.ContributionItem#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.CompoundContributionItem#getContributionItems()
	 */
	@Override
	protected IContributionItem[] getContributionItems() {
		// Get the selected node.
		IHandlerService service = (IHandlerService)serviceLocator.getService(IHandlerService.class);
		IEvaluationContext state = service.getCurrentState();
		ISelection selection = (ISelection)state.getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME);
		IStructuredSelection iss = (IStructuredSelection)selection;
		Object obj = iss.getFirstElement();
		Assert.isTrue(obj instanceof LaunchNode);
		final LaunchNode node = (LaunchNode) obj;
		List<IContributionItem> items = new ArrayList<IContributionItem>();
		final ILaunchConfigurationType type = node.getLaunchConfigurationType();
		final ILaunchConfiguration config;
		final boolean openConfig;
		if (node.getLaunchConfiguration() != null) {
			openConfig = true;
			config = node.getLaunchConfiguration();
		}
		else {
			openConfig = false;
			ILaunchConfiguration newConfig = null;
			try {
				newConfig = type.newInstance(null, "temp"); //$NON-NLS-1$
			}
			catch (Exception e) {
			}
			config = newConfig;
		}
		if (type != null && config != null) {
			try {
				for (String mode : LaunchConfigHelper.getLaunchConfigTypeModes(type, false)) {
					final ILaunchGroup launchGroup = DebugUITools.getLaunchGroup(config, mode);
					ILaunchMode launchMode = DebugPlugin.getDefault().getLaunchManager().getLaunchMode(mode);
					IAction action = new Action() {
						@Override
						public void run() {
							DebugUITools.openLaunchConfigurationDialogOnGroup(
											UIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(),
											new StructuredSelection(openConfig ? config : type),
											launchGroup.getIdentifier());
						}
					};
					action.setText(launchMode.getLabel() + " Configuration" + (openConfig ? "" : "s") + "..."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					action.setImageDescriptor(launchGroup.getImageDescriptor());
					items.add(new ActionContributionItem(action));
				}
			}
			catch (Exception e) {
			}
		}

		enabled = !items.isEmpty();
		return items.toArray(new IContributionItem[items.size()]);
	}

}
