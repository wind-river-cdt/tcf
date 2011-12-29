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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tcf.te.tcf.filesystem.internal.utils.ContentTypeHelper;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.nls.Messages;
import org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * The section that displays the basic information of a folder.
 */
public class BasicFolderSection extends BaseTitledSection {
	// The formatter for the size of a file.
	private static final DecimalFormat SIZE_FORMAT = new DecimalFormat();
	// The formatter for the modified time and the accessed time.
	private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);

	// The original node to be displayed and edited.
	protected FSTreeNode node;
	// The copy used to be edited.
	protected FSTreeNode clone;
	
	// The text for the name of the node.
	protected Text nameText;
	// The text for the type of the node.
	protected Text typeText;
	// The text for the location of the node.
	protected Text locationText;
	// The text for the modified time of the node.
	protected Text modifiedText;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection#createControls(org.eclipse.swt.widgets.Composite, org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
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

		CLabel nameLabel = getWidgetFactory().createCLabel(composite, Messages.GeneralInformationPage_Name);
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

		CLabel typeLabel = getWidgetFactory().createCLabel(composite, Messages.GeneralInformationPage_Type);
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(typeText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(typeText, 0, SWT.CENTER);
		typeLabel.setLayoutData(data);
		
		locationText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(typeText, ITabbedPropertyConstants.VSPACE);
		locationText.setLayoutData(data);
		locationText.setEditable(false);

		CLabel locationLabel = getWidgetFactory().createCLabel(composite, Messages.GeneralInformationPage_Location);
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(locationText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(locationText, 0, SWT.CENTER);
		locationLabel.setLayoutData(data);
		
		modifiedText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(locationText, ITabbedPropertyConstants.VSPACE);
		modifiedText.setLayoutData(data);
		modifiedText.setEditable(false);

		CLabel modifiedLabel = getWidgetFactory().createCLabel(composite, Messages.GeneralInformationPage_Modified);
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(modifiedText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(modifiedText, 0, SWT.CENTER);
		modifiedLabel.setLayoutData(data);
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
		nameText.setText(clone.name);
		typeText.setText(getNodeTypeLabel());
		String location = clone.isRoot() ? Messages.GeneralInformationPage_Computer : clone.getLocation();
		locationText.setText(location);
		modifiedText.setText(getDateText(clone.attr.mtime));
    }

	/**
	 * Get the string of the specific time using the formatter, DATE_FORMAT.
	 * 
	 * @param time The time to be formatted.
	 * @return The string in the format of DATE_FORMAT.
	 */
	protected String getDateText(long time) {
		return DATE_FORMAT.format(new Date(time));
	}
	
	/**
	 * Get the type of an FSTreeNode.
	 * 
	 * @return "folder" if it is a directory, "file" if is a file, or else defaults to node.type.
	 */
	protected String getNodeTypeLabel() {
		if (clone.isDirectory()) {
			return Messages.GeneralInformationPage_Folder;
		}
		else if (clone.isFile()) {
			IContentType contentType = ContentTypeHelper.getInstance().getContentType(clone);
			String contentTypeName = contentType == null ? Messages.GeneralInformationPage_UnknownFileType : contentType.getName();
			return NLS.bind(Messages.GeneralInformationPage_File, contentTypeName);
		}
		else return clone.type;
	}	 

	/**
	 * Get the string of the file size using using the formatter, SIZE_FORMAT.
	 *
	 * @param size
	 *            The size of the file to be formatted.
	 * @return The string in the format of SIZE_FORMAT.
	 */
	protected String getSizeText(long size) {
		return NLS.bind(Messages.GeneralInformationPage_FileSizeInfo, SIZE_FORMAT.format(size / 1024), SIZE_FORMAT.format(size));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection#getText()
	 */
	@Override
    protected String getText() {
	    return Messages.BasicFolderSection_BasicInfoText;
    }	
}