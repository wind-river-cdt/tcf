/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.editor.pages;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.te.core.interfaces.IFilterable;
import org.eclipse.tcf.te.core.interfaces.IPropertyChangeProvider;
import org.eclipse.tcf.te.ui.forms.CustomFormToolkit;
import org.eclipse.tcf.te.ui.trees.TreeControl;
import org.eclipse.tcf.te.ui.utils.TreeViewerUtil;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.MultiPageSelectionProvider;
import org.osgi.framework.Bundle;

/**
 * Tree viewer based editor page implementation.
 */
public abstract class TreeViewerExplorerEditorPage extends AbstractCustomFormToolkitEditorPage implements IDoubleClickListener {
	// The references to the pages subcontrol's (needed for disposal)
	private TreeControl treeControl;
	private IToolBarManager toolbarMgr;
	private PropertyChangeListener pcListener;
	private Image formImage;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormPage#dispose()
	 */
	@Override
	public void dispose() {
	    IPropertyChangeProvider provider = getPropertyChangeProvider();
		if(provider != null && pcListener != null) {
			provider.removePropertyChangeListener(pcListener);
		}
		if(formImage != null) {
			formImage.dispose();
		}
		if (treeControl != null) { treeControl.dispose(); treeControl = null; }
		super.dispose();
	}

