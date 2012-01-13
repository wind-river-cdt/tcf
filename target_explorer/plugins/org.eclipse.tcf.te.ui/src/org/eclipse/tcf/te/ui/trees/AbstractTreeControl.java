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


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.tcf.te.runtime.utils.Host;
import org.eclipse.tcf.te.ui.WorkbenchPartControl;
import org.eclipse.tcf.te.ui.activator.UIPlugin;
import org.eclipse.tcf.te.ui.forms.CustomFormToolkit;
import org.eclipse.tcf.te.ui.interfaces.IViewerInput;
import org.eclipse.tcf.te.ui.interfaces.ImageConsts;
import org.eclipse.tcf.te.ui.nls.Messages;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.part.MultiPageSelectionProvider;


/**
 * Abstract tree control implementation.
 */
public abstract class AbstractTreeControl extends WorkbenchPartControl implements SelectionListener,
								IDoubleClickListener, IPropertyChangeListener, ISelectionChangedListener, FocusListener {
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
	// The tool bar manager
	private ToolBarManager toolbarManager;

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

		final ITreeContentProvider contentProvider = doCreateTreeViewerContentProvider(viewer);
		InvocationHandler handler = new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				if (method.getName().equals("inputChanged")) { //$NON-NLS-1$
					onInputChanged(args[1], args[2]);
				} else if(method.getName().equals("dispose")) { //$NON-NLS-1$
					onContentProviderDisposed();
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
		viewer.addSelectionChangedListener(this);
		selectionChangedListener = doCreateTreeViewerSelectionChangedListener(viewer);
		if (selectionChangedListener != null) {
			viewer.addSelectionChangedListener(selectionChangedListener);
		}

		viewer.addDoubleClickListener(this);

		// Set the help context.
		String helpContextId = getHelpId();
		if (helpContextId != null) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getTree(), helpContextId);
		}
		Tree tree = viewer.getTree();
		tree.addFocusListener(this);
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
		if(oldInput != null) {
			uninstallPropertyChangeListener(oldInput);
		}
		columns = doCreateViewerColumns(newInput);
		filterDescriptors = doCreateFilterDescriptors(newInput);
		if (isStatePersistent()) {
			updateViewerState(newInput);
		}
		createTreeColumns(viewer);
		viewer.getTree().setHeaderVisible(true);
		updateFilters();
		new TreeViewerHeaderMenu(this).create();
		configFilterAction.updateEnablement();
		if(newInput != null) {
			installPropertyChangeListener(newInput);
		}
	}

	/**
	 * Handle the event when the content provider is disposed.
	 * Un-install the property change listener that has been added
	 * to the input.
	 *
	 * @param oldInput the old input.
	 * @param newInput The new input.
	 */
	void onContentProviderDisposed() {
		Object input = viewer.getInput();
		if(input != null) {
			uninstallPropertyChangeListener(input);
		}
	}

	/**
	 * Uninstall the property change listener from the specified input.
	 *
	 * @param input The input of the tree viewer.
	 */
	private void uninstallPropertyChangeListener(Object input) {
	    IViewerInput viewerInput = ViewerStateManager.getViewerInput(input);
		if(viewerInput != null) {
			viewerInput.removePropertyChangeListener(this);
		}
	}

	/**
	 * Install the property change listener to the input of the tree viewer.
	 *
	 * @param input The input of the tree viewer.
	 */
	private void installPropertyChangeListener(Object input) {
	    IViewerInput viewerInput = ViewerStateManager.getViewerInput(input);
		if(viewerInput != null) {
			viewerInput.addPropertyChangeListener(this);
		}
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
	    	TreeColumn treeColumn = createTreeColumn(column, false);
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
	protected void createTreeColumns(TreeViewer viewer) {
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
			createTreeColumn(visibleColumn, true);
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
	TreeColumn createTreeColumn(final ColumnDescriptor column, boolean append) {
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

		toolbarManager = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL | SWT.RIGHT);
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
		toolbarManager.add(configFilterAction = new ConfigFilterAction(this));
		Action action = new Action(null, IAction.AS_PUSH_BUTTON){
			@Override
            public void run() {
				PlatformUI.getWorkbench().getHelpSystem().displayDynamicHelp();
            }
		};
		action.setToolTipText(Messages.AbstractTreeControl_HelpTooltip);
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

	/**
	 * Listens to the double-click event of the tree and expand or collapse
	 * the tree by default. Subclass may override this method to invoke certain
	 * command.
	 *
	 * @param event the double click event
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(DoubleClickEvent)
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
                public void run() throws Exception {
					ISelection selection = event.getSelection();
					EvaluationContext ctx = new EvaluationContext(null, selection);
					ctx.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME, selection);
					ctx.addVariable(ISources.ACTIVE_MENU_SELECTION_NAME, selection);
					ctx.addVariable(ISources.ACTIVE_WORKBENCH_WINDOW_NAME, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
					IWorkbenchPart part = getParentPart();
					if (part != null) {
						IWorkbenchPartSite site = part.getSite();
						ctx.addVariable(ISources.ACTIVE_PART_ID_NAME, site.getId());
						ctx.addVariable(ISources.ACTIVE_PART_NAME, part);
						ctx.addVariable(ISources.ACTIVE_SITE_NAME, site);
						ctx.addVariable(ISources.ACTIVE_SHELL_NAME, site.getShell());
					}
					ExecutionEvent executionEvent = new ExecutionEvent(command, Collections.EMPTY_MAP, part, ctx);
					command.executeWithChecks(executionEvent);
                }});
		} else {
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			Object element = selection.getFirstElement();
			TreeViewer viewer = (TreeViewer) getViewer();
			if (viewer.isExpandable(element)) {
				viewer.setExpandedState(element, !viewer.getExpandedState(element));
			}
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		ToolBar toolbar = toolbarManager.getControl();
		if (!toolbar.isDisposed()) {
			Display display = toolbar.getDisplay();
			if (display.getThread() == Thread.currentThread()) {
				IContributionItem[] items = toolbarManager.getItems();
				for (IContributionItem item : items) {
					item.update();
				}
			}
			else {
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						propertyChange(event);
					}
				});
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
	 */
	@Override
    public void focusGained(FocusEvent e) {
		propagateSelection();
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
	 */
	@Override
    public void focusLost(FocusEvent e) {
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		propagateSelection();
	}

	/**
	 * Propagate the current selection to the editor's selection provider.
	 */
	private void propagateSelection() {
	    IWorkbenchPart parent = getParentPart();
		if (parent != null) {
			IWorkbenchPartSite site = parent.getSite();
			if (site != null) {
				ISelection selection = getViewer().getSelection();
				ISelectionProvider selectionProvider = site.getSelectionProvider();
				// If the parent control is already disposed, we have no real chance of
				// testing for it. Catch the SWT exception here just in case.
				try {
					selectionProvider.setSelection(selection);
					if (selectionProvider instanceof MultiPageSelectionProvider) {
						SelectionChangedEvent changedEvent = new SelectionChangedEvent(selectionProvider, selection);
						((MultiPageSelectionProvider) selectionProvider).firePostSelectionChanged(changedEvent);
					}
				} catch (SWTException e) {
					/* ignored on purpose */
				}
			}
		}
    }
}