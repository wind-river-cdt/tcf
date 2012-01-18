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

import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessTreeNode;
import org.eclipse.tcf.te.tcf.processes.ui.nls.Messages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * The property section to display the advanced properties of a process context.
 */
public class AdvancedPropertiesSection extends AbstractPropertySection {
	// The properties map.
	protected Map<String, Object> properties;

	// The table control to display the properties.
	private TableViewer viewer;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#createControls(org.eclipse.swt.widgets.Composite, org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	@Override
    public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
	    super.createControls(parent, aTabbedPropertySheetPage);
	    Composite composite = getWidgetFactory().createFlatFormComposite(parent);
	    Table table = getWidgetFactory().createTable(composite, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		FormData data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		data.bottom = new FormAttachment(100, -ITabbedPropertyConstants.VSPACE);
		table.setLayoutData(data);
		TableColumn column = new TableColumn(table, SWT.LEFT);
		column.setText(Messages.AdvancedPropertiesSection_Name);
		column.setWidth(100);
		column = new TableColumn(table, SWT.LEFT);
		column.setText(Messages.AdvancedPropertiesSection_Value);
		column.setWidth(150);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	    viewer = new TableViewer(table);
	    viewer.setContentProvider(new MapContentProvider());
	    viewer.setLabelProvider(new MapEntryLabelProvider());
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
        properties = node.context.getProperties();
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#refresh()
	 */
	@Override
    public void refresh() {
		viewer.setInput(properties);
    }
}