	/**
	 * Set the initial focus to the tree.
	 */
	@Override
    public void setFocus() {
		Control control = treeControl.getViewer().getControl();
		if(control != null && !control.isDisposed()) {
			control.setFocus();
		}
		else {
			super.setFocus();
		}
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractEditorPage#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) {
	    super.setInitializationData(config, propertyName, data);
	    String iconPath = config.getAttribute("icon"); //$NON-NLS-1$
	    if(iconPath != null) {
	    	String bundleId = config.getContributor().getName();
	    	Bundle bundle = Platform.getBundle(bundleId);
	    	if(bundle != null) {
	    		URL iconURL = bundle.getEntry(iconPath);
	    		if(iconURL != null) {
	    			ImageDescriptor iconDesc = ImageDescriptor.createFromURL(iconURL);
	    			if(iconDesc != null) {
	    				formImage = iconDesc.createImage();
	    			}
	    		}
	    	}
	    }
		treeControl = doCreateTreeControl();
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractCustomFormToolkitEditorPage#getFormImage()
	 */
	@Override
    protected Image getFormImage() {
	    return formImage;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractCustomFormToolkitEditorPage#createToolbarContributionItems(org.eclipse.jface.action.IToolBarManager)
	 */
	@Override
    protected void createToolbarContributionItems(IToolBarManager manager) {
	    this.toolbarMgr = manager;
	    treeControl.createToolbarContributionItems(manager);
	    super.createToolbarContributionItems(manager);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractCustomFormToolkitEditorPage#doCreateFormContent(org.eclipse.swt.widgets.Composite, org.eclipse.tcf.te.ui.forms.CustomFormToolkit)
	 */
	@Override
    protected void doCreateFormContent(Composite parent, CustomFormToolkit toolkit) {
		Assert.isNotNull(parent);
		Assert.isNotNull(toolkit);

		// Setup the tree control
		Assert.isNotNull(treeControl);
		treeControl.setupFormPanel(parent, toolkit);

		// Register the context menu at the parent workbench part site.
		getSite().registerContextMenu(getId(), treeControl.getContextMenuManager(), treeControl.getViewer());

		// Set the initial input
		Object input = getViewerInput();
		treeControl.getViewer().setInput(input);
		
	    addViewerListeners();
	    
	    updateUI();
	}

	/**
	 * Add tree viewer listeners to the tree control.
	 */
	private void addViewerListeners() {
		TreeViewer viewer = (TreeViewer) treeControl.getViewer();
		viewer.addSelectionChangedListener(new ISelectionChangedListener(){
			@Override
            public void selectionChanged(SelectionChangedEvent event) {
				propagateSelection();
            }});
		viewer.getTree().addFocusListener(new FocusAdapter(){
			@Override
            public void focusGained(FocusEvent e) {
				propagateSelection();
            }
		});
		viewer.addDoubleClickListener(this);
		
	    IPropertyChangeProvider provider = getPropertyChangeProvider();
		if(provider != null) {
			pcListener = new PropertyChangeListener() {
				@Override
				public void propertyChange(final PropertyChangeEvent event) {
					Object object = event.getSource();
					Object input = getTreeViewerInput();
					if (object == input) {
						if (Display.getCurrent() != null) {
							updateUI();
						}
						else {
							Display display = getSite().getShell().getDisplay();
							display.asyncExec(new Runnable() {
								@Override
								public void run() {
									updateUI();
								}
							});
						}
					}
				}
			};
			provider.addPropertyChangeListener(pcListener);
		}
    }
	
	@Override
    public Object getAdapter(Class adapter) {
		if(TreeViewer.class.equals(adapter)) {
			return treeControl.getViewer();
		}
	    return super.getAdapter(adapter);
    }

	/**
	 * Get an adapter instance from the adaptable with the specified 
	 * adapter interface.
	 * 
	 * @param adaptable The adaptable to get the adapter.
	 * @param adapter The adapter interface class.
	 * @return An adapter or null if it does not adapt to this type.
	 */
	@SuppressWarnings("rawtypes")
	private Object getAdapter(Object adaptable, Class adapter) {
		Object adapted = null;
		if(adapter.isInstance(adaptable)) {
			adapted = adaptable;
		}
		if(adapted == null && adaptable instanceof IAdaptable) {
			adapted = ((IAdaptable)adaptable).getAdapter(adapter);
		}
		if(adapted == null && adaptable != null) {
			adapted = Platform.getAdapterManager().getAdapter(adaptable, adapter);
		}
		return adapted;
	}
	
	/**
	 * Get an adapter of IFilteringLabelDecorator.
	 * 
	 * @return an IFilteringLabelDecorator adapter or null if it does not adapt to IFilteringLabelDecorator.
	 */
	private IFilterable adaptFilterable() {
		Object input = getTreeViewerInput();
		if (input != null) {
			return (IFilterable) getAdapter(input, IFilterable.class);
		}
		return null;
	}

	protected abstract Object getViewerInput();

	/**
	 * Get the viewer input adapter for the input.
	 * 
	 * @param input the input of the tree viewer.
	 * @return The adapter.
	 */
	private IPropertyChangeProvider getPropertyChangeProvider() {
		Object input = getTreeViewerInput();
		if (input != null) {
			return (IPropertyChangeProvider) getAdapter(input, IPropertyChangeProvider.class);
		}
		return null;
    }
	
	Object getTreeViewerInput() {
		if (treeControl != null && treeControl.getViewer() != null) {
			return treeControl.getViewer().getInput();
		}
		return null;
	}
	
	/**
	 * Creates and returns a tree control.
	 *
	 * @return The new tree control.
	 */
	protected TreeControl doCreateTreeControl() {
		return new TreeControl(getViewerId(), this);
	}

	/**
	 * Returns the associated tree control.
	 *
	 * @return The associated tree control or <code>null</code>.
	 */
	public final TreeControl getTreeControl() {
		return treeControl;
	}
	
	/**
	 * Update the page's ui including its toolbar and title text and image.
	 */
	protected void updateUI() {
		toolbarMgr.update(true);
		IManagedForm managedForm = getManagedForm();
		Form form = managedForm.getForm().getForm();
		Object element = getTreeViewerInput();
		boolean filterEnabled = false;
		IFilterable filterDecorator = adaptFilterable();
		if (filterDecorator != null) {
			TreeViewer viewer = (TreeViewer) treeControl.getViewer();
			filterEnabled = TreeViewerUtil.isFiltering(viewer, TreePath.EMPTY);
		}
		ILabelDecorator titleDecorator = getTitleBarDecorator();
		String text = getFormTitle();
		if (text != null) {
			if (titleDecorator != null) {
				text = titleDecorator.decorateText(text, element);
			}
			if (filterEnabled) {
				TreeViewer viewer = (TreeViewer) treeControl.getViewer();
				text = TreeViewerUtil.getDecoratedText(text, viewer, TreePath.EMPTY);
			}
		}
		Image image = getFormImage();
		if (image != null) {
			if (titleDecorator != null) {
				image = titleDecorator.decorateImage(image, element);
			}
			if (filterEnabled) {
				TreeViewer viewer = (TreeViewer) treeControl.getViewer();
				image = TreeViewerUtil.getDecoratedImage(image, viewer, TreePath.EMPTY);
			}
		}
		if (text != null) {
			try {
				form.setText(text);
			}
			catch (Exception e) {
				// Ignore any disposed exception
			}
		}
		if (image != null) {
			try {
				form.setImage(image);
			}
			catch (Exception e) {
				// Ignore any disposed exception
			}
		}
	}
	
	/**
	 * Get the title bar's decorator or null if there's no decorator for it.
	 */
	protected ILabelDecorator getTitleBarDecorator() {
	    return null;
    }

	/**
	 * Propagate the current selection to the editor's selection provider.
	 */
	protected void propagateSelection() {
		ISelection selection = treeControl.getViewer().getSelection();
		ISelectionProvider selectionProvider = getSite().getSelectionProvider();
		// If the parent control is already disposed, we have no real chance of
		// testing for it. Catch the SWT exception here just in case.
		try {
			selectionProvider.setSelection(selection);
			if (selectionProvider instanceof MultiPageSelectionProvider) {
				SelectionChangedEvent changedEvent = new SelectionChangedEvent(selectionProvider, selection);
				((MultiPageSelectionProvider) selectionProvider).firePostSelectionChanged(changedEvent);
			}
		}
		catch (SWTException e) {
			/* ignored on purpose */
		}
	}
	
	/**
	 * Get the id of the command invoked when the tree is double-clicked.
	 * If the id is null, then no command is invoked.
	 *
	 * @return The double-click command id.
	 */
	protected String getDoubleClickCommandId() {
		return null;
	}
	
	/**
	 * Get the tree viewer's id. This viewer id is used by
	 * viewer extension to define columns and filters.
	 *
	 * @return This viewer's id or null.
	 */
	protected abstract String getViewerId();

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
    public void doubleClick(final DoubleClickEvent event) {
		// If an handled and enabled command is registered for the ICommonActionConstants.OPEN
		// retargetable action id, redirect the double click handling to the command handler.
		//
		// Note: The default tree node expansion must be re-implemented in the active handler!
		String commandId = getDoubleClickCommandId();
		Command cmd = null;
		if(commandId != null) {
			ICommandService service = (ICommandService)PlatformUI.getWorkbench().getService(ICommandService.class);
			cmd = service != null ? service.getCommand(commandId) : null;
		}
		if (cmd != null && cmd.isDefined() && cmd.isEnabled()) {
			final Command command = cmd;
			SafeRunner.run(new SafeRunnable(){
				@Override
                public void handleException(Throwable e) {
					// Ignore exception
                }
				@Override
                public void run() throws Exception {
					ISelection selection = event.getSelection();
					EvaluationContext ctx = new EvaluationContext(null, selection);
					ctx.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME, selection);
					ctx.addVariable(ISources.ACTIVE_MENU_SELECTION_NAME, selection);
					ctx.addVariable(ISources.ACTIVE_WORKBENCH_WINDOW_NAME, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
					IWorkbenchPartSite site = getSite();
					ctx.addVariable(ISources.ACTIVE_PART_ID_NAME, site.getId());
					ctx.addVariable(ISources.ACTIVE_PART_NAME, TreeViewerExplorerEditorPage.this);
					ctx.addVariable(ISources.ACTIVE_SITE_NAME, site);
					ctx.addVariable(ISources.ACTIVE_SHELL_NAME, site.getShell());
					ctx.setAllowPluginActivation(true);

					ParameterizedCommand pCmd = ParameterizedCommand.generateCommand(command, null);
					Assert.isNotNull(pCmd);
					IHandlerService handlerSvc = (IHandlerService)PlatformUI.getWorkbench().getService(IHandlerService.class);
					Assert.isNotNull(handlerSvc);
					handlerSvc.executeCommandInContext(pCmd, null, ctx);
                }});
		} else {
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			Object element = selection.getFirstElement();
			TreeViewer viewer = (TreeViewer) treeControl.getViewer();
			if (viewer.isExpandable(element)) {
				viewer.setExpandedState(element, !viewer.getExpandedState(element));
			}
		}	    
    }
}
