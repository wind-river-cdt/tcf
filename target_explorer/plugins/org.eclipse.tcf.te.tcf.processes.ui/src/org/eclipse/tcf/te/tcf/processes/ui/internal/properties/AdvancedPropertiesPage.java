/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.internal.properties;

import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.tcf.te.tcf.processes.core.model.ProcessTreeNode;
import org.eclipse.tcf.te.tcf.processes.ui.internal.tabbed.MapContentProvider;
import org.eclipse.tcf.te.tcf.processes.ui.internal.tabbed.MapEntryLabelProvider;
import org.eclipse.tcf.te.tcf.processes.ui.nls.Messages;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * The property page to display the advanced properties of a process context.
 */
public class AdvancedPropertiesPage extends PropertyPage {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
    @Override
	protected Control createContents(Composite parent) {
		IAdaptable element = getElement();
		Assert.isTrue(element instanceof ProcessTreeNode);

		ProcessTreeNode node = (ProcessTreeNode) element;
		Map<String, Object> properties = null;
		if(node.context != null) {
			properties = node.context.getProperties();
		}
        
		Composite page = new Composite(parent, SWT.NONE);
		FillLayout layout = new FillLayout();
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		page.setLayout(layout);
		
		TableViewer viewer = new TableViewer(page, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		Table table = viewer.getTable();
		TableColumn column = new TableColumn(table, SWT.LEFT);
		column.setText(Messages.AdvancedPropertiesSection_Name);
		column.setWidth(100);
		column = new TableColumn(table, SWT.LEFT);
		column.setText(Messages.AdvancedPropertiesSection_Value);
		column.setWidth(150);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	    viewer.setContentProvider(new MapContentProvider());
	    viewer.setLabelProvider(new MapEntryLabelProvider());
	    if(properties != null) {
	    	viewer.setInput(properties);
	    }
	    
		return page;
	}
}
