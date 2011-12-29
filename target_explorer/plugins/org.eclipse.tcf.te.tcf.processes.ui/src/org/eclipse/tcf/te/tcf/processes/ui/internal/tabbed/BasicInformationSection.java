/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.internal.tabbed;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessTreeNode;
import org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * The property section to display the basic information of a process.
 */
public class BasicInformationSection extends BaseTitledSection {
	protected ProcessTreeNode node;
	
	protected Text nameText;
	protected Text typeText;
	protected Text stateText;
	protected Text userText;
	@Override
    public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
	    super.createControls(parent, aTabbedPropertySheetPage);
		
		nameText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		FormData data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		nameText.setLayoutData(data);
		nameText.setEditable(false);

		CLabel nameLabel = getWidgetFactory().createCLabel(composite, "Name:");
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(nameText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(nameText, 0, SWT.CENTER);
		nameLabel.setLayoutData(data);
		
		typeText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(nameText, ITabbedPropertyConstants.VSPACE);
		typeText.setLayoutData(data);
		typeText.setEditable(false);

		CLabel typeLabel = getWidgetFactory().createCLabel(composite, "Type:");
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(typeText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(typeText, 0, SWT.CENTER);
		typeLabel.setLayoutData(data);
		
		stateText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(typeText, ITabbedPropertyConstants.VSPACE);
		stateText.setLayoutData(data);
		stateText.setEditable(false);

		CLabel stateLabel = getWidgetFactory().createCLabel(composite, "State:");
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(stateText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(stateText, 0, SWT.CENTER);
		stateLabel.setLayoutData(data);
		
		userText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(stateText, ITabbedPropertyConstants.VSPACE);
		userText.setLayoutData(data);
		userText.setEditable(false);

		CLabel userLabel = getWidgetFactory().createCLabel(composite, "User:");
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(userText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(userText, 0, SWT.CENTER);
		userLabel.setLayoutData(data);
    }

	@Override
    public void setInput(IWorkbenchPart part, ISelection selection) {
        super.setInput(part, selection);
        Assert.isTrue(selection instanceof IStructuredSelection);
        Object input = ((IStructuredSelection) selection).getFirstElement();
        Assert.isTrue(input instanceof ProcessTreeNode);
        this.node = (ProcessTreeNode) input;
    }

	@Override
    public void refresh() {
		nameText.setText(node.name == null ? "System" : node.name);
		typeText.setText(node.type);
		stateText.setText(node.state == null ? "" : node.state);
		userText.setText(node.username == null ? "" : node.username);
    }

	@Override
	protected String getText() {
		return "Basic Information"; //$NON-NLS-1$
	}

}
