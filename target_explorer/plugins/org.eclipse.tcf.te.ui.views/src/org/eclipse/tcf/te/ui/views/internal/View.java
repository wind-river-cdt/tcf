/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.internal;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.te.ui.trees.TreeViewerEditorActivationStrategy;
import org.eclipse.tcf.te.ui.views.activator.UIPlugin;
import org.eclipse.tcf.te.ui.views.interfaces.IRoot;
import org.eclipse.tcf.te.ui.views.interfaces.IUIConstants;
import org.eclipse.ui.IAggregateWorkingSet;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;


/**
 * View implementation.
 * <p>
 * The view is based on the Eclipse Common Navigator framework.
 */
public class View extends CommonNavigator implements ITabbedPropertySheetPageContributor{
	// The view root mode
	private int rootMode = IUIConstants.MODE_NORMAL;

	/**
	 * Used only in the case of top level = MODE_NORMAL and only when some
	 * working sets are selected.
	 */
	private String workingSetLabel;

	/**
	 * Constructor.
	 */
	public View() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonNavigator#getInitialInput()
	 */
	@Override
	protected Object getInitialInput() {
		return ViewRoot.getInstance();
	}

	/**
	 * Sets the view's root mode.
	 *
	 * @param mode The root mode.
	 * @see IUIConstants
	 */
	@Override
    public void setRootMode(int mode) {
		rootMode = mode;
	}

	/**
	 * Returns the view's root mode.
	 *
	 * @return The root mode
	 * @see IUIConstants
	 */
	@Override
    public int getRootMode() {
		return rootMode;
	}

	/**
	 * Sets the working set label.
	 *
	 * @param label The working set label or <code>null</code>.
	 */
	@Override
    public void setWorkingSetLabel(String label) {
		workingSetLabel = label;
	}

	/**
	 * Returns the working set label.
	 *
	 * @return The working set label or <code>null</code>.
	 */
	@Override
    public String getWorkingSetLabel() {
		return workingSetLabel;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonNavigator#dispose()
	 */
	@Override
	public void dispose() {
	    super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonNavigator#createCommonViewerObject(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected CommonViewer createCommonViewerObject(Composite parent) {
		ViewViewer viewer = new ViewViewer(getViewSite().getId(), parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		// Define an editor activation strategy for the common viewer so as to be invoked only programmatically.
		ColumnViewerEditorActivationStrategy activationStrategy = new TreeViewerEditorActivationStrategy(getSite().getId(), viewer);
		TreeViewerEditor.create(viewer, null, activationStrategy, ColumnViewerEditor.DEFAULT);
		return viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonNavigator#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		// Add the additional custom toolbar groups
		addCustomToolbarGroups();
	}

	/**
	 * Adds the custom toolbar groups to the view toolbar.
	 */
	protected void addCustomToolbarGroups() {
		if (getViewSite() != null && getViewSite().getActionBars() != null) {
			IToolBarManager tbManager = getViewSite().getActionBars().getToolBarManager();
			if (tbManager != null) {
				tbManager.insertBefore("FRAME_ACTION_GROUP_ID", new GroupMarker("group.new")); //$NON-NLS-1$ //$NON-NLS-2$
				tbManager.appendToGroup("group.new", new Separator("group.configure")); //$NON-NLS-1$ //$NON-NLS-2$
				tbManager.appendToGroup("group.configure", new Separator("group.connect")); //$NON-NLS-1$ //$NON-NLS-2$
				tbManager.appendToGroup("group.connect", new Separator("group.symbols.rd")); //$NON-NLS-1$ //$NON-NLS-2$
				tbManager.appendToGroup("group.symbols.rd", new GroupMarker("group.symbols")); //$NON-NLS-1$ //$NON-NLS-2$
				tbManager.appendToGroup("group.symbols", new Separator("group.refresh")); //$NON-NLS-1$ //$NON-NLS-2$
				tbManager.appendToGroup("group.refresh", new Separator(IWorkbenchActionConstants.MB_ADDITIONS)); //$NON-NLS-1$
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonNavigator#handleDoubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	protected void handleDoubleClick(DoubleClickEvent dblClickEvent) {
		// If an handled and enabled command is registered for the ICommonActionConstants.OPEN
		// retargetable action id, redirect the double click handling to the command handler.
		//
		// Note: The default tree node expansion must be re-implemented in the active handler!
		ICommandService service = (ICommandService)PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = service != null ? service.getCommand(ICommonActionConstants.OPEN) : null;
		if (command != null && command.isDefined() && command.isEnabled()) {
			try {
				ISelection selection = dblClickEvent.getSelection();
				EvaluationContext ctx = new EvaluationContext(null, selection);
				ctx.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME, selection);
				ctx.addVariable(ISources.ACTIVE_MENU_SELECTION_NAME, selection);
				ctx.addVariable(ISources.ACTIVE_WORKBENCH_WINDOW_NAME, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
				ctx.addVariable(ISources.ACTIVE_PART_ID_NAME, getViewSite().getId());
				ctx.addVariable(ISources.ACTIVE_PART_NAME, this);
				ctx.addVariable(ISources.ACTIVE_SITE_NAME, getViewSite());
				ctx.addVariable(ISources.ACTIVE_SHELL_NAME, getViewSite().getShell());
				ctx.setAllowPluginActivation(true);

				ParameterizedCommand pCmd = ParameterizedCommand.generateCommand(command, null);
				Assert.isNotNull(pCmd);
				IHandlerService handlerSvc = (IHandlerService)PlatformUI.getWorkbench().getService(IHandlerService.class);
				Assert.isNotNull(handlerSvc);
				handlerSvc.executeCommandInContext(pCmd, null, ctx);
			} catch (Exception e) {
				// If the platform is in debug mode, we print the exception to the log view
				if (Platform.inDebugMode()) {
					IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), e.getLocalizedMessage(), e);
					UIPlugin.getDefault().getLog().log(status);
				}
			}
		} else {
			// Fallback to the default implementation
			super.handleDoubleClick(dblClickEvent);
		}
	}

	/**
	 * The superclass does not deal with the content description, handle it here.
	 *
	 * @noreference
	 */
	@Override
    public void updateTitle() {
		super.updateTitle();

		// Get the input from the common viewer
		Object input = getCommonViewer().getInput();

		// The content description to set
		String contentDescription = null;

		if (input instanceof IAdaptable) {
			IWorkbenchAdapter adapter = (IWorkbenchAdapter) ((IAdaptable) input).getAdapter(IWorkbenchAdapter.class);
			if (adapter != null) contentDescription = adapter.getLabel(input);
		}
		else if (input instanceof IRoot || (input != null && "WorkingSetViewStateManager".equals(input.getClass().getSimpleName()))) { //$NON-NLS-1$
			// The root node does not have a content description
		}
		else if (input != null && !(input instanceof IAggregateWorkingSet)) {
			contentDescription = input.toString();
		}

		setContentDescription(contentDescription != null ? contentDescription : ""); //$NON-NLS-1$
	}


    /* (non-Javadoc)
     * @see org.eclipse.ui.navigator.CommonNavigator#getAdapter(java.lang.Class)
     */
	@Override
    public Object getAdapter(Class adapter) {
		if(adapter == IPropertySheetPage.class) {
			return new TabbedPropertySheetPage(this);
		}
	    return super.getAdapter(adapter);
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor#getContributorId()
	 */
	@Override
    public String getContributorId() {
	    return IUIConstants.TABBED_PROPERTIES_CONTRIBUTOR_ID;
    }
}
