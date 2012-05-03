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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tcf.te.launch.ui.model.LaunchNode;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Launch handler implementation.
 */
public class LaunchHandler extends AbstractHandler {

	private String mode;

	public LaunchHandler(String mode) {
		this.mode = mode;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
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
}
