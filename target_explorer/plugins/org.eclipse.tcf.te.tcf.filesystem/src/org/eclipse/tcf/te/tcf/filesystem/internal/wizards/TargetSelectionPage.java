/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.wizards;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tcf.te.tcf.filesystem.nls.Messages;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.model.Model;
import org.eclipse.tcf.te.tcf.ui.navigator.LabelProvider;
import org.eclipse.tcf.te.ui.activator.UIPlugin;
import org.eclipse.tcf.te.ui.interfaces.IUIConstants;
import org.eclipse.tcf.te.ui.wizards.pages.AbstractValidatableWizardPage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * The New Target creation wizard selection page implementation.
 * <p>
 * This class is copied and adapted from <code>org.eclipse.tcf.te.ui.wizards.newWizard.NewWizardSelectionPage</code>.
 *
 * @since 3.8
 */
public class TargetSelectionPage extends AbstractValidatableWizardPage {
	// References to the page subcontrol's
	private FilteredTree filteredTree;
	private PatternFilter filteredTreeFilter;

	// The workbench instance as passed in by init(...)
	private IWorkbench workbench;
	// The selection as passed in by init(...)
	private IStructuredSelection selection;
	// The tree viewer to display the targets.
	private TreeViewer treeViewer;

	/**
	 * Internal class. The wizard viewer comparator is responsible for the sorting in the tree.
	 * Current implementation is not prioritizing categories.
	 */
	/* default */static class TargetViewerComparator extends ViewerComparator {

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ViewerComparator#isSorterProperty(java.lang.Object,
		 * java.lang.String)
		 */
		@Override
		public boolean isSorterProperty(Object element, String property) {
			// The comparator is affected if the label of the elements should change.
			return property.equals(IBasicPropertyConstants.P_TEXT);
		}
	}

	/**
	 * Constructor.
	 *
	 * @param wizardRegistry The new target wizard registry. Must not be <code>null</code>.
	 */
	public TargetSelectionPage() {
		super(TargetSelectionPage.class.getSimpleName());
		setTitle(getDefaultTitle());
		setDescription(getDefaultDescription());
	}

	@Override
    public NewNodeWizard getWizard() {
		return (NewNodeWizard) super.getWizard();
	}
	/**
	 * Returns the default page title.
	 *
	 * @return The default page title. Must be never <code>null</code>.
	 */
	protected String getDefaultTitle() {
		return Messages.TargetSelectionPage_Title;
	}

	/**
	 * Returns the default page description.
	 *
	 * @return The default page description. Must be never <code>null</code>.
	 */
	protected String getDefaultDescription() {
		return Messages.TargetSelectionPage_Description;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		label.setText(Messages.TargetSelectionPage_Targets);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		filteredTreeFilter = new TargetPatternFilter();
		filteredTree = new FilteredTree(composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, filteredTreeFilter, true);
		filteredTree.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.heightHint = 250;
		layoutData.widthHint = 450;
		filteredTree.setLayoutData(layoutData);

		treeViewer = filteredTree.getViewer();
		treeViewer.setContentProvider(new TargetContentProvider());
		treeViewer.setLabelProvider(new LabelProvider());
		treeViewer.setComparator(new TargetViewerComparator());
		ViewerFilter fsPeerFilter = new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof IPeerModel) {
					IPeerModel peer = (IPeerModel) element;
					NewNodeWizard wizard = getWizard();
					return wizard.hasFileSystem(peer);
				}
				return false;
			}
		};
		treeViewer.addFilter(fsPeerFilter);
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				onSelectionChanged();
			}
		});
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				// Double-click on a connection type is triggering the sub wizard
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					// The tree is single selection, so look for the first element only.
					Object element = selection.getFirstElement();
					if (element instanceof IPeerModel) {
						// Double-click on a connection type is triggering the sub wizard
						getWizard().getContainer().showPage(getNextPage());
					}
					else if (event.getViewer() instanceof TreeViewer) {
						TreeViewer viewer = (TreeViewer) event.getViewer();
						if (viewer.isExpandable(element)) {
							viewer.setExpandedState(element, !viewer.getExpandedState(element));
						}
					}
				}
			}
		});

		treeViewer.setInput(Model.getModel());
		NewNodeWizard wizard = getWizard();
		IPeerModel peer = wizard.getPeer();
		if (wizard.getPeer() != null) {
			treeViewer.setSelection(new StructuredSelection(peer), true);
		}

		// apply the standard dialog font
		Dialog.applyDialogFont(composite);

		setControl(composite);

		// Restore the tree state
		restoreWidgetValues();

		// Initialize the context help id
		PlatformUI.getWorkbench().getHelpSystem()
		                .setHelp(getControl(), IUIConstants.HELP_NEW_WIZARD_SELECTION_PAGE);

		setPageComplete(peer != null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.wizards.pages.AbstractValidatableWizardPage#validatePage()
	 */
	@Override
	public void validatePage() {
		super.validatePage();
		if (!isPageComplete()) return;
		if (isValidationInProgress()) return;
		setValidationInProgress(true);
		boolean valid = true;
		ISelection selection = treeViewer.getSelection();
		if (selection.isEmpty()) {
			setMessage(getDefaultDescription(), IMessageProvider.ERROR);
			valid = false;
		}
		setPageComplete(valid);
		setValidationInProgress(false);
	}

	/**
	 * Initialize the page with the current workbench instance and the current workbench selection.
	 *
	 * @param workbench The current workbench.
	 * @param selection The current object selection.
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
	}

	/**
	 * Returns the current workbench.
	 *
	 * @return The current workbench or <code>null</code> if not set.
	 */
	public IWorkbench getWorkbench() {
		return workbench;
	}

	/**
	 * Returns the current object selection.
	 *
	 * @return The current object selection or <code>null</code> if not set.
	 */
	public IStructuredSelection getSelection() {
		return selection;
	}

	/**
	 * Called from the selection listener to propagate the current system type selection to the
	 * underlying wizard.
	 */
	protected void onSelectionChanged() {
		if (filteredTree.getViewer().getSelection() instanceof IStructuredSelection) {
			IStructuredSelection filteredTreeSelection = (IStructuredSelection) filteredTree
			                .getViewer().getSelection();
			NewNodeWizard wizard = getWizard();
			if (filteredTreeSelection.getFirstElement() instanceof IPeerModel) {
				wizard.setPeer((IPeerModel) filteredTreeSelection.getFirstElement());
			}
			else {
				wizard.setPeer(null);
			}
		}

		// Update the wizard container UI elements
		IWizardContainer container = getContainer();
		if (container != null && container.getCurrentPage() != null) {
			container.updateWindowTitle();
			container.updateTitleBar();
			container.updateButtons();
		}
		validatePage();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getDialogSettings()
	 */
	@Override
	protected IDialogSettings getDialogSettings() {
		// If the wizard is set and returns dialog settings, we re-use them here
		IDialogSettings settings = super.getDialogSettings();
		// If the dialog settings could not set from the wizard, fallback to the plugin's
		// dialog settings store.
		if (settings == null) settings = UIPlugin.getDefault().getDialogSettings();
		String sectionName = this.getClass().getName();
		if (settings.getSection(sectionName) == null) settings.addNewSection(sectionName);
		settings = settings.getSection(sectionName);

		return settings;
	}
}
