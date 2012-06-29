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

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tcf.te.launch.ui.model.LaunchNode;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Launch handler implementation.
 */
public class LaunchHandler extends AbstractHandler implements IExecutableExtension {

	private String mode = null;

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Assert.isNotNull(mode);
		// Get the current selection
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection && ((IStructuredSelection) selection).size() == 1) {
			Object element = ((IStructuredSelection)selection).getFirstElement();
			if (element instanceof LaunchNode) {
				LaunchNode node = (LaunchNode)element;
				if (node.getLaunchConfiguration() != null) {
					DebugUITools.launch(node.getLaunchConfiguration(), mode);
				}
			}
		}
		return null;
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
		}
	}
}
