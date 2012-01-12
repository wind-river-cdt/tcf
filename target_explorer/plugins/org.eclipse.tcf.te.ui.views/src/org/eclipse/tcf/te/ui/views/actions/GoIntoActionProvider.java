/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.actions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.internal.navigator.framelist.GoIntoAction;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

/**
 * The action provider to add Go Into action to the context menu of Target Explorer.
 * 
 * @since 1.0.0
 * 						-Copied and adapted from org.eclipse.navigator.resource.GoIntoActionProvider.
 */
@SuppressWarnings("restriction")
public class GoIntoActionProvider extends CommonActionProvider {
	// GoInto Action.
    private GoIntoAction goIntoAction;

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.navigator.CommonActionProvider#init(org.eclipse.ui.navigator.ICommonActionExtensionSite)
     */
	@Override
    public void init(ICommonActionExtensionSite anActionSite) {
		anActionSite.getViewSite().getShell();
		CommonViewer viewer = (CommonViewer) anActionSite.getStructuredViewer();
		goIntoAction = new GoIntoAction(viewer.getFrameList());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#dispose()
	 */
	@Override
    public void dispose() {
		goIntoAction.dispose();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
	 */
	@Override
    public void fillActionBars(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.GO_INTO, goIntoAction);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
    public void fillContextMenu(IMenuManager menu) {
		menu.appendToGroup("group.goto", goIntoAction); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#updateActionBars()
	 */
	@Override
    public void updateActionBars() {
		goIntoAction.update();
	}
}
