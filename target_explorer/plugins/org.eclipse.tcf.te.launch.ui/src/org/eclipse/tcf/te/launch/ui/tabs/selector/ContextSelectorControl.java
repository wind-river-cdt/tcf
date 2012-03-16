/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.tabs.selector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
import org.eclipse.tcf.te.runtime.services.ServiceManager;
import org.eclipse.tcf.te.runtime.services.interfaces.IPropertiesAccessService;
import org.eclipse.tcf.te.ui.controls.AbstractDecoratedDialogPageControl;
import org.eclipse.tcf.te.ui.jface.interfaces.IValidatingContainer;
import org.eclipse.tcf.te.ui.swt.SWTControlUtil;
import org.eclipse.tcf.te.ui.views.interfaces.IUIConstants;
import org.eclipse.tcf.te.ui.views.internal.ViewRoot;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.internal.navigator.NavigatorContentService;
import org.eclipse.ui.navigator.CommonViewerSorter;

/**
 * Context selector control.
 * <p>
 * Allows to present a configurable set of elements from the data model from which the user can
 * select one or more elements.
 * <p>
 * Default properties:
 * <ul>
 * <li>PROPERTY_SHOW_GHOST_MODEL_NODES = false</li>
 * </ul>
 */
@SuppressWarnings("restriction")
public class ContextSelectorControl extends AbstractDecoratedDialogPageControl implements ISelectionProvider {

	/**
	 * Property: If set to <code>true</code>, ghost model nodes will be shown within the tree.
	 */
	public static final String PROPERTY_SHOW_GHOST_MODEL_NODES = "showGhostModelNodes"; //$NON-NLS-1$

	/**
	 * List of selection changed listeners.
	 */
	private final List<ISelectionChangedListener> selectionListeners = new ArrayList<ISelectionChangedListener>();

	/**
	 * Control properties. See predefined property constants within the class and subclass
	 * implementations. Property changes are not notified.
	 */
	private final IPropertiesContainer properties = new PropertiesContainer();

	// Reference to the tree viewer control used.
	private TreeViewer viewer;
	// The current selection within the tree viewer.
	/* default */ ISelection selection;

	// Reference to the navigator content service used
	private NavigatorContentService contentService;

	/**
	 * Constant to return an empty viewer filter array.
	 */
	protected final static ViewerFilter[] NO_FILTERS = new ViewerFilter[0];

	/**
	 * Constant to return an empty selected model context array.
	 */
	protected final static IModelNode[] NO_CONTEXTS = new IModelNode[0];

	// Currently active set of viewer filters.
	private ViewerFilter[] filters;
	// Currently active checkbox tree viewer check state listener
	private ICheckStateListener listener;

	/**
	 * Default implementation of the context selector controls tree viewer.
	 */
	protected class ContextSelectorTreeViewer extends ContainerCheckedTreeViewer {

		/**
		 * Constructor.
		 *
		 * @param parent The parent control.
		 * @param style The SWT style bits used to create the tree.
		 */
		public ContextSelectorTreeViewer(Composite parent, int style) {
			// make sure that the passed in style bits are not contaminated
			// by the CheckboxTreeViewer.
			this(new Tree(parent, style));
		}

		/**
		 * Constructor.
		 *
		 * @param parent The parent control.
		 */
		public ContextSelectorTreeViewer(Composite parent) {
			super(parent);
		}

