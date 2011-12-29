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
import org.eclipse.tcf.te.tcf.processes.ui.nls.Messages;
import org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * The property section to display the IDs of a process.
 */
public class IDSection extends BaseTitledSection {
	// The process tree node selected.
	protected ProcessTreeNode node;
	// The text field to display the process id.
	protected Text pidText;
	// The text field to display the parent process id.
	protected Text ppidText;
	// The  text field to display the internal process id.
	protected Text ipidText;
	// The text field to display the internal parent process id.
	protected Text ippidText;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection#createControls(org.eclipse.swt.widgets.Composite, org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	@Override
    public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
	    super.createControls(parent, aTabbedPropertySheetPage);
		
	    pidText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		FormData data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		pidText.setLayoutData(data);
		pidText.setEditable(false);

		CLabel pidLabel = getWidgetFactory().createCLabel(composite, Messages.IDSection_ProcessID);
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(pidText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(pidText, 0, SWT.CENTER);
		pidLabel.setLayoutData(data);
		
		ppidText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(pidText, ITabbedPropertyConstants.VSPACE);
		ppidText.setLayoutData(data);
		ppidText.setEditable(false);

		CLabel ppidLabel = getWidgetFactory().createCLabel(composite, Messages.IDSection_ParentID);
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(ppidText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(ppidText, 0, SWT.CENTER);
		ppidLabel.setLayoutData(data);
		
		ipidText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(ppidText, ITabbedPropertyConstants.VSPACE);
		ipidText.setLayoutData(data);
		ipidText.setEditable(false);

		CLabel ipidLabel = getWidgetFactory().createCLabel(composite, Messages.IDSection_InternalID);
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(ipidText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(ipidText, 0, SWT.CENTER);
		ipidLabel.setLayoutData(data);
		
		ippidText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(ipidText, ITabbedPropertyConstants.VSPACE);
		ippidText.setLayoutData(data);
		ippidText.setEditable(false);

		CLabel ippidLabel = getWidgetFactory().createCLabel(composite, Messages.IDSection_InternalPPID);
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(ippidText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(ippidText, 0, SWT.CENTER);
		ippidLabel.setLayoutData(data);
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
        Assert.isTrue(input instanceof ProcessTreeNode);
        this.node = (ProcessTreeNode) input;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#refresh()
	 */
	@Override
    public void refresh() {
		pidText.setText(""+node.pid); //$NON-NLS-1$
		ppidText.setText(""+node.ppid); //$NON-NLS-1$
		ipidText.setText(node.id == null ? "" : node.id); //$NON-NLS-1$
		ippidText.setText(node.parentId == null ? "" : node.parentId); //$NON-NLS-1$
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection#getText()
	 */
	@Override
	protected String getText() {
		return Messages.IDSection_Title; 
	}
}
