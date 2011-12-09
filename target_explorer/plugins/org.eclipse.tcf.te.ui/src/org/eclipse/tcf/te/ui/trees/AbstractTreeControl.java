/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.trees;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.tcf.te.ui.WorkbenchPartControl;
import org.eclipse.tcf.te.ui.activator.UIPlugin;
import org.eclipse.tcf.te.ui.forms.CustomFormToolkit;
import org.eclipse.tcf.te.ui.interfaces.ImageConsts;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.menus.IMenuService;


/**
 * Abstract tree control implementation.
 */
public abstract class AbstractTreeControl extends WorkbenchPartControl implements SelectionListener{
	// Reference to the tree viewer instance
	private TreeViewer viewer;
	// Reference to the selection changed listener
	private ISelectionChangedListener selectionChangedListener;
	// The descriptors of the viewer filters configured for this viewer.
	private FilterDescriptor[] filterDescriptors;
	// The tree viewer columns of this viewer.
	private ColumnDescriptor[] columns;

	/**
	 * Constructor.
	 */
	public AbstractTreeControl() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param parentPart The parent workbench part this control is embedded in or <code>null</code>.
	 */
	public AbstractTreeControl(IWorkbenchPart parentPart) {
		super(parentPart);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.WorkbenchPartControl#dispose()
	 */
	@Override
	public void dispose() {
		// Unregister the selection changed listener
		if (selectionChangedListener != null) {
			if (getViewer() != null) {
				getViewer().removeSelectionChangedListener(selectionChangedListener);
			}
			selectionChangedListener = null;
		}

		// Dispose the columns' resources.
		if (columns != null) {
			for (ColumnDescriptor column : columns) {
				if (column.getImage() != null) {
					column.getImage().dispose();
				}
				if (column.getLabelProvider() != null) {
					column.getLabelProvider().dispose();
				}
			}
		}
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.WorkbenchPartControl#setupFormPanel(org.eclipse.swt.widgets.Composite, org.eclipse.tcf.te.ui.forms.CustomFormToolkit)
	 */
	@Override
	public void setupFormPanel(Composite parent, CustomFormToolkit toolkit) {
		super.setupFormPanel(parent, toolkit);

		// Create the tree viewer
		viewer = doCreateTreeViewer(parent);
		// And configure the tree viewer
		configureTreeViewer(viewer);

		// Prepare popup menu and toolbar
		createContributionItems(viewer);
		
	}

	/**
	 * Creates the tree viewer instance.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @return The tree viewer.
	 */
	protected TreeViewer doCreateTreeViewer(Composite parent) {
		Assert.isNotNull(parent);
		return new TreeViewer(parent, SWT.FULL_SELECTION | SWT.SINGLE);
	}

	/**
	 * Configure the tree viewer.
	 *
	 * @param viewer The tree viewer. Must not be <code>null</code>.
	 */
	protected void configureTreeViewer(TreeViewer viewer) {
		Assert.isNotNull(viewer);

		viewer.setAutoExpandLevel(getAutoExpandLevel());

		viewer.setLabelProvider(doCreateTreeViewerLabelProvider(viewer));
		viewer.setContentProvider(doCreateTreeViewerContentProvider(viewer));
		viewer.setComparator(doCreateTreeViewerComparator(viewer));

		viewer.getTree().setLayoutData(doCreateTreeViewerLayoutData(viewer));

		// Attach the selection changed listener
		selectionChangedListener = doCreateTreeViewerSelectionChangedListener(viewer);
		if (selectionChangedListener != null) {
			viewer.addSelectionChangedListener(selectionChangedListener);
		}
		
		// Create the tree columns.
		columns = doCreateViewerColumns(viewer);
		if (hasColumns()) {
			createTreeColumns(viewer);
		}
		
		// Add the viewer filters.
		filterDescriptors = doCreateFilterDescriptors(viewer);
		updateFilters();
		
		// Set the header visible.
		viewer.getTree().setHeaderVisible(hasColumns());
		
		// Set the help context.
		String helpContextId = getHelpId();
		if (helpContextId != null) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getTree(), helpContextId);
		}
	}
	
	/**
	 * Get the help context id of this viewer.
	 * 
	 * @return The help context id or null if no help available.
	 */
	protected String getHelpId() {
		return null;
	}
	
	/**
	 * Create the tree viewer columns from the viewers extension. 
	 * Subclass may override it to provide its customized viewer columns. 
	 * 
	 * @param viewer The tree viewer to create the columns for.
	 * @return The tree viewer columns.
	 */
	protected ColumnDescriptor[] doCreateViewerColumns(TreeViewer viewer) {
		if(columns == null) {
			parseExtension();
		}
		return columns;
	}
	
	/**
	 * Parse the viewer extension to create tree viewer columns and viewer filters.
	 */
	private void parseExtension() {
		if (getViewerId() != null) {
			TreeViewerExtension viewerExtension = new TreeViewerExtension(getViewerId(), viewer);
			columns = viewerExtension.getColumns();
			filterDescriptors = viewerExtension.getFilterDescriptors();
		}
	}
	
