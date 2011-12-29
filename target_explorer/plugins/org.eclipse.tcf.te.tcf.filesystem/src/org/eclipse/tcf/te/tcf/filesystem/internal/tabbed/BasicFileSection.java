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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tcf.te.tcf.filesystem.nls.Messages;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * The section that displays the basic information of a file.
 */
public class BasicFileSection extends BasicFolderSection {
	
	// The text field for the size of the file.
	protected Text sizeText;
	// The text field for the access time of the file.
	protected Text accessedText;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.tabbed.BasicFolderSection#createControls(org.eclipse.swt.widgets.Composite, org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	@Override
	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);

		sizeText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		FormData data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(modifiedText, ITabbedPropertyConstants.VSPACE);
		sizeText.setLayoutData(data);
		sizeText.setEditable(false);

		CLabel sizeLabel = getWidgetFactory().createCLabel(composite, Messages.GeneralInformationPage_Size);
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(sizeText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(sizeText, 0, SWT.CENTER);
		sizeLabel.setLayoutData(data);

		accessedText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(sizeText, ITabbedPropertyConstants.VSPACE);
		accessedText.setLayoutData(data);
		accessedText.setEditable(false);

		CLabel accessedLabel = getWidgetFactory().createCLabel(composite, Messages.GeneralInformationPage_Accessed);
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(accessedText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(accessedText, 0, SWT.CENTER);
		accessedLabel.setLayoutData(data);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.tabbed.BasicFolderSection#refresh()
	 */
	@Override
    public void refresh() {
		super.refresh();
		sizeText.setText(getSizeText(clone.attr.size));
		accessedText.setText(getDateText(clone.attr.atime));
    }
}
