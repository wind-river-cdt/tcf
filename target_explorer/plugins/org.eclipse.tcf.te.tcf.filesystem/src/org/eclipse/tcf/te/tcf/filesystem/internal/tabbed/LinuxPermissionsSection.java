/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.tabbed;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.nls.Messages;
import org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * The property section for displaying the permissions of a linux file/folder.
 */
public class LinuxPermissionsSection extends BaseTitledSection {
	// The original node.
	protected FSTreeNode node;
	// The copy node to be edited.
	protected FSTreeNode clone;
	// The button of "Permissions"
	protected Button[] btnPermissions;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection#createControls(org.eclipse.swt.widgets.Composite, org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	@Override
	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		btnPermissions = new Button[9];
		Composite comp1 = createPermissionGroup(null, composite, 0, Messages.PermissionsGroup_UserPermissions);
		Composite comp2 = createPermissionGroup(comp1, composite, 3, Messages.PermissionsGroup_GroupPermissions);
		createPermissionGroup(comp2, composite, 6, Messages.PermissionsGroup_OtherPermissions);
	}

	/**
	 * Create a permission group for a role, such as a user, a group or others.
	 * 
	 * @param prev The previous permission group to align with.
	 * @param parent The parent composite.
	 * @param bit The permission bit index.
	 * @param header The group's header label.
	 */
	protected Composite createPermissionGroup(Composite prev, Composite parent, int bit, String header) {
		Composite group = getWidgetFactory().createFlatFormComposite(parent);
		FormLayout layout = (FormLayout) group.getLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.spacing = 0;

		FormData data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		if (prev == null) data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		else data.top = new FormAttachment(prev, ITabbedPropertyConstants.VSPACE);
		group.setLayoutData(data);

		createPermissionButton(Messages.PermissionsGroup_Readable, bit, group);
		createPermissionButton(Messages.PermissionsGroup_Writable, bit + 1, group);
		createPermissionButton(Messages.PermissionsGroup_Executable, bit + 2, group);

		CLabel groupLabel = getWidgetFactory().createCLabel(parent, header);
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(group, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(group, 0, SWT.TOP);
		groupLabel.setLayoutData(data);

		return group;
	}

	/**
	 * Create a check-box field for a single permission item.
	 * 
	 * @param label The label of the permission.
	 * @param index The index of current permission bit mask index.
	 * @param parent The parent to hold the check-box field.
	 */
	private void createPermissionButton(String label, final int index, Composite parent) {
		btnPermissions[index] = getWidgetFactory().createButton(parent, label, SWT.CHECK);
		FormData data = new FormData();
		if ((index % 3) == 0) data.left = new FormAttachment(0, 0);
		else data.left = new FormAttachment(btnPermissions[index - 1], ITabbedPropertyConstants.HSPACE);
		data.right = new FormAttachment(((index % 3) + 1) * 33, 0);
		if ((index % 3) == 0) data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		else data.top = new FormAttachment(btnPermissions[index - 1], 0, SWT.CENTER);
		btnPermissions[index].setLayoutData(data);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#setInput(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		Assert.isTrue(selection instanceof IStructuredSelection);
		Object input = ((IStructuredSelection) selection).getFirstElement();
		Assert.isTrue(input instanceof FSTreeNode);
		this.node = (FSTreeNode) input;
		this.clone = (FSTreeNode) node.clone();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#refresh()
	 */
	@Override
	public void refresh() {
		for (int i = 0; i < 9; i++) {
			final int bit = 1 << (8 - i);
			final boolean on = (clone.attr.permissions & bit) != 0;
			btnPermissions[i].setSelection(on);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection#getText()
	 */
	@Override
    protected String getText() {
	    return Messages.LinuxPermissionsSection_Permissions;
    }
}
