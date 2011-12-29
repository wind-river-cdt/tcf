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
 * The property section to display the memory usage of a process.
 */
public class MemorySection extends BaseTitledSection {
	protected ISysMonitor.SysMonitorContext context;
	protected Text vsizeText;
	protected Text psizeText;
	protected Text rssText;

	@Override
	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);

		vsizeText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		FormData data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		vsizeText.setLayoutData(data);
		vsizeText.setEditable(false);

		CLabel vsizeTextLabel = getWidgetFactory().createCLabel(composite, "Virtual:");
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(vsizeText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(vsizeText, 0, SWT.CENTER);
		vsizeTextLabel.setLayoutData(data);

		psizeText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(vsizeText, ITabbedPropertyConstants.VSPACE);
		psizeText.setLayoutData(data);
		psizeText.setEditable(false);

		CLabel psizeTextLabel = getWidgetFactory().createCLabel(composite, "Pages:");
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(psizeText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(psizeText, 0, SWT.CENTER);
		psizeTextLabel.setLayoutData(data);

		rssText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(psizeText, ITabbedPropertyConstants.VSPACE);
		rssText.setLayoutData(data);
		rssText.setEditable(false);

		CLabel rssTextLabel = getWidgetFactory().createCLabel(composite, "Resident:");
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(rssText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(rssText, 0, SWT.CENTER);
		rssTextLabel.setLayoutData(data);
	}

	@Override
	protected String getText() {
		return "Memory Usage"; //$NON-NLS-1$
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
		vsizeText.setText("" + context.getVSize()); //$NON-NLS-1$
		psizeText.setText("" + context.getPSize());
		rssText.setText("" + context.getRSS());
	}
}
