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
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.tcf.te.launch.core.bindings.LaunchConfigTypeBindingsManager;
import org.eclipse.tcf.te.launch.core.interfaces.IReferencedProjectItem;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ICommonLaunchAttributes;
import org.eclipse.tcf.te.launch.core.persistence.DefaultPersistenceDelegate;
import org.eclipse.tcf.te.launch.core.persistence.launchcontext.LaunchContextsPersistenceDelegate;
import org.eclipse.tcf.te.launch.core.persistence.projects.ReferencedProjectItem;
import org.eclipse.tcf.te.launch.core.persistence.projects.ReferencedProjectsPersistenceDelegate;
import org.eclipse.tcf.te.launch.core.selection.LaunchSelection;
import org.eclipse.tcf.te.launch.core.selection.ProjectSelectionContext;
import org.eclipse.tcf.te.launch.core.selection.RemoteSelectionContext;
import org.eclipse.tcf.te.runtime.events.ChangeEvent;
import org.eclipse.tcf.te.runtime.events.EventManager;
import org.eclipse.tcf.te.runtime.interfaces.events.IEventListener;
import org.eclipse.tcf.te.runtime.model.interfaces.IContainerModelNode;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.ui.views.Managers;
import org.eclipse.tcf.te.ui.views.interfaces.ICategory;

/**
 * LaunchModel
 */
public final class LaunchModel implements IEventListener, ILaunchConfigurationListener {

	private static final Map<Object,LaunchModel> models = new HashMap<Object, LaunchModel>();
	private static final Map<String,String> nameToUUID = new HashMap<String, String>();

	/**
	 * Get the launch model of the rootNode.
	 * If it does not exist yet, create a new instance and store it.
	 *
	 * @param rootNode The rootNode of the model.
	 * @return The launch model.
	 */
	public static LaunchModel getLaunchModel(final Object modelRoot) {
		if (modelRoot instanceof ICategory || modelRoot instanceof IModelNode || modelRoot instanceof IProject) {
			LaunchModel model = models.get(modelRoot);
			if (model == null) {
				model = new LaunchModel(modelRoot);
				models.put(modelRoot, model);
			}
			return model;
		}
		return null;
	}

	private final LaunchNode rootNode;
	private final Object modelRoot;

	private String lastAddedUUID = null;

