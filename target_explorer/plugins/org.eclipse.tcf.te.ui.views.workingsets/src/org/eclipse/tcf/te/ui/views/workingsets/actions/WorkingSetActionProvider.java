/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.workingsets.actions;

import org.eclipse.tcf.te.ui.views.interfaces.IPersistableExpandingState;
import org.eclipse.tcf.te.ui.views.workingsets.WorkingSetsContentProvider;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.IExtensionActivationListener;
import org.eclipse.ui.navigator.IExtensionStateModel;
import org.eclipse.ui.navigator.INavigatorContentService;

/**
 * Working set action provider implementation.
 */
public class WorkingSetActionProvider extends CommonActionProvider {
	// Mark if the contribution items have been added to the menu
	private boolean contributedToViewMenu = false;

	/* default */ CommonViewer viewer;
	/* default */ INavigatorContentService contentService;
	/* default */ WorkingSetActionGroup workingSetActionGroup;
	/* default */ IExtensionStateModel extensionStateModel;

	private IExtensionActivationListener activationListener = new IExtensionActivationListener() {

		@Override
        public void onExtensionActivation(String viewerId, String[] extensionIds, boolean isActive) {

			for (int i = 0; i < extensionIds.length; i++) {
				if (WorkingSetsContentProvider.EXTENSION_ID.equals(extensionIds[i])) {
					if (isActive) {
						extensionStateModel = contentService.findStateModel(WorkingSetsContentProvider.EXTENSION_ID);
						workingSetActionGroup.setStateModel(extensionStateModel);
					} else {
						workingSetActionGroup.setShowTopLevelWorkingSets(false);
					}
				}
			}
		}

	};

	@Override
    public void init(ICommonActionExtensionSite site) {
		viewer = (CommonViewer) site.getStructuredViewer();
		contentService = site.getContentService();

		extensionStateModel = contentService.findStateModel(WorkingSetsContentProvider.EXTENSION_ID);

		workingSetActionGroup = new WorkingSetActionGroup(viewer, extensionStateModel);

		contentService.getActivationService().addExtensionActivationListener(activationListener);
	}

	@Override
    public void restoreState(final IMemento memento) {
		super.restoreState(memento);

		// Need to run this asynchronous to avoid being reentered when processing a selection change
		viewer.getControl().getShell().getDisplay().asyncExec(new Runnable() {
			@Override
            public void run() {
				boolean showWorkingSets = true;
				if (memento != null) {
					Integer showWorkingSetsInt = memento.getInteger(WorkingSetsContentProvider.SHOW_TOP_LEVEL_WORKING_SETS);
					showWorkingSets = showWorkingSetsInt == null || showWorkingSetsInt.intValue() == 1;
					extensionStateModel.setBooleanProperty(WorkingSetsContentProvider.SHOW_TOP_LEVEL_WORKING_SETS, showWorkingSets);
					workingSetActionGroup.setShowTopLevelWorkingSets(showWorkingSets);
					// Restore the expanded state only after the working set mode is set!
					IPersistableExpandingState state = (IPersistableExpandingState)viewer.getCommonNavigator().getAdapter(IPersistableExpandingState.class);
					if(state != null) {
						state.restoreExpandingState(memento);
					}
				} else {
					showWorkingSets = false;

					extensionStateModel.setBooleanProperty(WorkingSetsContentProvider.SHOW_TOP_LEVEL_WORKING_SETS, showWorkingSets);
					workingSetActionGroup.setShowTopLevelWorkingSets(showWorkingSets);
				}
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonActionProvider#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
    public void saveState(IMemento memento) {
		super.saveState(memento);

		if (memento != null) {
			int showWorkingSets = extensionStateModel.getBooleanProperty(WorkingSetsContentProvider.SHOW_TOP_LEVEL_WORKING_SETS) ? 1 : 0;
			memento.putInteger(WorkingSetsContentProvider.SHOW_TOP_LEVEL_WORKING_SETS, showWorkingSets);
			// Save the expanding state of the common viewer.
			IPersistableExpandingState state = (IPersistableExpandingState)viewer.getCommonNavigator().getAdapter(IPersistableExpandingState.class);
			if (state != null) {
				state.saveExpandingState(memento);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
	 */
	@Override
    public void fillActionBars(IActionBars actionBars) {
		if (!contributedToViewMenu) {
			try {
				super.fillActionBars(actionBars);
				workingSetActionGroup.fillActionBars(actionBars);
			} finally {
				contributedToViewMenu = true;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#dispose()
	 */
	@Override
    public void dispose() {
		super.dispose();
		workingSetActionGroup.dispose();
		contentService.getActivationService().removeExtensionActivationListener(activationListener);
	}
}
