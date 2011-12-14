/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.dialogs;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.te.tcf.processes.ui.controls.ProcessesTreeContentProvider;
import org.eclipse.tcf.te.tcf.processes.ui.controls.ProcessesTreeNode;
import org.eclipse.tcf.te.tcf.processes.ui.internal.columns.ProcessComparator;
import org.eclipse.tcf.te.tcf.processes.ui.internal.columns.ProcessLabelProvider;
import org.eclipse.tcf.te.ui.activator.UIPlugin;
import org.eclipse.tcf.te.ui.interfaces.IUIConstants;
import org.eclipse.tcf.te.ui.trees.FilterDescriptor;
import org.eclipse.tcf.te.ui.trees.ViewerStateManager;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

/**
 * Process selection dialog.
 */
public class ProcessSelectionDialog extends ElementTreeSelectionDialog {
	// Label provider used by the file system tree.
	private ProcessLabelProvider labelProvider;

	/**
	 * Create an FSFolderSelectionDialog using the specified shell as the parent.
	 * 
	 * @param parentShell The parent shell.
	 */
	public ProcessSelectionDialog(Shell parentShell) {
		this(parentShell, new ProcessLabelProvider(), new ProcessesTreeContentProvider());
	}

	/**
	 * Create an FSFolderSelectionDialog using the specified shell, an FSTreeLabelProvider, and a
	 * content provider that provides the tree nodes.
	 * 
	 * @param parentShell The parent shell.
	 * @param labelProvider The label provider.
	 * @param contentProvider The content provider.
	 */
	private ProcessSelectionDialog(Shell parentShell, ProcessLabelProvider labelProvider, ITreeContentProvider contentProvider) {
		super(parentShell, createDecoratingLabelProvider(labelProvider), contentProvider);
		this.labelProvider = labelProvider;
		this.setAllowMultiple(false);
		this.setStatusLineAboveButtons(false);
		this.setComparator(new ViewerComparator(){
			private ProcessComparator comparator = new ProcessComparator();
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof ProcessesTreeNode && e2 instanceof ProcessesTreeNode) {
					return comparator.compare((ProcessesTreeNode) e1, (ProcessesTreeNode) e2);
				}
				return super.compare(viewer, e1, e2);
			}
		});
		this.setValidator(new ISelectionStatusValidator() {
			@Override
			public IStatus validate(Object[] selection) {
				return isValidSelection(selection);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.ElementTreeSelectionDialog#setInput(java.lang.Object)
	 */
	@Override
    public void setInput(Object input) {
		super.setInput(input);
		FilterDescriptor[] filterDescriptors = ViewerStateManager.getInstance().
						getFilterDescriptors(IUIConstants.ID_CONTROL_MENUS_BASE + ".viewer.processes", input); //$NON-NLS-1$
		if (filterDescriptors != null) {
			for(FilterDescriptor descriptor : filterDescriptors) {
				if(descriptor.isEnabled()) addFilter(descriptor.getFilter());
			}
		}
	}

	/**
	 * Create a decorating label provider using the specified label provider.
	 * 
	 * @param labelProvider The label provider that actually provides labels and images.
	 * @return The decorating label provider.
	 */
	private static ILabelProvider createDecoratingLabelProvider(ILabelProvider labelProvider) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IDecoratorManager manager = workbench.getDecoratorManager();
		ILabelDecorator decorator = manager.getLabelDecorator();
		return new DecoratingLabelProvider(labelProvider,decorator);
	}

	/**
	 * Create the tree viewer and set it to the label provider.
	 */
	@Override
	protected TreeViewer doCreateTreeViewer(Composite parent, int style) {
		TreeViewer viewer = super.doCreateTreeViewer(parent, style);
		viewer.getTree().setLinesVisible(false);
		labelProvider.setViewer(viewer);
		return viewer;
	}

	/**
	 * If the specified selection is a valid folder to be selected.
	 * 
	 * @param selection The selected folders.
	 * @return An error status if it is invalid or an OK status indicating it is valid.
	 */
	IStatus isValidSelection(Object[] selection) {
		String pluginId = UIPlugin.getUniqueIdentifier();
		IStatus error = new Status(IStatus.ERROR, pluginId, null);
		if (selection == null || selection.length == 0) {
			return error;
		}
		if (!(selection[0] instanceof ProcessesTreeNode)) {
			return error;
		}
		return new Status(IStatus.OK, pluginId, null);
	}
}
