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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tcf.te.launch.ui.model.LaunchNode;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;

/**
 * OpenActionProvider
 */
public class OpenActionProvider extends CommonActionProvider {

	private OpenAction openAction = new OpenAction();

	protected static class OpenAction extends Action {

		private LaunchDialogHandler handler = new LaunchDialogHandler();
		private LaunchNode node = null;

		/* (non-Javadoc)
		 * @see org.eclipse.jface.action.Action#run()
		 */
		@Override
		public void run() {
			handler.doLaunch(node);
		}

		public void selectionChanged(ISelection selection) {
			node = null;
			if (selection instanceof IStructuredSelection && ((IStructuredSelection)selection).size() == 1) {
				Object element = ((IStructuredSelection)selection).getFirstElement();
				if (element instanceof LaunchNode && ((LaunchNode)element).getLaunchConfiguration() != null) {
					node = (LaunchNode)element;
				}
			}
			setEnabled(node != null);
		}
	}

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
	 */
	@Override
	public void fillActionBars(IActionBars actionBars) {
		openAction.selectionChanged(getContext().getSelection());
		if (openAction.isEnabled()) {
			actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openAction);
		}
	}
}