	/**
	 * Constructor.
	 */
	private LaunchModel(Object modelRoot) {
		Assert.isNotNull(modelRoot);
		this.modelRoot = modelRoot;
		rootNode = new LaunchNode(this);
		refresh();
		DebugPlugin.getDefault().getLaunchManager().addLaunchConfigurationListener(this);

		if (modelRoot instanceof ICategory) {
			EventManager.getInstance().addEventListener(this, ChangeEvent.class);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationRemoved(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
		if (!configuration.isWorkingCopy()) {
			if (getModelRoot() instanceof ICategory) {
				if (!getCategoryId(configuration).equals(lastAddedUUID)) {
					Managers.getCategoryManager().remove(((ICategory)getModelRoot()).getId(), getCategoryId(configuration));
				}
				nameToUUID.remove(configuration.getName());
				lastAddedUUID = null;
			}
			if (refresh()) {
				EventManager.getInstance().fireEvent(new ChangeEvent(this, ChangeEvent.ID_REMOVED, null, null));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationChanged(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public void launchConfigurationChanged(ILaunchConfiguration configuration) {
		if (!configuration.isWorkingCopy()) {
			refresh();
			EventManager.getInstance().fireEvent(new ChangeEvent(this, ChangeEvent.ID_CHANGED, null, null));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationAdded(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public void launchConfigurationAdded(ILaunchConfiguration configuration) {
		if (!configuration.isWorkingCopy()) {
			if (getModelRoot() instanceof ICategory) {
				if (Managers.getCategoryManager().belongsTo(((ICategory)getModelRoot()).getId(), getCategoryId(configuration))) {
					lastAddedUUID = getCategoryId(configuration);
				}
			}
			if (refresh()) {
				EventManager.getInstance().fireEvent(new ChangeEvent(this, ChangeEvent.ID_ADDED, null, null));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.events.IEventListener#eventFired(java.util.EventObject)
	 */
	@Override
	public void eventFired(EventObject event) {
		Assert.isTrue(event instanceof ChangeEvent);
		ChangeEvent e = (ChangeEvent)event;
		if (e.getSource() instanceof ICategory &&
						((e.getNewValue() instanceof String && ((String)e.getNewValue()).startsWith(LaunchNode.class.getName())) ||
										(e.getOldValue() instanceof String && ((String)e.getOldValue()).startsWith(LaunchNode.class.getName())))) {
			if (refresh()) {
				EventManager.getInstance().fireEvent(new ChangeEvent(this, ((ChangeEvent)event).getEventId(), null, null));
			}
		}
	}

	/**
	 * Return the root node of the model tree.
	 */
	public LaunchNode getRootNode() {
		return rootNode;
	}

	/**
	 * Return the model root (IModelNode, IProject, ICategory).
	 */
	public Object getModelRoot() {
		return modelRoot;
	}

	/**
	 * Refresh the model.
	 * @return <code>true</code> if the model has changed.
	 */
	public boolean refresh() {
		boolean changed = false;
		Object parent = rootNode.getModel().getModelRoot();
		String[] typeIds = new String[0];
		if (parent instanceof ICategory) {
			List<String> ids = new ArrayList<String>();
			for (ILaunchConfigurationType type : DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationTypes()) {
				ids.add(type.getIdentifier());
			}
			typeIds = ids.toArray(new String[ids.size()]);
		}
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
					if (parent instanceof ICategory) {
						if (Managers.getCategoryManager().belongsTo(((ICategory)parent).getId(), getCategoryId(config))) {
							changed |= checkAndAdd(config, (IContainerModelNode)typeNode, configNodes);
						}
					}
					else if (parent instanceof IModelNode) {
						IModelNode[] contexts = LaunchContextsPersistenceDelegate.getLaunchContexts(config);
						if (contexts != null && Arrays.asList(contexts).contains(parent)) {
							changed |= checkAndAdd(config, (IContainerModelNode)typeNode, configNodes);
						}
					}
					else if (parent instanceof IProject) {
						IReferencedProjectItem[] projects = ReferencedProjectsPersistenceDelegate.getReferencedProjects(config);
						IReferencedProjectItem project = new ReferencedProjectItem();
						project.setProperty(IReferencedProjectItem.PROPERTY_PROJECT_NAME, ((IProject)parent).getName());
						if (projects != null && Arrays.asList(projects).contains(project)) {
							changed |= checkAndAdd(config, (IContainerModelNode)typeNode, configNodes);
						}
					}
				}
				for (IModelNode configToDelete : configNodes) {
					((IContainerModelNode)typeNode).remove(configToDelete, true);
					changed = true;
				}
				if (parent instanceof ICategory && typeNode.isEmpty()) {
					typeNodes.add(typeNode);
				}
			}
		}
		for (IModelNode typeToDelete : typeNodes) {
			rootNode.remove(typeToDelete, true);
			changed = true;
		}
		return changed;
	}

	private boolean checkAndAdd(ILaunchConfiguration config, IContainerModelNode typeNode, List<IModelNode> configNodes) {
		IModelNode configNode = find(config, configNodes);
		if (configNode != null) {
			configNodes.remove(configNode);
		}
		else {
			configNode = new LaunchNode(config);
			typeNode.add(configNode);
			return true;
		}
		return false;
	}

	private IModelNode find(ILaunchConfiguration config, List<IModelNode> list) {
		for (IModelNode candidate : list) {
			if (candidate instanceof LaunchNode) {
				LaunchNode node = new LaunchNode(config);
				node.setProperty(LaunchNode.PROPERTY_MODEL, this);
				if (((LaunchNode)candidate).equals(node)) {
					return candidate;
				}
			}
		}
		return null;
	}

	private IModelNode find(ILaunchConfigurationType type, List<IModelNode> list) {
		for (IModelNode candidate : list) {
			if (candidate instanceof LaunchNode) {
				LaunchNode node = new LaunchNode(type);
				node.setProperty(LaunchNode.PROPERTY_MODEL, this);
				if (((LaunchNode)candidate).equals(node)) {
					return candidate;
				}
			}
		}
		return null;
	}

	/**
	 * Get the unique category id for this launch config.
	 * @param config The launch configuration.
	 * @return The unique category id.
	 */
	public static String getCategoryId(ILaunchConfiguration config) {
		String uuid = DefaultPersistenceDelegate.getAttribute(config, ICommonLaunchAttributes.ATTR_UUID, (String)null);
		if (uuid == null) {
			uuid = nameToUUID.get(config.getName());
		}
		if (uuid != null && !nameToUUID.containsKey(config.getName())) {
			nameToUUID.put(config.getName(), uuid);
		}

		return LaunchNode.class.getName() + "." + (uuid != null ? uuid : config.getName()); //$NON-NLS-1$
	}
}
