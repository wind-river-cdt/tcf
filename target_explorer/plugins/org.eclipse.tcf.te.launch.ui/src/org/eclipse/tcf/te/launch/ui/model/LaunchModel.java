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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.tcf.te.launch.core.bindings.LaunchConfigTypeBindingsManager;
import org.eclipse.tcf.te.launch.core.interfaces.IReferencedProjectItem;
import org.eclipse.tcf.te.launch.core.persistence.launchcontext.LaunchContextsPersistenceDelegate;
import org.eclipse.tcf.te.launch.core.persistence.projects.ReferencedProjectItem;
import org.eclipse.tcf.te.launch.core.persistence.projects.ReferencedProjectsPersistenceDelegate;
import org.eclipse.tcf.te.launch.core.selection.LaunchSelection;
import org.eclipse.tcf.te.launch.core.selection.ProjectSelectionContext;
import org.eclipse.tcf.te.launch.core.selection.RemoteSelectionContext;
import org.eclipse.tcf.te.runtime.events.ChangeEvent;
import org.eclipse.tcf.te.runtime.events.EventManager;
import org.eclipse.tcf.te.runtime.model.interfaces.IContainerModelNode;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;

/**
 * LaunchModel
 */
public final class LaunchModel {

	private static final Map<Object,LaunchModel> models = new HashMap<Object, LaunchModel>();

	/**
	 * Get the launch model of the rootNode.
	 * If it does not exist yet, create a new instance and store it.
	 *
	 * @param rootNode The rootNode of the model.
	 * @return The launch model.
	 */
	public static LaunchModel getLaunchModel(final Object modelRoot) {
		LaunchModel model = models.get(modelRoot);
		if (model == null) {
			model = new LaunchModel(modelRoot);
			models.put(modelRoot, model);
		}
		return model;
	}

	private final LaunchNode rootNode;
	private final Object modelRoot;

	/**
	 * Constructor.
	 */
	private LaunchModel(Object modelRoot) {
		this.modelRoot = modelRoot;
		rootNode = new LaunchNode(this);
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

	public LaunchNode getRootNode() {
		return rootNode;
	}

	public Object getModelRoot() {
		return modelRoot;
	}

	public boolean refresh() {
		boolean changed = false;
		Object parent = rootNode.getModel().getModelRoot();
		String[] typeIds = new String[0];
		if (parent instanceof IProject) {
			typeIds = LaunchConfigTypeBindingsManager.getInstance().getValidLaunchConfigTypes(
							new LaunchSelection(null, new ProjectSelectionContext((IProject)parent, true)));
		}
		else if (parent instanceof IModelNode) {
			typeIds = LaunchConfigTypeBindingsManager.getInstance().getValidLaunchConfigTypes(
							new LaunchSelection(null, new RemoteSelectionContext((IModelNode)parent, true)));
		}
		List<IModelNode> typeNodes = new ArrayList<IModelNode>(Arrays.asList(rootNode.getChildren()));
		for (String typeId : typeIds) {
			ILaunchConfigurationType type = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(typeId);
			if (type.isPublic()) {
				IModelNode typeNode = find(type, typeNodes);
				if (typeNode != null) {
					typeNodes.remove(typeNode);
				}
				else {
					typeNode = new LaunchNode(type);
					rootNode.add(typeNode);
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
					if (parent instanceof IModelNode) {
						IModelNode[] contexts = LaunchContextsPersistenceDelegate.getLaunchContexts(config);
						if (contexts == null || contexts.length == 0 || Arrays.asList(contexts).contains(parent)) {
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
					else if (parent instanceof IProject) {
						IReferencedProjectItem[] projects = ReferencedProjectsPersistenceDelegate.getReferencedProjects(config);
						IReferencedProjectItem project = new ReferencedProjectItem();
						project.setProperty(IReferencedProjectItem.PROPERTY_PROJECT_NAME, ((IProject)parent).getName());
						if (projects != null && Arrays.asList(projects).contains(project)) {
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
				}
				for (IModelNode configToDelete : configNodes) {
					((IContainerModelNode)typeNode).remove(configToDelete, true);
					changed = true;
				}
			}
			for (IModelNode typeToDelete : typeNodes) {
				rootNode.remove(typeToDelete, true);
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
