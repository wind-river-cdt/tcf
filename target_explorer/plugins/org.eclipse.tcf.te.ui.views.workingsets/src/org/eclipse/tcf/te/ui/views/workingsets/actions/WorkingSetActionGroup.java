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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.tcf.te.ui.views.workingsets.WorkingSetsContentProvider;
import org.eclipse.tcf.te.ui.views.workingsets.activator.UIPlugin;
import org.eclipse.tcf.te.ui.views.workingsets.interfaces.ImageConsts;
import org.eclipse.tcf.te.ui.views.workingsets.nls.Messages;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.IExtensionStateModel;

/**
 * Provides the radio buttons at the top of the view menu that control the root of the Target
 * Explorer, which is either working sets or targets. When the state is changed through the
 * actions, the WorkingSetsContentProvider.SHOW_TOP_LEVEL_WORKING_SETS property in the extension
 * state model is updated.
 *
 * This is installed by the WorkingSetActionProvider.
 *
 * <p>
 * Copied and adapted from <code>org.eclipse.ui.internal.navigator.resources.actions.WorkingSetRootModeActionGroup</code>.
 */
public class WorkingSetActionGroup extends ActionGroup {
	private static final String WS_GROUP = "group.ws"; //$NON-NLS-1$

	/* default */ IExtensionStateModel stateModel;
	/* default */ StructuredViewer viewer;

	private boolean hasContributedToViewMenu = false;
	private IAction workingSetsAction = null;
	private IAction elementsAction = null;
	/* default */ IAction[] actions;
	/* default */ int currentSelection;
	/* default */ MenuItem[] items;

	/**
	 * Toggle action switching the top elements between working sets and elements.
	 */
	private class TopLevelContentAction extends Action {
		// If true, the action does enable working set top elements.
		private final boolean showWorkingSets;

		/**
		 * Constructor
		 *
		 * @param showWorkingSets If <code>true</code>, the action does enable working sets as top level elements,
		 *                        <code>false</code> to disable working sets as top level elements.
		 */
		public TopLevelContentAction(boolean toGroupWorkingSets) {
			super("", AS_RADIO_BUTTON); //$NON-NLS-1$
			showWorkingSets = toGroupWorkingSets;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.action.Action#run()
		 */
		@SuppressWarnings("restriction")
        @Override
		public void run() {
			boolean isShowTopLevelWorkingSets = stateModel.getBooleanProperty(WorkingSetsContentProvider.SHOW_TOP_LEVEL_WORKING_SETS);
			if (isShowTopLevelWorkingSets != showWorkingSets) {
				// Toggle the "show working set top level elements" property.
				//
				// This will trigger the WorkingSetsContentProvider property change listener
				// to update the view root mode.
				stateModel.setBooleanProperty(WorkingSetsContentProvider.SHOW_TOP_LEVEL_WORKING_SETS, showWorkingSets);

				// Disable the viewer redraw, refresh the viewer, reset
				// the frame list and enable redraw finally.
				viewer.getControl().setRedraw(false);
				try {
					viewer.refresh();
					if (viewer instanceof CommonViewer) {
						((CommonViewer)viewer).getFrameList().reset();
					}
				}
				finally {
					viewer.getControl().setRedraw(true);
				}
			}
		}
	}

	/**
	 * Create an action group that will listen to the stateModel and update the structuredViewer
	 * when necessary.
	 *
	 * @param viewer
	 * @param stateModel
	 */
	public WorkingSetActionGroup(StructuredViewer viewer, IExtensionStateModel stateModel) {
		super();
		this.viewer = viewer;
		this.stateModel = stateModel;
	}

	/* (non-Javadoc)
	 * @see ActionGroup#fillActionBars(IActionBars)
	 */
	@Override
	public void fillActionBars(IActionBars actionBars) {
		if (hasContributedToViewMenu) return;
		IMenuManager topLevelSubMenu = new MenuManager(Messages.WorkingSetActionGroup_Top_Level_Element);
		addActions(topLevelSubMenu);

		IMenuManager manager = actionBars.getMenuManager();
		manager.insertBefore(IWorkbenchActionConstants.MB_ADDITIONS, new GroupMarker(WS_GROUP));
		manager.appendToGroup(WS_GROUP, topLevelSubMenu);
		manager.appendToGroup(WS_GROUP, new Separator());
		manager.appendToGroup(WS_GROUP, new ConfigureWorkingSetAction(viewer));
		hasContributedToViewMenu = true;
	}

	/**
	 * Adds the actions to the given menu manager.
	 */
	protected void addActions(IMenuManager viewMenu) {
		if (actions == null) actions = createActions();

		viewMenu.add(new Separator());
		items = new MenuItem[actions.length];

		for (int i = 0; i < actions.length; i++) {
			final int j = i;

			viewMenu.add(new ContributionItem() {

				@Override
				public void fill(Menu menu, int index) {

					int style = SWT.CHECK;
					if ((actions[j].getStyle() & IAction.AS_RADIO_BUTTON) != 0) style = SWT.RADIO;

					final MenuItem mi = new MenuItem(menu, style, index);
					items[j] = mi;
					mi.setText(actions[j].getText());
					mi.setSelection(currentSelection == j);
					mi.addSelectionListener(new SelectionAdapter() {

						@Override
						public void widgetSelected(SelectionEvent e) {
							if (currentSelection == j) {
								items[currentSelection].setSelection(true);
								return;
							}
							actions[j].run();

							// Update checked state
							items[currentSelection].setSelection(false);
							currentSelection = j;
							items[currentSelection].setSelection(true);
						}

					});

				}

				@Override
				public boolean isDynamic() {
					return false;
				}
			});
		}
	}

	@SuppressWarnings("restriction")
    private IAction[] createActions() {

		elementsAction = new TopLevelContentAction(false);
		elementsAction.setText(Messages.WorkingSetActionGroup_Elements);
		elementsAction.setImageDescriptor(org.eclipse.tcf.te.ui.views.activator.UIPlugin.getImageDescriptor(org.eclipse.tcf.te.ui.views.interfaces.ImageConsts.VIEW));

		workingSetsAction = new TopLevelContentAction(true);
		workingSetsAction.setText(Messages.WorkingSetActionGroup_Working_Set);
		workingSetsAction.setImageDescriptor(UIPlugin.getImageDescriptor(ImageConsts.WORKING_SETS));

		return new IAction[] { elementsAction, workingSetsAction };
	}

	/**
	 * Toggle whether top level working sets should be displayed as a group or collapse to just show
	 * their contents.
	 *
	 * @param showTopLevelWorkingSets
	 */
	public void setShowTopLevelWorkingSets(boolean showTopLevelWorkingSets) {
		if (actions == null) actions = createActions();

		currentSelection = showTopLevelWorkingSets ? 1 : 0;
		workingSetsAction.setChecked(showTopLevelWorkingSets);
		elementsAction.setChecked(!showTopLevelWorkingSets);

		if (items != null) {
			for (int i = 0; i < items.length; i++) {
				if (items[i] != null && actions[i] != null) {
					items[i].setSelection(actions[i].isChecked());
				}
			}
		}
		if (stateModel != null) {
			stateModel.setBooleanProperty(WorkingSetsContentProvider.SHOW_TOP_LEVEL_WORKING_SETS, showTopLevelWorkingSets);
		}
	}

	/**
	 * @param stateModel
	 */
	public void setStateModel(IExtensionStateModel stateModel) {
		this.stateModel = stateModel;
	}
}
