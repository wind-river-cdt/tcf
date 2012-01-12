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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.te.ui.activator.UIPlugin;
import org.eclipse.tcf.te.ui.interfaces.ImageConsts;
import org.eclipse.tcf.te.ui.nls.Messages;
import org.eclipse.ui.dialogs.ListSelectionDialog;

/**
 * The action to configure the filters of a tree viewer.
 * 
 */
public class ConfigFilterAction extends Action {
	// The tree control whose filters are to be configured.
	private AbstractTreeControl treeControl;

	/**
	 * Create an instance for the specified tree control.
	 * 
	 * @param treeControl The tree control to be configured.
	 */
	public ConfigFilterAction(AbstractTreeControl treeControl) {
		super(null, AS_PUSH_BUTTON);
		this.treeControl = treeControl;
		this.setToolTipText(Messages.ConfigFilterAction_TooltipText);
		ImageDescriptor image = UIPlugin.getImageDescriptor(ImageConsts.VIEWER_FILTER_CONFIG_ENABLED);
		setImageDescriptor(image);
		image = UIPlugin.getImageDescriptor(ImageConsts.VIEWER_FILTER_CONFIG_DISABLED);
		setDisabledImageDescriptor(image);
		updateEnablement();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		FilterDescriptor[] filterDescriptors = treeControl.getFilterDescriptors();
		if (filterDescriptors == null) return;
		
		ILabelProvider filterLabelProvider = createFilterLabelProvider();
		Shell parent = treeControl.getViewer().getControl().getShell();
		String message = Messages.ConfigFilterAction_PromptMessage;
		ListSelectionDialog dialog = new ListSelectionDialog(parent, filterDescriptors, ArrayContentProvider.getInstance(), filterLabelProvider, message);
		dialog.setTitle(Messages.ConfigFilterAction_Title);
		List<FilterDescriptor> initialSelection = new ArrayList<FilterDescriptor>();
		for (FilterDescriptor descriptor : filterDescriptors) {
			if (descriptor.isEnabled()) {
				initialSelection.add(descriptor);
			}
		}
		dialog.setInitialElementSelections(initialSelection);
		if (dialog.open() == Window.OK) {
			Object[] elements = dialog.getResult();
			for (FilterDescriptor descriptor : filterDescriptors) {
				descriptor.setEnabled(false);
			}
			for (Object element : elements) {
				if (element instanceof FilterDescriptor) {
					FilterDescriptor descriptor = (FilterDescriptor) element;
					descriptor.setEnabled(true);
				}
			}
			treeControl.updateFilters();
			treeControl.updateFilterState();
		}
	}

	/**
	 * Create a label provider for the configure dialog's tree.
	 * 
	 * @return The label provider.
	 */
	private ILabelProvider createFilterLabelProvider() {
		return new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof FilterDescriptor) {
					return ((FilterDescriptor) element).getName();
				}
				return super.getText(element);
			}
			@Override
            public Image getImage(Object element) {
				if (element instanceof FilterDescriptor) {
					return ((FilterDescriptor) element).getImage();
				}
	            return super.getImage(element);
            }
		};
	}

	/**
	 * Update the enablement of this action. If there is any visible filter in the
	 * tree control, then enable this action. Or else disable it.
	 */
	public void updateEnablement() {
		FilterDescriptor[] filterDescriptors = treeControl.getFilterDescriptors();
		boolean enabled = false;
		if(filterDescriptors != null && filterDescriptors.length > 0) {
			for(FilterDescriptor filterDescriptor : filterDescriptors) {
				if(filterDescriptor.isVisible()) {
					enabled = true;
					break;
				}
			}
		}
		setEnabled(enabled);	    
    }
}
