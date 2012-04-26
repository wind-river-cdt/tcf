/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.tcf.te.launch.ui.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.tcf.te.launch.core.bindings.LaunchConfigTypeBindingsManager;
import org.eclipse.tcf.te.launch.core.persistence.launchcontext.LaunchContextsPersistenceDelegate;
import org.eclipse.tcf.te.launch.core.selection.LaunchSelection;
import org.eclipse.tcf.te.launch.core.selection.RemoteSelectionContext;
import org.eclipse.tcf.te.launch.ui.activator.UIPlugin;
import org.eclipse.tcf.te.runtime.events.ChangeEvent;
import org.eclipse.tcf.te.runtime.events.EventManager;
import org.eclipse.tcf.te.runtime.model.interfaces.IContainerModelNode;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.runtime.services.ServiceManager;
import org.eclipse.tcf.te.runtime.services.interfaces.IPropertiesAccessService;

/**
 * LaunchModel
 */
public final class LaunchModel {

	/* default */static final String LAUNCH_MODEL_KEY = UIPlugin.getUniqueIdentifier() + ".launch.root"; //$NON-NLS-1$

	/**
	 * Get the file system model of the peer model. If it does not
	 * exist yet, create a new instance and store it.
	 *
	 * @param peerModel The peer model
	 * @return The file system model connected this peer model.
	 */
	public static LaunchModel getLaunchModel(final IModelNode modelNode) {
		if (modelNode != null) {
			IPropertiesAccessService service = ServiceManager.getInstance().getService(modelNode, IPropertiesAccessService.class);
			LaunchModel model = service != null ? (LaunchModel)service.getProperty(modelNode, LAUNCH_MODEL_KEY) : (LaunchModel)modelNode.getProperty(LAUNCH_MODEL_KEY);
			if (model == null) {
				model = new LaunchModel(modelNode);
				if (service != null) {
					service.setProperty(modelNode, LAUNCH_MODEL_KEY, model);
				}
				else {
					modelNode.setProperty(LAUNCH_MODEL_KEY, model);
				}
			}
			return model;
		}
		return null;
	}

	private final LaunchNode root;

	/**
	 * Constructor.
	 */
	private LaunchModel(IModelNode node) {
		root = new LaunchNode(node);
		refresh();
		DebugPlugin.getDefault().getLaunchManager().addLaunchConfigurationListener(new ILaunchConfigurationListener() {
			@Override
			public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
				if (!configuration.isWorkingCopy()) {
					if (refresh()) {
						EventManager.getInstance().fireEvent(new ChangeEvent(LaunchModel.this, "launchRemoved", null, null)); //$NON-NLS-1$
					}
				}
			}
			@Override
			public void launchConfigurationChanged(ILaunchConfiguration configuration) {
				if (!configuration.isWorkingCopy()) {
					refresh();
					EventManager.getInstance().fireEvent(new ChangeEvent(LaunchModel.this, "launchChanged", null, null)); //$NON-NLS-1$
				}
			}
			@Override
			public void launchConfigurationAdded(ILaunchConfiguration configuration) {
				if (!configuration.isWorkingCopy()) {
					if (refresh()) {
						EventManager.getInstance().fireEvent(new ChangeEvent(LaunchModel.this, "launchAdded", null, null)); //$NON-NLS-1$
					}
				}
			}
		});
	}

	public LaunchNode getRoot() {
		return root;
	}

	public boolean refresh() {
		boolean changed = false;
		IModelNode parent = root.getRootModelNode();
		String[] typeIds = LaunchConfigTypeBindingsManager.getInstance().getValidLaunchConfigTypes(
						new LaunchSelection(null, new RemoteSelectionContext(parent, true)));
		List<IModelNode> typeNodes = new ArrayList<IModelNode>(Arrays.asList(root.getChildren()));
		for (String typeId : typeIds) {
			ILaunchConfigurationType type = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(typeId);
			if (type.isPublic()) {
				IModelNode typeNode = find(type, typeNodes);
				if (typeNode != null) {
					typeNodes.remove(typeNode);
				}
				else {
					typeNode = new LaunchNode(type);
					root.add(typeNode);
					changed = true;
				}

				ILaunchConfiguration[] configs;
				try {
					configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(type);
				}
				catch (Exception e) {
					configs = new ILaunchConfiguration[0];
				}

				List<IModelNode> configNodes = new ArrayList<IModelNode>(Arrays.asList(((IContainerModelNode)typeNode).getChildren()));
				for (ILaunchConfiguration config : configs) {
					IModelNode[] contexts = LaunchContextsPersistenceDelegate.getLaunchContexts(config);
					if (parent != null && (contexts == null || Arrays.asList(contexts).contains(parent))) {
						IModelNode configNode = find(config, configNodes);
						if (configNode != null) {
							configNodes.remove(configNode);
						}
						else {
							configNode = new LaunchNode(config);
							((IContainerModelNode)typeNode).add(configNode);
							changed = true;
						}
					}
				}
				for (IModelNode configToDelete : configNodes) {
					((IContainerModelNode)typeNode).remove(configToDelete, true);
					changed = true;
				}
			}
			for (IModelNode typeToDelete : typeNodes) {
				root.remove(typeToDelete, true);
				changed = true;
			}
		}
		return changed;
	}

	private IModelNode find(Object data, List<IModelNode> list) {
		for (IModelNode candidate : list) {
			if (candidate instanceof LaunchNode) {
				if ((data instanceof LaunchNode && ((LaunchNode)candidate).equals(data)) ||
								(data instanceof ILaunchConfiguration && ((LaunchNode)candidate).equals(new LaunchNode((ILaunchConfiguration)data))) ||
								(data instanceof ILaunchConfigurationType && ((LaunchNode)candidate).equals(new LaunchNode((ILaunchConfigurationType)data)))) {
					return candidate;
				}
			}
		}
		return null;
	}
}
