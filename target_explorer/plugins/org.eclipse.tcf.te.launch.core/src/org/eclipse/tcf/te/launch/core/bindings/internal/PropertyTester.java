/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.bindings.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.tcf.te.launch.core.bindings.LaunchConfigTypeBindingsManager;
import org.eclipse.tcf.te.launch.core.selection.LaunchSelection;
import org.eclipse.tcf.te.launch.core.selection.ProjectSelectionContext;
import org.eclipse.tcf.te.launch.core.selection.RemoteSelectionContext;
import org.eclipse.tcf.te.launch.core.selection.interfaces.ISelectionContext;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNodeProvider;

/**
 * Launch property tester.
 */
public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
	public boolean test(final Object receiver, String property, Object[] args, Object expectedValue) {

		if ("launchMode".equals(property) && expectedValue instanceof String) { //$NON-NLS-1$
			if (receiver instanceof ILaunch) {
				return ((ILaunch)receiver).getLaunchMode().equalsIgnoreCase((String)expectedValue);
			}
		}
		else if ("isValidLaunchConfigType".equals(property) && expectedValue instanceof String) { //$NON-NLS-1$
			ISelectionContext selContext = null;
			if (receiver instanceof IModelNodeProvider) {
				selContext = new RemoteSelectionContext(((IModelNodeProvider)receiver).getModelNode(), true);
			}
			else if (receiver instanceof IResource) {
				selContext = new ProjectSelectionContext(((IResource)receiver).getProject(), true);
			}
			else if (receiver instanceof IProject) {
				selContext = new ProjectSelectionContext((IProject)receiver, true);
			}
			else if (receiver instanceof IAdaptable) {
				IProject project = (IProject)((IAdaptable)receiver).getAdapter(IProject.class);
				if (project != null) {
					selContext = new ProjectSelectionContext(project, true);
				}
				else {
					IResource resource = (IResource)((IAdaptable)receiver).getAdapter(IResource.class);
					if (resource != null) {
						selContext = new ProjectSelectionContext(resource.getProject(), true);
					}
					else {
						IModelNode modelNode = (IModelNode)((IAdaptable)receiver).getAdapter(IModelNode.class);
						if (modelNode != null) {
							selContext = new RemoteSelectionContext(modelNode, true);
						}
					}
				}
			}
			if (selContext != null) {
				return LaunchConfigTypeBindingsManager.getInstance().isValidLaunchConfigType(
								(String)expectedValue,
								new LaunchSelection(
												(args != null && args.length > 0 ? args[0].toString() : null),
												selContext));
			}
		}
		return false;
	}
}
