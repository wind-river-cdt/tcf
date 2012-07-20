/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.tcf.te.launch.ui.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IProject;
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
import org.eclipse.tcf.te.launch.core.lm.LaunchManager;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchManagerDelegate;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification;
import org.eclipse.tcf.te.launch.core.selection.LaunchSelection;
import org.eclipse.tcf.te.launch.core.selection.ProjectSelectionContext;
import org.eclipse.tcf.te.launch.core.selection.RemoteSelectionContext;
import org.eclipse.tcf.te.launch.core.selection.interfaces.ILaunchSelection;
import org.eclipse.tcf.te.launch.core.selection.interfaces.ISelectionContext;
import org.eclipse.tcf.te.launch.ui.activator.UIPlugin;
import org.eclipse.tcf.te.launch.ui.model.LaunchNode;
import org.eclipse.tcf.te.launch.ui.selection.LaunchSelectionManager;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.ui.views.interfaces.ICategory;
import org.eclipse.ui.ISources;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

/**
 * LaunchLaunchConfigurationContributionItem
 */
public class NewLaunchConfigurationContributionItem extends CompoundContributionItem implements IWorkbenchContribution {

	private boolean enabled = true;

	// Service locator to located the handler service.
	private IServiceLocator serviceLocator;

	/**
	 * Constructor.
	 */
	public NewLaunchConfigurationContributionItem() {
		super();
	}

	/**
	 * Constructor.
	 * @param id
	 */
	public NewLaunchConfigurationContributionItem(String id) {
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
		List<IContributionItem> items = new ArrayList<IContributionItem>();
		if (obj instanceof LaunchNode) {
			final LaunchNode node = (LaunchNode) obj;
			final ILaunchConfigurationType type = node.getLaunchConfigurationType();
			if (type != null) {
				try {
					for (String mode : LaunchConfigHelper.getLaunchConfigTypeModes(type, false)) {
						ILaunchManagerDelegate delegate = LaunchManager.getInstance().getLaunchManagerDelegate(type, mode);
						ILaunchSelection launchSelection = null;
						if (node.getModel().getModelRoot() instanceof ICategory) {
							launchSelection = LaunchSelectionManager.getInstance().getLaunchSelection(type, mode, LaunchSelectionManager.PART_ID_TE_VIEW);
						}
						else if (node.getModel().getModelRoot() instanceof IModelNode) {
							List<ISelectionContext> selectionContexts = new ArrayList<ISelectionContext>();
							selectionContexts.add(new RemoteSelectionContext((IModelNode)node.getModel().getModelRoot(), true));
							selectionContexts.addAll(LaunchSelectionManager.getInstance().getSelectionContextsFor(LaunchSelectionManager.PART_ID_PROJECT_VIEW, type, mode, false));
							launchSelection = new LaunchSelection(mode, selectionContexts.toArray(new ISelectionContext[selectionContexts.size()]));
						}
						else if (node.getModel().getModelRoot() instanceof IProject) {
							List<ISelectionContext> selectionContexts = new ArrayList<ISelectionContext>();
							selectionContexts.add(new ProjectSelectionContext((IProject)node.getModel().getModelRoot(), true));
							selectionContexts.addAll(LaunchSelectionManager.getInstance().getSelectionContextsFor(LaunchSelectionManager.PART_ID_TE_VIEW, type, mode, false));
							launchSelection = new LaunchSelection(mode, selectionContexts.toArray(new ISelectionContext[selectionContexts.size()]));
						}
						if (launchSelection != null) {
							final ILaunchSpecification launchSpec = delegate.getLaunchSpecification(type.getIdentifier(), launchSelection);
							final ILaunchGroup launchGroup = DebugUITools.getLaunchGroup(type.newInstance(null, "temp"), mode); //$NON-NLS-1$
							ILaunchMode launchMode = DebugPlugin.getDefault().getLaunchManager().getLaunchMode(mode);
							IAction action = new Action() {
								@Override
								public void run() {
									try {
										ILaunchConfiguration config = LaunchManager.getInstance().createOrUpdateLaunchConfiguration(null, launchSpec);
										DebugUITools.openLaunchConfigurationDialogOnGroup(
														UIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(),
														new StructuredSelection(config),
														launchGroup.getIdentifier());
									}
									catch (Exception e) {
										e.printStackTrace();
									}
								}
							};
							action.setText(launchMode.getLabel() + " Configuration"); //$NON-NLS-1$
							action.setImageDescriptor(launchGroup.getImageDescriptor());
							items.add(new ActionContributionItem(action));
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		enabled = !items.isEmpty();
		return items.toArray(new IContributionItem[items.size()]);
	}

}
