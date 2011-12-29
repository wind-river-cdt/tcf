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
import org.eclipse.tcf.services.ISysMonitor;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessTreeNode;
import org.eclipse.tcf.te.tcf.processes.ui.nls.Messages;
import org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * The property section to display the basic context information of a process.
 */
public class BasicContextSection extends BaseTitledSection {
	// The process context to be displayed.
	protected ISysMonitor.SysMonitorContext context;
	// The text field for the executable file.
	protected Text fileText;
	// The text field for the working directory.
	protected Text workDirText;
	// The text field for the root directory.
	protected Text rootText;
	// The state of the process.
	protected Text stateText;
	// The owner of the process.
	protected Text userText;
	// The owner group of the process.
	protected Text groupText;
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection#createControls(org.eclipse.swt.widgets.Composite, org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	@Override
    public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
	    super.createControls(parent, aTabbedPropertySheetPage);
		
	    fileText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		FormData data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		fileText.setLayoutData(data);
		fileText.setEditable(false);

		CLabel fileLabel = getWidgetFactory().createCLabel(composite, Messages.BasicContextSection_File);
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(fileText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(fileText, 0, SWT.CENTER);
		fileLabel.setLayoutData(data);
		
		workDirText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(fileText, ITabbedPropertyConstants.VSPACE);
		workDirText.setLayoutData(data);
		workDirText.setEditable(false);

		CLabel workDirLabel = getWidgetFactory().createCLabel(composite, Messages.BasicContextSection_WorkDir);
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(workDirText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(workDirText, 0, SWT.CENTER);
		workDirLabel.setLayoutData(data);

		rootText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(workDirText, ITabbedPropertyConstants.VSPACE);
		rootText.setLayoutData(data);
		rootText.setEditable(false);

		CLabel rootTextLabel = getWidgetFactory().createCLabel(composite, Messages.BasicContextSection_Root);
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(rootText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(rootText, 0, SWT.CENTER);
		rootTextLabel.setLayoutData(data);

		stateText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(rootText, ITabbedPropertyConstants.VSPACE);
		stateText.setLayoutData(data);
		stateText.setEditable(false);

		CLabel stateLabel = getWidgetFactory().createCLabel(composite, Messages.BasicContextSection_State);
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

		CLabel userLabel = getWidgetFactory().createCLabel(composite, Messages.BasicContextSection_User);
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(userText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(userText, 0, SWT.CENTER);
		userLabel.setLayoutData(data);
		
		groupText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(userText, ITabbedPropertyConstants.VSPACE);
		groupText.setLayoutData(data);
		groupText.setEditable(false);

		CLabel groupLabel = getWidgetFactory().createCLabel(composite, Messages.BasicContextSection_Group);
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(groupText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(groupText, 0, SWT.CENTER);
		groupLabel.setLayoutData(data);
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
        ProcessTreeNode node = (ProcessTreeNode) input;
        Assert.isNotNull(node.context);
        context = node.context;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#refresh()
	 */
	@Override
    public void refresh() {
		fileText.setText(context.getFile() == null ? "" : context.getFile()); //$NON-NLS-1$
		workDirText.setText(context.getCurrentWorkingDirectory() == null ? "" : context.getCurrentWorkingDirectory()); //$NON-NLS-1$
		rootText.setText(context.getRoot() == null ? "" : context.getRoot()); //$NON-NLS-1$
		stateText.setText(context.getState() == null ? "" : context.getState()); //$NON-NLS-1$
		userText.setText(context.getUserName() == null ? "" : context.getUserName()); //$NON-NLS-1$
		groupText.setText(context.getGroupName() == null ? "" : context.getGroupName()); //$NON-NLS-1$
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection#getText()
	 */
	@Override
	protected String getText() {
		return Messages.BasicContextSection_Title; 
	}
}
