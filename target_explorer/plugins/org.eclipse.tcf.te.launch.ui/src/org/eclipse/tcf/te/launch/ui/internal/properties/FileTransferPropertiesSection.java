/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.internal.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.tcf.te.launch.core.persistence.filetransfer.FileTransfersPersistenceDelegate;
import org.eclipse.tcf.te.launch.ui.model.LaunchNode;
import org.eclipse.tcf.te.launch.ui.nls.Messages;
import org.eclipse.tcf.te.launch.ui.properties.BaseTitledSection;
import org.eclipse.tcf.te.launch.ui.tabs.filetransfers.FileTransferContentProvider;
import org.eclipse.tcf.te.runtime.services.interfaces.filetransfer.IFileTransferItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * The property section to display the file transfer property of a launch configuration.
 */
public class FileTransferPropertiesSection extends BaseTitledSection {

	protected static class FileTransferLabelProvider extends LabelProvider implements ITableLabelProvider {
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		@Override
		public String getColumnText(Object element, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return ((IFileTransferItem)element).getHostPath().toOSString();
			case 1:
				return (((IFileTransferItem)element).getDirection() == IFileTransferItem.TARGET_TO_HOST ?
								Messages.FileTransferSection_toHost_text : Messages.FileTransferSection_toTarget_text);
			case 2:
				return ((IFileTransferItem)element).getTargetPath().toPortableString();
			case 3:
				return ((IFileTransferItem)element).getOptions();
			}
			return ""; //$NON-NLS-1$
		}
	}

	private IFileTransferItem[] items;
	// The table control to display the properties.
	private TableViewer viewer;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#createControls(org.eclipse.swt.widgets.Composite, org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	@Override
	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);

		Table table = getWidgetFactory().createTable(composite, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		FormData data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		data.bottom = new FormAttachment(100, -ITabbedPropertyConstants.VSPACE);
		table.setLayoutData(data);
		TableColumn column = new TableColumn(table, SWT.LEFT);
		column.setText(Messages.FileTransferSection_host_column);
		column.setWidth(200);
		column = new TableColumn(table, SWT.CENTER);
		column.setWidth(30);
		column = new TableColumn(table, SWT.LEFT);
		column.setText(Messages.FileTransferSection_target_column);
		column.setWidth(200);
		column = new TableColumn(table, SWT.LEFT);
		column.setText(Messages.FileTransferSection_options_column);
		column.setWidth(100);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		viewer = new TableViewer(table);
		viewer.setContentProvider(new FileTransferContentProvider());
		viewer.setLabelProvider(new FileTransferLabelProvider());
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
		Assert.isTrue(input instanceof LaunchNode);
		ILaunchConfiguration node = ((LaunchNode)input).getLaunchConfiguration();
		List<IFileTransferItem> list = new ArrayList<IFileTransferItem>();
		for (IFileTransferItem item : Arrays.asList(FileTransfersPersistenceDelegate.getFileTransfers(node))) {
			if (item.isEnabled()) {
				list.add(item);
			}
		}
		items = list.toArray(new IFileTransferItem[list.size()]);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#refresh()
	 */
	@Override
	public void refresh() {
		viewer.setInput(items);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection#getText()
	 */
	@Override
	protected String getText() {
		return Messages.FileTransferSection_title;
	}
}
