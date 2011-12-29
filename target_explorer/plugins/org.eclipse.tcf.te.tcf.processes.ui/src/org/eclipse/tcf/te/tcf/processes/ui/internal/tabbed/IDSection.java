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
 * The property section to display the IDs of a process.
 */
public class IDSection extends BaseTitledSection {
	protected ProcessTreeNode node;
	protected Text pidText;
	protected Text ppidText;
	protected Text ipidText;
	protected Text ippidText;

	
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

		CLabel pidLabel = getWidgetFactory().createCLabel(composite, "Process ID:");
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

		CLabel ppidLabel = getWidgetFactory().createCLabel(composite, "Parent ID:");
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

		CLabel ipidLabel = getWidgetFactory().createCLabel(composite, "Internal PID:");
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

		CLabel ippidLabel = getWidgetFactory().createCLabel(composite, "InternalPPID:");
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(ippidText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(ippidText, 0, SWT.CENTER);
		ippidLabel.setLayoutData(data);
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
		pidText.setText(""+node.pid);
		ppidText.setText(""+node.ppid);
		ipidText.setText(node.id == null ? "" : node.id);
		ippidText.setText(node.parentId == null ? "" : node.parentId);
    }


	@Override
	protected String getText() {
		return "Process IDs"; //$NON-NLS-1$
	}
}
