/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.trees;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.tcf.te.core.interfaces.IViewerInput;
import org.eclipse.tcf.te.runtime.utils.Host;
import org.eclipse.tcf.te.ui.WorkbenchPartControl;
import org.eclipse.tcf.te.ui.forms.CustomFormToolkit;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;


/**
 * Abstract tree control implementation.
 */
public abstract class AbstractTreeControl extends WorkbenchPartControl implements SelectionListener {
	// Reference to the tree viewer instance
	private TreeViewer viewer;
	// Reference to the selection changed listener
	private ISelectionChangedListener selectionChangedListener;
	// The descriptors of the viewer filters configured for this viewer.
	private FilterDescriptor[] filterDescriptors;
	// The tree viewer columns of this viewer.
	private ColumnDescriptor[] columns;
	// The state of the tree viewer used to restore and save the the tree viewer's state.
	private TreeViewerState viewerState;
	// The action to configure the filters.
	private ConfigFilterAction configFilterAction;
	// The menu manager
	private MenuManager manager;

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
	public AbstractTreeControl(IWorkbenchPart parent) {
		super(parent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.WorkbenchPartControl#dispose()
	 */
	@Override
	public void dispose() {
		saveViewerState();
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
		if(filterDescriptors != null) {
			for(FilterDescriptor filterDescriptor : filterDescriptors) {
				if(filterDescriptor.getImage() != null) {
					filterDescriptor.getImage().dispose();
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
		doConfigureTreeViewer(viewer);

		// Prepare popup menu and toolbar
		doCreateContributionItems(viewer);
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
	protected void doConfigureTreeViewer(TreeViewer viewer) {
		Assert.isNotNull(viewer);

		viewer.setAutoExpandLevel(getAutoExpandLevel());

		viewer.setLabelProvider(doCreateTreeViewerLabelProvider(viewer));

		final ITreeContentProvider contentProvider = doCreateTreeViewerContentProvider(viewer);
		InvocationHandler handler = new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				if (method.getName().equals("inputChanged")) { //$NON-NLS-1$
					onInputChanged(args[1], args[2]);
				}
				return method.invoke(contentProvider, args);
			}
		};
		ClassLoader classLoader = getClass().getClassLoader();
		Class<?>[] interfaces = new Class[] { ITreeContentProvider.class };
		ITreeContentProvider proxy = (ITreeContentProvider) Proxy.newProxyInstance(classLoader, interfaces, handler);
		viewer.setContentProvider(proxy);

		viewer.setComparator(doCreateTreeViewerComparator(viewer));

		viewer.getTree().setLayoutData(doCreateTreeViewerLayoutData(viewer));

		// Attach the selection changed listener
		selectionChangedListener = doCreateTreeViewerSelectionChangedListener(viewer);
		if (selectionChangedListener != null) {
			viewer.addSelectionChangedListener(selectionChangedListener);
		}

		// Define an editor activation strategy for the common viewer so as to be invoked only programmatically.
		ColumnViewerEditorActivationStrategy activationStrategy = new TreeViewerEditorActivationStrategy(getViewerId(), viewer);
		TreeViewerEditor.create(viewer, null, activationStrategy, ColumnViewerEditor.DEFAULT);
	}

	/**
	 * Handle the event when the new input is set. Get the viewer's state
	 * and update the state of the viewer's columns and filters.
	 *
	 * @param oldInput the old input.
	 * @param newInput The new input.
	 */
	void onInputChanged(Object oldInput, Object newInput) {
		columns = doCreateViewerColumns(newInput);
		filterDescriptors = doCreateFilterDescriptors(newInput);
		if (isStatePersistent()) {
			updateViewerState(newInput);
		}
		doCreateTreeColumns(viewer);
		viewer.getTree().setHeaderVisible(true);
		updateFilters();
		new TreeViewerHeaderMenu(this).create();
		if (configFilterAction != null) configFilterAction.updateEnablement();
	}

	/**
	 * Update the viewer state using the states from the viewerState which
	 * is retrieved or created based on the input.
	 *
	 * @param newInput The new input of the viewer.
	 */
	private void updateViewerState(Object newInput) {
		IViewerInput viewerInput = ViewerStateManager.getViewerInput(newInput);
		if (viewerInput != null) {
			String inputId = viewerInput.getInputId();
			if (inputId != null) {
				inputId = getViewerId() + "." + inputId; //$NON-NLS-1$
				viewerState = ViewerStateManager.getInstance().getViewerState(inputId);
				if (viewerState == null) {
					viewerState = ViewerStateManager.createViewerState(columns, filterDescriptors);
					ViewerStateManager.getInstance().putViewerState(inputId, viewerState);
				}
				else {
					viewerState.updateColumnDescriptor(columns);
					viewerState.updateFilterDescriptor(filterDescriptors);
				}
			}
		}
    }

	/**
	 * Save the viewer's state.
	 */
	private void saveViewerState() {
		if (isStatePersistent() && viewerState != null) {
    		viewerState.updateColumnState(columns);
    		viewerState.updateFilterState(filterDescriptors);
		}
	}

	/**
	 * Update the filter's state using the latest filter descriptors.
	 */
	void updateFilterState() {
		if (isStatePersistent() && viewerState != null) {
    		viewerState.updateFilterState(filterDescriptors);
		}
	}

	/**
	 * Show or hide the specified column. Return true if the visible
	 * state has changed.
	 *
	 * @param column The column to be changed.
	 * @param visible The new visible value.
	 * @return true if the state has changed.
	 */
	boolean setColumnVisible(ColumnDescriptor column, boolean visible) {
	    if (column.isVisible() && !visible) {
	    	TreeColumn treeColumn = column.getTreeColumn();
	    	treeColumn.dispose();
	    	column.setTreeColumn(null);
		    column.setVisible(visible);
		    return true;
	    }
	    else if (!column.isVisible() && visible) {
	    	TreeColumn treeColumn = doCreateTreeColumn(column, false);
	    	column.setTreeColumn(treeColumn);
		    column.setVisible(visible);
		    return true;
	    }
		return false;
    }

	/**
	 * Return if this tree viewer's state is persistent. If it is persistent,
	 * then its viewer state will be persisted during different session.
	 *
	 * @return true if the viewer's state is persistent.
	 */
	protected boolean isStatePersistent() {
	    return true;
    }

	/**
	 * Create the tree viewer columns from the viewers extension.
	 * Subclass may override it to provide its customized viewer columns.
	 *
	 * @param newInput the input when the columns are created.
	 * @return The tree viewer columns.
	 */
	protected ColumnDescriptor[] doCreateViewerColumns(Object newInput) {
		if(columns == null) {
			TreeViewerExtension viewerExtension = new TreeViewerExtension(getViewerId());
			columns = viewerExtension.parseColumns(newInput);
		}
		return columns;
	}

	/**
	 * Create the viewer filters from the viewers extension. Subclass may
	 * override it to provide its customized viewer filters.
	 *
	 * @param newInput the input when the filters are initialized.
	 * @return The filter descriptors for the viewer.
	 */
	protected FilterDescriptor[] doCreateFilterDescriptors(Object newInput) {
		if(filterDescriptors == null) {
			TreeViewerExtension viewerExtension = new TreeViewerExtension(getViewerId());
			filterDescriptors = viewerExtension.parseFilters(newInput);
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
	protected void doCreateTreeColumns(TreeViewer viewer) {
		Assert.isTrue(columns != null && columns.length > 0);
		List<ColumnDescriptor> visibleColumns = new ArrayList<ColumnDescriptor>();
		for (ColumnDescriptor column : columns) {
			if (column.isVisible()) visibleColumns.add(column);
		}
		Collections.sort(visibleColumns, new Comparator<ColumnDescriptor>(){
			@Override
            public int compare(ColumnDescriptor o1, ColumnDescriptor o2) {
				return o1.getOrder() < o2.getOrder() ? -1 : (o1.getOrder() > o2.getOrder() ? 1 : 0);
            }});
		for(ColumnDescriptor visibleColumn : visibleColumns) {
			doCreateTreeColumn(visibleColumn, true);
		}
		if(!Host.isWindowsHost()) {
			Tree tree = viewer.getTree();
			TreeColumn column = new TreeColumn(tree, SWT.LEFT);
			column.setWidth(1);
		}
		// Set the default sort column to the first column (the tree column).
		Assert.isTrue(viewer.getTree().getColumnCount() > 0);
		TreeColumn treeColumn = viewer.getTree().getColumn(0);
		ColumnDescriptor column = (ColumnDescriptor) treeColumn.getData();
		if (column != null) {
			viewer.getTree().setSortColumn(treeColumn);
			viewer.getTree().setSortDirection(column.isAscending() ? SWT.UP : SWT.DOWN);
		}
	}

	/**
	 * Create the tree column described by the specified colum descriptor.
	 *
	 * @param column The column descriptor.
	 * @param append If the new column should be appended.
	 * @return The tree column created.
	 */
	TreeColumn doCreateTreeColumn(final ColumnDescriptor column, boolean append) {
		Tree tree = viewer.getTree();
		final TreeColumn treeColumn = append ? new TreeColumn(tree, column.getStyle()) :
			new TreeColumn(tree, column.getStyle(), getColumnIndex(column));
	    treeColumn.setData(column);
	    treeColumn.setText(column.getName());
	    treeColumn.setToolTipText(column.getDescription());
	    treeColumn.setAlignment(column.getAlignment());
	    treeColumn.setImage(column.getImage());
	    treeColumn.setMoveable(column.isMoveable());
	    treeColumn.setResizable(column.isResizable());
	    treeColumn.setWidth(column.getWidth());
	    treeColumn.addSelectionListener(this);
	    treeColumn.addControlListener(new ControlAdapter(){

			@Override
            public void controlMoved(ControlEvent e) {
				columnMoved();
            }

			@Override
            public void controlResized(ControlEvent e) {
				column.setWidth(treeColumn.getWidth());
            }});
	    column.setTreeColumn(treeColumn);
	    return treeColumn;
    }

	/**
	 * Called when a column is moved. Store the column's order.
	 */
	void columnMoved() {
		Tree tree = viewer.getTree();
		TreeColumn[] treeColumns = tree.getColumns();
		if(treeColumns != null && treeColumns.length > 0) {
			int[] orders = tree.getColumnOrder();
			for(int i=0;i<orders.length;i++) {
				TreeColumn treeColumn = treeColumns[orders[i]];
				ColumnDescriptor column = (ColumnDescriptor) treeColumn.getData();
				if (column != null) {
					column.setOrder(i);
				}
			}
		}
	}

	/**
	 * Get the column index of the specified column. The column index
	 * equals to the count of the visible columns before this column.
	 *
	 * @param column The column descriptor.
	 * @return The column index.
	 */
	private int getColumnIndex(ColumnDescriptor column) {
		Assert.isTrue(columns != null);
		int visibleCount = 0;
		for(int i=0;i<columns.length;i++) {
			if(columns[i] == column)
				break;
			if(columns[i].isVisible()) {
				visibleCount++;
			}
		}
		return visibleCount;
	}

	/**
	 * Get the tree viewer's id. This viewer id is used by
	 * viewer extension to define columns and filters.
	 *
	 * @return This viewer's id or null.
	 */
	protected abstract String getViewerId();

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
	    GridData data = new GridData(GridData.FILL_BOTH);
	    data.widthHint = 0;
	    data.heightHint = 0;
	    return data;
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
		return new TreeViewerDecoratingLabelProvider(viewer, labelProvider,decorator);
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
	protected void doCreateContributionItems(TreeViewer viewer) {
		Assert.isNotNull(viewer);

		// Create the menu manager
		manager = new MenuManager("#PopupMenu"); //$NON-NLS-1$
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
	}

	/**
	 * Get the context menu manager.
	 *
	 * @return the context menu manager.
	 */
	public MenuManager getContextMenuManager() {
		return manager;
	}

	/**
	 * Create the toolbar items to be added to the toolbar. Override
	 * to add the wanted toolbar items.
	 * <p>
	 * <b>Note:</b> The toolbar items are added from left to right.
	 *
	 * @param toolbarManager The toolbar to add the toolbar items too. Must not be <code>null</code>.
	 */
	public void createToolbarContributionItems(IToolBarManager toolbarManager) {
		toolbarManager.add(new CollapseAllAction(this));
		toolbarManager.add(configFilterAction = new ConfigFilterAction(this));
	}

	/**
	 * Get the current filter descriptors of this viewer.
	 *
	 * @return The filter descriptors of this viewer.
	 */
	public FilterDescriptor[] getFilterDescriptors() {
		return filterDescriptors != null ? Arrays.copyOf(filterDescriptors, filterDescriptors.length) : new FilterDescriptor[0];
	}

	/**
	 * Get the current viewer columns of this viewer.
	 *
	 * @return The current viewer columns.
	 */
	public ColumnDescriptor[] getViewerColumns() {
		return columns != null ? Arrays.copyOf(columns, columns.length) : new ColumnDescriptor[0];
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
		ColumnDescriptor column = (ColumnDescriptor) treeColumn.getData();
		if (column != null) {
			viewer.getTree().setSortColumn(treeColumn);
			column.setAscending(!column.isAscending());
			viewer.getTree().setSortDirection(column.isAscending() ? SWT.UP : SWT.DOWN);
			Object[] expandedElements = viewer.getExpandedElements();
			viewer.refresh();
			viewer.setExpandedElements(expandedElements);
		}
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
    public void widgetDefaultSelected(SelectionEvent e) {
	}
}