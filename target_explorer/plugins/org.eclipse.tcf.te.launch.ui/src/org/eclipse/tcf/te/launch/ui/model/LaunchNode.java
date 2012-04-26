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

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.tcf.te.launch.core.lm.LaunchConfigHelper;
import org.eclipse.tcf.te.launch.core.lm.LaunchManager;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchManagerDelegate;
import org.eclipse.tcf.te.runtime.model.ContainerModelNode;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;

/**
 * LaunchNode
 */
public class LaunchNode extends ContainerModelNode {

	public static final String TYPE_ROOT = "root"; //$NON-NLS-1$
	public static final String TYPE_LAUNCH_CONFIG_TYPE = "launchConfigType"; //$NON-NLS-1$
	public static final String TYPE_LAUNCH_CONFIG = "launchConfig"; //$NON-NLS-1$

	private static final String PROPERTY_ROOT_MODEL_NODE = "modelNode"; //$NON-NLS-1$

	private LaunchNode(String type) {
		super();
		setProperty(IModelNode.PROPERTY_TYPE, type);
	}

	public LaunchNode(IModelNode modelNode) {
		this(TYPE_ROOT);
		setProperty(PROPERTY_ROOT_MODEL_NODE, modelNode);
	}

	public LaunchNode(ILaunchConfiguration config) {
		this(TYPE_LAUNCH_CONFIG);
		setProperty(TYPE_LAUNCH_CONFIG, config);
	}

	public LaunchNode(ILaunchConfigurationType configType) {
		this(TYPE_LAUNCH_CONFIG_TYPE);
		setProperty(TYPE_LAUNCH_CONFIG_TYPE, configType);
	}

	public String getType() {
		return getStringProperty(IModelNode.PROPERTY_TYPE);
	}

	public IModelNode getRootModelNode() {
		IModelNode node = (IModelNode)getProperty(PROPERTY_ROOT_MODEL_NODE);
		if (node == null && getParent() instanceof LaunchNode) {
			return ((LaunchNode)getParent()).getRootModelNode();
		}
		return node;
	}

	public ILaunchConfiguration getLaunchConfiguration() {
		if (TYPE_LAUNCH_CONFIG.equals(getType())) {
			return (ILaunchConfiguration)getProperty(TYPE_LAUNCH_CONFIG);
		}
		return null;
	}

	public ILaunchConfigurationType getLaunchConfigurationType() {
		if (TYPE_LAUNCH_CONFIG.equals(getType())) {
			return ((LaunchNode)getParent()).getLaunchConfigurationType();
		}
		else if (TYPE_LAUNCH_CONFIG_TYPE.equals(getType())) {
			return (ILaunchConfigurationType)getProperty(TYPE_LAUNCH_CONFIG_TYPE);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.ModelNode#getName()
	 */
	@Override
	public String getName() {
		if (TYPE_ROOT.equals(getType())) {
			return "Launches"; //$NON-NLS-1$
		}
		else if (TYPE_LAUNCH_CONFIG_TYPE.equals(getType())) {
			return getLaunchConfigurationType().getName();
		}
		else if (TYPE_LAUNCH_CONFIG.equals(getType())) {
			return getLaunchConfiguration().getName();
		}
		return super.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.properties.PropertiesContainer#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LaunchNode && getType() != null && getType().equals(((LaunchNode)obj).getType())) {
			if (TYPE_LAUNCH_CONFIG_TYPE.equals(getType())) {
				return getLaunchConfigurationType().equals(((LaunchNode)obj).getLaunchConfigurationType());
			}
			else if (TYPE_LAUNCH_CONFIG.equals(getType())) {
				return getLaunchConfiguration().equals(((LaunchNode)obj).getLaunchConfiguration());
			}
		}
		return super.equals(obj);
	}

	public boolean isValidFor(String mode) {
		if (TYPE_LAUNCH_CONFIG.equals(getType())) {
			List<String> modes;
			if (mode != null && mode.trim().length() > 0) {
				modes = new ArrayList<String>();
				modes.add(mode);
			}
			else {
				modes = Arrays.asList(LaunchConfigHelper.getLaunchConfigTypeModes(getLaunchConfigurationType(), false));
			}
			for (String m : modes) {
				ILaunchManagerDelegate delegate = LaunchManager.getInstance().getLaunchManagerDelegate(getLaunchConfigurationType(), m);
				if (delegate != null) {
					try {
						delegate.validate(mode, getLaunchConfiguration());
					}
					catch (Exception e) {
						return false;
					}
				}
			}
		}
		return true;
	}
}