	/**
	 * Create the viewer filters from the viewers extension. Subclass may 
	 * override it to provide its customized viewer filters. 
	 * 
	 * @param viewer The tree viewer to create filters for.
	 * @return The filter descriptors for the viewer.
	 */
	protected FilterDescriptor[] doCreateFilterDescriptors(TreeViewer viewer) {
		if(filterDescriptors == null) {
			parseExtension();
		}
		return filterDescriptors;
	}
	
	/**
	 * Update the tree viewer's filters using the current filter descriptors.
	 */
	public void updateFilters() {
		if (filterDescriptors != null) {
			List<ViewerFilter> newFilters = new ArrayList<ViewerFilter>();
			for (FilterDescriptor descriptor : filterDescriptors) {
				if (descriptor.getFilter() != null) {
					if (descriptor.isEnabled()) {
						newFilters.add(descriptor.getFilter());
					}
				}
			}
			viewer.setFilters(newFilters.toArray(new ViewerFilter[newFilters.size()]));
		}
	}

	/**
	 * Create the tree columns for the viewer from the tree viewer columns.
	 * Subclass may override to create its customized the creation.
	 * 
	 * @param viewer The tree viewer.
	 */
	protected void createTreeColumns(TreeViewer viewer) {
		if (columns != null) {
			Tree tree = viewer.getTree();
			for (ColumnDescriptor column : columns) {
				TreeColumn treeColumn = new TreeColumn(tree, column.getStyle());
				treeColumn.setData(column);
				treeColumn.setText(column.getName());
				treeColumn.setToolTipText(column.getDescription());
				treeColumn.setAlignment(column.getAlignment());
				treeColumn.setImage(column.getImage());
				treeColumn.setMoveable(column.isMoveable());
				treeColumn.setResizable(column.isResizable());
				treeColumn.setWidth(column.isVisible() ? column.getWidth() : 0);
				treeColumn.addSelectionListener(this);
				column.setTreeColumn(treeColumn);
			}
		}
	}

	/**
	 * Get the tree viewer's id. This viewer id is used by
	 * viewer extension to define columns and filters.
	 * 
	 * @return This viewer's id or null.
	 */
	protected abstract String getViewerId();
	
	/**
	 * Returns if or if not to show the tree columns.
	 *
	 * @return <code>True</code> to show the tree columns, <code>false</code> otherwise.
	 */
	protected boolean hasColumns() {
		return columns != null && columns.length > 0;
	}

	/**
	 * Returns the number of levels to auto expand.
	 * If the method returns <code>0</code>, no auto expansion will happen
	 *
	 * @return The number of levels to auto expand or <code>0</code>.
	 */
	protected int getAutoExpandLevel() {
		return 2;
	}

	/**
	 * Creates the tree viewer layout data instance.
	 *
	 * @param viewer The tree viewer. Must not be <code>null</code>.
	 * @return The tree viewer layout data instance.
	 */
	protected Object doCreateTreeViewerLayoutData(TreeViewer viewer) {
		return new GridData(GridData.FILL_BOTH);
	}

	/**
	 * Creates the tree viewer label provider instance.
	 *
	 * @param viewer The tree viewer. Must not be <code>null</code>.
	 * @return The tree viewer label provider instance.
	 */
	protected ILabelProvider doCreateTreeViewerLabelProvider(TreeViewer viewer) {
		TreeViewerLabelProvider labelProvider = new TreeViewerLabelProvider(viewer);
		IWorkbench workbench = PlatformUI.getWorkbench();
		IDecoratorManager manager = workbench.getDecoratorManager();
		ILabelDecorator decorator = manager.getLabelDecorator();
		return new TreeViewerDecoratingLabelProvider(labelProvider,decorator);
	}

	/**
	 * Creates the tree viewer content provider instance.
	 *
	 * @param viewer The tree viewer. Must not be <code>null</code>.
	 * @return The tree viewer content provider instance.
	 */
	protected abstract ITreeContentProvider doCreateTreeViewerContentProvider(TreeViewer viewer);

	/**
	 * Creates the tree viewer comparator instance.
	 *
	 * @param viewer The tree viewer. Must not be <code>null</code>.
	 * @return The tree viewer comparator instance or <code>null</code> to turn of sorting.
	 */
	protected ViewerComparator doCreateTreeViewerComparator(TreeViewer viewer) {
		return new TreeViewerComparator();
	}

	/**
	 * Creates a new selection changed listener instance.
	 *
	 * @param viewer The tree viewer. Must not be <code>null</code>.
	 * @return The selection changed listener instance.
	 */
	protected abstract ISelectionChangedListener doCreateTreeViewerSelectionChangedListener(TreeViewer viewer);