		/**
		 * Constructor.
		 *
		 * @param tree The tree control.
		 */
		public ContextSelectorTreeViewer(Tree tree) {
			super(tree);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.TreeViewer#isExpandable(java.lang.Object)
		 */
		@Override
		public boolean isExpandable(Object element) {
			boolean expandable = super.isExpandable(element);
			// adjust the expandable state if the element does not have
			// children after the filtering.
			if (expandable) {
				expandable = getFilteredChildren(element).length > 0;
			}
			return expandable;
		}

		/**
		 * Returns the child items for the given element if any.
		 *
		 * @param element The element.
		 * @return The child items of the element or <code>null</code> if none.
		 */
		public Item[] getChildren(Object element) {
			Widget item = findItem(element);
			return getChildren(item);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.dialogs.ContainerCheckedTreeViewer#doCheckStateChanged(java.lang.Object)
		 */
		@Override
		protected void doCheckStateChanged(Object element) {
			// Our ghost model elements requires some special handling, as
			// these elements should be never checked fully. Try to determine
			// if we have to double check on the parents state.
			boolean skipDoubleCheckParentState = false;

			// If the element isn't one of our model elements, pass on to
			// to super implementation
			skipDoubleCheckParentState |= !(element instanceof IModelNode);

			// If the element is one of our model elements and it is not
			// a ghost node, the parent can't be a ghost node either.
			if (!skipDoubleCheckParentState) {
				skipDoubleCheckParentState |= !isGhost((IModelNode) element);
			}

			// If the element is a ghost model element, then we have to check
			// on the parent as well.
			if (!skipDoubleCheckParentState) {
				skipDoubleCheckParentState |= ((IModelNode) element).getParent() == null || !isGhost(((IModelNode) element).getParent());
			}

			// Call the super implementation to check the item and
			// updating parents and children the first time
			super.doCheckStateChanged(element);

			if (!skipDoubleCheckParentState) {
				// Get the tree item for the element and check the parent items
				// for being associated with ghost model elements.
				Widget item = findItem(element);
				if (item instanceof TreeItem) {
					TreeItem treeItem = ((TreeItem) item).getParentItem();
					while (treeItem != null) {
						Object data = treeItem.getData();

						// If a child item is checked, otherwise we wouldn't come here, and this
						// parent item isn't expanded, we must(!) expand the item now here by force,
						// otherwise we will loose the checked states of the children elements!
						if (!treeItem.getExpanded()) {
							treeItem.setExpanded(true);
						}

						// Decide if we shall gray the checked state.
						// --> The checked state is grayed if the item is a ghost.
						boolean isGhost = data instanceof IModelNode && isGhost((IModelNode) data);
						if (!treeItem.getGrayed() && isGhost) {
							treeItem.setGrayed(true);
						}

						// go one level up in the hierarchy
						treeItem = treeItem.getParentItem();
					}
				}
			}
		}
	}

	/**
	 * Returns if or if not the given model node is a ghost node.
	 *
	 * @param node The model node. Must not be <code>null</code>.
	 * @return <code>True</code> if the node is a ghost node, <code>false</code> otherwise.
	 */
	/* default */ boolean isGhost(IModelNode node) {
		Assert.isNotNull(node);

		IPropertiesAccessService service = ServiceManager.getInstance().getService(node, IPropertiesAccessService.class);
		if (service != null) {
			Object value = service.getProperty(node, IModelNode.PROPERTY_IS_GHOST);
			if (value instanceof Boolean) {
				return ((Boolean)value).booleanValue();
			} else if (value instanceof String) {
				return Boolean.valueOf((String)value).booleanValue();
			}
			return false;
		}

		try {
			return node.isProperty(IModelNode.PROPERTY_IS_GHOST, true);
		} catch (AssertionFailedException e) { /* ignored on purpose */ }

		return false;
	}

	/**
	 * Default implementation of the context selector controls check state listener.
	 */
	protected class ContextSelectedCheckStateListener implements ICheckStateListener {
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
		 */
		@Override
		public void checkStateChanged(CheckStateChangedEvent event) {
			Object element = event.getElement();
			if (element instanceof IModelNode) {
				onModelNodeCheckStateChanged((IModelNode) element, event.getChecked());
			}
		}
	}

	/**
	 * Default implementation of the context selector controls viewer filter.
	 */
	protected class ContextSelectorViewerFilter extends ViewerFilter {

		/**
		 * Returns if or if not ghost model elements should be visible within the model context
		 * selector controls tree viewer. Default is not to show the ghost model elements.
		 *
		 * @return <code>True</code> to show the ghost model elements, <code>false</code> otherwise.
		 */
		protected boolean doShowGhostModelElements() {
			return getPropertiesContainer().getBooleanProperty(PROPERTY_SHOW_GHOST_MODEL_NODES);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (element instanceof IModelNode) {
				if (isGhost((IModelNode) element)) {
					return doShowGhostModelElements();
				}
			}

			return true;
		}
	}

	/**
	 * Constructor.
	 *
	 * @param parentPage The parent target connection page this control is embedded in. Might be
	 *            <code>null</code> if the control is not associated with a page.
	 */
	public ContextSelectorControl(IDialogPage parentPage) {
		super(parentPage);
		selectionListeners.clear();
		// initialize the properties
		initializeProperties(getPropertiesContainer());
	}

	/**
	 * Returns the properties container associated with this control.
	 *
	 * @return The properties container.
	 */
	public final IPropertiesContainer getPropertiesContainer() {
		return properties;
	}

	/**
	 * Initialize the properties associated with this control.
	 *
	 * @param properties The properties container. Must not be <code>null</code>.
	 */
	protected void initializeProperties(IPropertiesContainer properties) {
		Assert.isNotNull(properties);
		properties.setProperty(PROPERTY_SHOW_GHOST_MODEL_NODES, false);
	}

	/**
	 * Returns the default title text which should be used by the enclosing controls or windows if
	 * these controls do need to set a title.
	 * <p>
	 * The default implementation returns <code>null</code>.
	 *
	 * @return The default title text or <code>null</code> if none.
	 */
	public String getDefaultTitle() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.BaseControl#dispose()
	 */
	@Override
	public void dispose() {
		// Dispose the navigator content service
		if (contentService != null) {
			contentService.dispose();
			contentService = null;
		}

		viewer = null;

		super.dispose();
	}

	/**
	 * Returns the list of checked model node contexts. The elements are in the order they appear
	 * within the tree from top to bottom. The client of the control is in charge detecting any type
	 * of hierarchy or other relationships between the elements.
	 *
	 * @return The list of checked model node contexts or an empty list of none.
	 */
	public IModelNode[] getCheckedModelContexts() {
		// This method does return something useful only if it is a checkable
		// tree viewer and the check style is set for the tree.
		if (getViewer() instanceof ContainerCheckedTreeViewer && (getTreeViewerStyle() & SWT.CHECK) != 0) {
			ContainerCheckedTreeViewer viewer = (ContainerCheckedTreeViewer) getViewer();
			// Get the list of checked elements. Checked elements includes the grayed elements
			List<?> checked = viewer.getCheckedElements() != null ? Arrays.asList(viewer.getCheckedElements()) : Collections.emptyList();
			// Get the list of grayed elements.
			List<?> grayed = viewer.getGrayedElements() != null ? Arrays.asList(viewer.getGrayedElements()) : Collections.emptyList();

			// There must be at least one element checked
			if (!checked.isEmpty()) {
				List<IModelNode> contexts = new ArrayList<IModelNode>();
				for (Object element : checked) {
					// If the context is a model node and the parent container node is fully
					// checked, drop the model node and the use the container node only.
					if (element instanceof IModelNode) {
						IModelNode node = (IModelNode) element;

						// Determine the parent node
						IPropertiesAccessService service = ServiceManager.getInstance().getService(node, IPropertiesAccessService.class);
						IModelNode parent = service != null ? (IModelNode)service.getParent(node) : node.getParent();

						if (parent != null && checked.contains(parent) && !grayed.contains(parent)) {
							continue;
						}
					}

					// If the element is a model node and not grayed,
					// add the element to the list of checked contexts.
					if (element instanceof IModelNode && !grayed.contains(element)) {
						contexts.add((IModelNode) element);
					}
				}
				return contexts.toArray(new IModelNode[contexts.size()]);
			}
		}
		return NO_CONTEXTS;
	}

	/**
	 * Sets the list of checked model node contexts.
	 *
	 * @param contexts The list of model node contexts. Must not be <code>null</code>.
	 */
	public void setCheckedModelContexts(IModelNode[] contexts) {
		Assert.isNotNull(contexts);

		// This method does nothing if the tree viewer isn't a checkable tree viewer.
		if (getViewer() instanceof ContainerCheckedTreeViewer && (getTreeViewerStyle() & SWT.CHECK) != 0) {
			ContainerCheckedTreeViewer viewer = (ContainerCheckedTreeViewer) getViewer();
			// Set the checked elements. This will trigger the validation of the
			// checked state of all the parent and children elements.
			viewer.setCheckedElements(contexts);
			// Make sure that at least the first checked element is visible to the user
			if (contexts.length > 0) {
				viewer.setSelection(new StructuredSelection(contexts[0]), true);
			}
		}
	}

	/**
	 * Returns the controls associated tree viewer.
	 *
	 * @return The tree viewer instance or <code>null</code> if not created yet.
	 */
	public final TreeViewer getViewer() {
		return viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.controls.BaseControl#setupPanel(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void setupPanel(Composite parent) {
		super.setupPanel(parent);

		// Create the container composite for the tree control.
		Composite composite = doCreateTopContainerComposite(parent);
		Assert.isNotNull(composite);

		// Create the viewer
		viewer = createTreeViewerControl(composite);

		// And now configure the listeners
		configureControlListener();

		// Trigger a selection changed event to give listeners
		// a chance to initialize their enabled state correctly
		viewer.setSelection(viewer.getSelection());
	}

	/**
	 * Create the top container composite.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @return The top container composite. Must not be <code>null</code>.
	 */
	protected Composite doCreateTopContainerComposite(Composite parent) {
		Assert.isNotNull(parent);

		// Set the default layout data attributes for the composite
		int style = GridData.FILL_BOTH;
		int heightHint = SWT.DEFAULT;

		// Fallback to standard non-form controls and create a composite which extends
		// in both directions and has no margins
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		GridData layoutData = new GridData(style);
		layoutData.heightHint = heightHint;
		composite.setLayoutData(layoutData);

		return composite;
	}

	/**
	 * Creates the tree viewer control. Override to return a custom tree viewer implementation.
	 *
	 * @param parent The parent composite of the tree viewer. Must not be <code>null</code>.
	 * @return The tree viewer control. Must be never <code>null</code>.
	 */
	protected TreeViewer createTreeViewerControl(Composite parent) {
		Assert.isNotNull(parent);

		CheckboxTreeViewer viewer = doCreateNewTreeViewerControl(parent, getTreeViewerStyle());
		doConfigureTreeViewerControl(viewer);

		viewer.setInput(getInitialViewerInput());
		doEnableControl(viewer);

		return viewer;
	}

	/**
	 * Creates a new instance of the tree viewer control to use. This method will be called from
	 * {@link #createTreeViewerControl(Composite)} if creating the tree viewer instance.
	 *
	 * @param parent The parent composite of the tree viewer. Must not be <code>null</code>.
	 * @param style The SWT style bits.
	 *
	 * @return The tree viewer instance. Must not be <code>null</code>.
	 */
	protected CheckboxTreeViewer doCreateNewTreeViewerControl(Composite parent, int style) {
		return new ContextSelectorTreeViewer(parent, style);
	}

	/**
	 * Configure the tree viewer control.
	 *
	 * @param viewer The tree viewer instance. Must not be <code>null</code>.
	 */
	protected void doConfigureTreeViewerControl(CheckboxTreeViewer viewer) {
		Assert.isNotNull(viewer);

		viewer.setUseHashlookup(true);

		doConfigureTreeLayoutData(viewer.getTree());

		// Create the navigator content service
		contentService = new NavigatorContentService(IUIConstants.ID_EXPLORER, viewer);
		Assert.isNotNull(contentService);

		viewer.setContentProvider(contentService.createCommonContentProvider());
		viewer.setLabelProvider(contentService.createCommonLabelProvider());
		viewer.setSorter(doCreateViewerSorter());

		if ((getTreeViewerStyle() & SWT.CHECK) != 0 && getViewerCheckStateListener(viewer) != null) {
			viewer.addCheckStateListener(getViewerCheckStateListener(viewer));
		}

		if (hasViewerFilters()) {
			viewer.setFilters(getViewerFilters());
		}
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				selection = event.getSelection();
				fireSelectionChanged();
			}
		});
		doCreateControlDecoration(viewer.getTree());
	}

	/**
	 * Configure the tree's layout data. The layout data set to the tree must be of type
	 * <code>GridData</code>.
	 *
	 * @param tree The tree to configure. Must not be <code>null</code>.
	 */
	protected void doConfigureTreeLayoutData(Tree tree) {
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.heightHint = 150;
		tree.setLayoutData(layoutData);
	}

	/**
	 * Returns the style bits to apply to the tree viewer.
	 * <p>
	 * The default set tree viewer style bits are:
	 * <ul>
	 * <li>SWT.SINGLE</li>
	 * <li>SWT.H_SCROLL</li>
	 * <li>SWT.V_SCROLL</li>
	 * <li>SWT.BORDER</li>
	 *
	 * @return The style bits to apply to the tree viewer.
	 */
	protected int getTreeViewerStyle() {
		return SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER;
	}

	/**
	 * Creates the viewer sorter instance to associated to the controls tree viewer.
	 *
	 * @return The viewer sorter to associate or <code>null</code> if none.
	 */
	protected ViewerSorter doCreateViewerSorter() {
		return new CommonViewerSorter();
	}

	/**
	 * Returns if or if not to associate viewer filters to the controls tree viewer. If this method
	 * returns <code>true</code>, {@link #doCreateViewerFilters()} must return not null!
	 *
	 * @return <code>True</code> if to associate viewer filters, <code>false</code> otherwise.
	 */
	protected boolean hasViewerFilters() {
		return true;
	}

	/**
	 * Creates a returns a new set of the viewer filters to associated to the controls tree viewer.
	 * This method will be called from {@link #getViewerFilters()} in case the method
	 * {@link #hasViewerFilters()} returns <code>true</code> and no viewer filters got created
	 * before.
	 *
	 * @return The viewer filters to associate or <code>null</code> if none.
	 */
	protected ViewerFilter[] doCreateViewerFilters() {
		return new ViewerFilter[] { new ContextSelectorViewerFilter() };
	}

	/**
	 * Returns the associated viewer filters of the controls tree viewer. If the control does have
	 * viewer filters ({@link #hasViewerFilters()} returns <code>true</code>) and the viewer filters
	 * had not yet been created, the method calls {@link #doCreateViewerFilters()}.
	 *
	 * @return The associated viewer filters of the controls tree viewer or the constant
	 *         {@link #NO_FILTERS}.
	 */
	protected ViewerFilter[] getViewerFilters() {
		if (filters == null && hasViewerFilters()) {
			filters = doCreateViewerFilters();
		}
		return filters != null ? filters : NO_FILTERS;
	}

	/**
	 * Creates a new checkbox tree viewer check state listener. This method will be called from
	 * {@link #getViewerCheckStateListener()} in case the listener did not got created before.
	 *
	 * @param viewer The checkbox tree viewer. Must not be <code>null</code>.
	 * @return The checkbox tree viewer check state listener or <code>null</code> if none.
	 */
	protected ICheckStateListener doCreateViewerCheckStateListener(CheckboxTreeViewer viewer) {
		Assert.isNotNull(viewer);
		return new ContextSelectedCheckStateListener();
	}

	/**
	 * Returns the associated checkbox tree viewer check state listener. If the listener had not yet
	 * been created, the method calls {@link #doCreateLabelProvider()}.
	 *
	 * @param viewer The checkbox tree viewer. Must not be <code>null</code>.
	 * @return The associated checkbox tree viewer check state listener or <code>null</code> if
	 *         none.
	 */
	protected ICheckStateListener getViewerCheckStateListener(CheckboxTreeViewer viewer) {
		Assert.isNotNull(viewer);
		if (listener == null) {
			listener = doCreateViewerCheckStateListener(viewer);
		}
		return listener;
	}

	/**
	 * Called from the default check state listener implementation if the checked state of the
	 * passed in model node has changed.
	 *
	 * @param node The model node. Must not be <code>null</code>.
	 * @param checked <code>True</code> if the model node has been checked, <code>false</code> if
	 *            unchecked.
	 */
	protected void onModelNodeCheckStateChanged(IModelNode node, boolean checked) {
		// validate the parent page if there is one set
		if (getParentPage() instanceof IValidatingContainer) {
			((IValidatingContainer) getParentPage()).validate();
		}
	}

	/**
	 * Returns the initial input object to set to the controls tree viewer.
	 *
	 * @return The initial viewer input to set or <code>null</code> if none.
	 */
	protected Object getInitialViewerInput() {
		return ViewRoot.getInstance();
	}

	/**
	 * Enables the tree control.
	 *
	 * @param viewer The tree viewer object. Must not be <code>null</code>.
	 */
	protected void doEnableControl(TreeViewer viewer) {
		Assert.isNotNull(viewer);
		SWTControlUtil.setEnabled(viewer.getTree(), viewer.getTree().getItemCount() > 0);
	}

	/**
	 * Refresh the controls viewer and check the viewers enablement. This method is called by the
	 * standard refresh toolbar item, if the control has a toolbar.
	 */
	public void refresh() {
		if (viewer != null && viewer.getControl() != null && !viewer.getControl().isDisposed()) {
			viewer.refresh(true);
			doEnableControl(viewer);
		}
	}

	/**
	 * Called from {@link #setupPanel(Composite)} before returning the control to the caller.
	 * Override to plug-in and configure any custom listener the subclassed control might need.
	 */
	protected void configureControlListener() {
	}

	/**
	 * Sets the given selection to the viewer.
	 *
	 * @param selection The selection to set. Must not be <code>null</code>.
	 */
	@Override
	public void setSelection(ISelection selection) {
		Assert.isNotNull(selection);
		this.selection = selection;
		if (viewer != null) {
			viewer.setSelection(selection, true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	@Override
	public ISelection getSelection() {
		return selection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		if (listener != null && !selectionListeners.contains(listener)) {
			selectionListeners.add(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		if (listener != null) {
			selectionListeners.remove(listener);
		}
	}

	/**
	 * Fire the selection changed event to the registered listeners.
	 */
	/* default */ void fireSelectionChanged() {
		if (selection != null) {
			SelectionChangedEvent event = new SelectionChangedEvent(this, selection);
			for (ISelectionChangedListener listener : selectionListeners) {
				listener.selectionChanged(event);
			}
		}
	}
}
