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
import org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * The property section to display the context IDs of a process.
 */
public class ContextIDSection extends BaseTitledSection {
	protected ISysMonitor.SysMonitorContext context;
	
	protected Text idText;
	protected Text parentIdText;
	protected Text pgrpText;
	protected Text pidText;
	protected Text ppidText;
	protected Text tgidText;
	protected Text tracerPidText;
	protected Text uidText;
	protected Text ugidText;
	
	@Override
    public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
	    super.createControls(parent, aTabbedPropertySheetPage);
		
	    idText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		FormData data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		idText.setLayoutData(data);
		idText.setEditable(false);

		CLabel idLabel = getWidgetFactory().createCLabel(composite, "ID:");
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(idText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(idText, 0, SWT.CENTER);
		idLabel.setLayoutData(data);
		
		parentIdText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(idText, ITabbedPropertyConstants.VSPACE);
		parentIdText.setLayoutData(data);
		parentIdText.setEditable(false);

		CLabel parentIdLabel = getWidgetFactory().createCLabel(composite, "Parent ID:");
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(parentIdText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(parentIdText, 0, SWT.CENTER);
		parentIdLabel.setLayoutData(data);
		
		pgrpText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(parentIdText, ITabbedPropertyConstants.VSPACE);
		pgrpText.setLayoutData(data);
		pgrpText.setEditable(false);

		CLabel pgrpLabel = getWidgetFactory().createCLabel(composite, "Group ID:");
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(pgrpText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(pgrpText, 0, SWT.CENTER);
		pgrpLabel.setLayoutData(data);
		
		pidText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(pgrpText, ITabbedPropertyConstants.VSPACE);
		pidText.setLayoutData(data);
		pidText.setEditable(false);

		CLabel pidLabel = getWidgetFactory().createCLabel(composite, "PID:");
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(pidText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(pidText, 0, SWT.CENTER);
		pidLabel.setLayoutData(data);
		
		///////////////////////
		ppidText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(pidText, ITabbedPropertyConstants.VSPACE);
		ppidText.setLayoutData(data);
		ppidText.setEditable(false);

		CLabel ppidLabel = getWidgetFactory().createCLabel(composite, "PPID:");
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(ppidText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(ppidText, 0, SWT.CENTER);
		ppidLabel.setLayoutData(data);
		
		tgidText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(ppidText, ITabbedPropertyConstants.VSPACE);
		tgidText.setLayoutData(data);
		tgidText.setEditable(false);

		CLabel tgidLabel = getWidgetFactory().createCLabel(composite, "TTY GRPID:");
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(tgidText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(tgidText, 0, SWT.CENTER);
		tgidLabel.setLayoutData(data);
		
		tracerPidText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(tgidText, ITabbedPropertyConstants.VSPACE);
		tracerPidText.setLayoutData(data);
		tracerPidText.setEditable(false);

		CLabel tracerPidLabel = getWidgetFactory().createCLabel(composite, "Tracer PID:");
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(tracerPidText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(tracerPidText, 0, SWT.CENTER);
		tracerPidLabel.setLayoutData(data);
		//////////////////////////////////////////////////////////
		uidText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(tracerPidText, ITabbedPropertyConstants.VSPACE);
		uidText.setLayoutData(data);
		uidText.setEditable(false);

		CLabel uidLabel = getWidgetFactory().createCLabel(composite, "User ID:");
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(uidText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(uidText, 0, SWT.CENTER);
		uidLabel.setLayoutData(data);
		//**************
		ugidText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(uidText, ITabbedPropertyConstants.VSPACE);
		ugidText.setLayoutData(data);
		ugidText.setEditable(false);

		CLabel ugidLabel = getWidgetFactory().createCLabel(composite, "User GRPID:");
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(ugidText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(ugidText, 0, SWT.CENTER);
		ugidLabel.setLayoutData(data);
    }

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

	@Override
    public void refresh() {
		this.idText.setText(context.getID() != null ? "" : context.getID());
		this.parentIdText.setText(context.getParentID()==null?"":context.getParentID());
		this.pgrpText.setText(""+context.getPGRP());
		this.pidText.setText(""+context.getPID());
		this.ppidText.setText(""+context.getPPID());
		this.tgidText.setText(""+context.getTGID());
		this.tracerPidText.setText(""+context.getTracerPID());
		this.uidText.setText(""+context.getUID());
		this.ugidText.setText("" +context.getUGID());
    }

	@Override
	protected String getText() {
		return "Context IDs";
	}

}