	/**
	 * Create the context menu and toolbar groups.
	 *
	 * @param viewer The tree viewer instance. Must not be <code>null</code>.
	 */
	protected void createContributionItems(TreeViewer viewer) {
		Assert.isNotNull(viewer);

		// Create the menu manager
		MenuManager manager = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		// Attach the menu listener
		manager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			}
		});
		// All items are removed when menu is closing
		manager.setRemoveAllWhenShown(true);
		// Associated with the tree
		viewer.getTree().setMenu(manager.createContextMenu(viewer.getTree()));

		// Register the context menu at the parent workbench part site.
		if (getParentPart() != null && getParentPart().getSite() != null && getContextMenuId() != null) {
			getParentPart().getSite().registerContextMenu(getContextMenuId(), manager, viewer);
		}

		// The toolbar is a bit more complicated as we want to have the
		// toolbar placed within the section title.
		createToolbarContributionItem(viewer);
		new TreeViewerHeaderMenu(viewer.getTree()).create();
	}

	/**
	 * Returns the context menu id.
	 *
	 * @return The context menu id.
	 */
	protected abstract String getContextMenuId();

	/**
	 * Creates the toolbar within the section parent of the given tree viewer.
	 *
	 * @param viewer The tree viewer instance. Must not be <code>null</code>.
	 */
	protected void createToolbarContributionItem(TreeViewer viewer) {
		Assert.isNotNull(viewer);

		// Determine the section parent from the tree viewer
		Composite parent = viewer.getTree().getParent();
		while (parent != null && !(parent instanceof Section)) {
			parent = parent.getParent();
		}

		// We are done here if we cannot find a section parent or the parent is disposed
		if (parent == null || parent.isDisposed()) {
			return;
		}

		ToolBarManager toolbarManager = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL | SWT.RIGHT);
		// create the toolbar items
		createToolBarItems(toolbarManager);
		if (getParentPart() != null && getParentPart().getSite() != null && getContextMenuId() != null) {
			IMenuService service = (IMenuService) getParentPart().getSite().getService(IMenuService.class);
			if (service != null) {
				service.populateContributionManager(toolbarManager, "toolbar:" + this.getContextMenuId()); //$NON-NLS-1$
			}
		}		
		ToolBar toolbar = toolbarManager.createControl(parent);

		// The cursor within the toolbar shall change to an hand
		final Cursor handCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);
		toolbar.setCursor(handCursor);
		// Cursor needs to be explicitly disposed
		toolbar.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (handCursor.isDisposed() == false) {
					handCursor.dispose();
				}
			}
		});

		// If the parent composite is a forms section, set the toolbar
		// as text client to the section header
		if (parent instanceof Section) {
			Section section = (Section)parent;
			// Set the toolbar as text client
			section.setTextClient(toolbar);
		}
	}

	/**
	 * Create the toolbar items to be added to the toolbar. Override
	 * to add the wanted toolbar items.
	 * <p>
	 * <b>Note:</b> The toolbar items are added from left to right.
	 *
	 * @param toolbarManager The toolbar to add the toolbar items too. Must not be <code>null</code>.
	 */
	protected void createToolBarItems(ToolBarManager toolbarManager) {
		Assert.isNotNull(toolbarManager);
		toolbarManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		toolbarManager.add(new ConfigFilterAction(this));
		Action action = new Action(null, IAction.AS_PUSH_BUTTON){
			@Override
            public void run() {
				PlatformUI.getWorkbench().getHelpSystem().displayDynamicHelp();
            }
		};
		ImageDescriptor image = UIPlugin.getImageDescriptor(ImageConsts.VIEWER_HELP);
		action.setImageDescriptor(image);
		toolbarManager.add(action);
	}

	/**
	 * Get the current filter descriptors of this viewer.
	 *  
	 * @return The filter descriptors of this viewer.
	 */
	public FilterDescriptor[] getFilterDescriptors() {
		return filterDescriptors;
	}

	/**
	 * Get the current viewer columns of this viewer.
	 * 
	 * @return The current viewer columns.
	 */
	public ColumnDescriptor[] getViewerColumns() {
		return columns;
	}

	/**
	 * Returns the viewer instance.
	 *
	 * @return The viewer instance or <code>null</code>.
	 */
	public Viewer getViewer() {
		return viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (Viewer.class.isAssignableFrom(adapter)) {
			// We have to double check if our real viewer is assignable to
			// the requested Viewer class.
			Viewer viewer = getViewer();
			if (!adapter.isAssignableFrom(viewer.getClass())) {
				viewer = null;
			}
			return viewer;
		}

		return super.getAdapter(adapter);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
    public void widgetSelected(SelectionEvent e) {
		Assert.isTrue(e.getSource() instanceof TreeColumn);
		TreeColumn treeColumn = (TreeColumn) e.getSource();
		viewer.getTree().setSortColumn(treeColumn);
		ColumnDescriptor column = (ColumnDescriptor) treeColumn.getData();
		column.setAscending(!column.isAscending());
		viewer.getTree().setSortDirection(column.isAscending() ? SWT.UP : SWT.DOWN);
		Object[] expandedElements = viewer.getExpandedElements();
		viewer.refresh();
		viewer.setExpandedElements(expandedElements);
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
    public void widgetDefaultSelected(SelectionEvent e) {
	}
}
