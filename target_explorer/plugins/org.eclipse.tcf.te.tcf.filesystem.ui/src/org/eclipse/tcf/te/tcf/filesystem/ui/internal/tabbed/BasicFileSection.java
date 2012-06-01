/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.tabbed;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tcf.te.tcf.filesystem.ui.nls.Messages;
import org.eclipse.tcf.te.ui.swt.SWTControlUtil;
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
		sizeText = createTextField(null, Messages.GeneralInformationPage_Size);
		accessedText = createTextField(sizeText, Messages.GeneralInformationPage_Accessed);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.tabbed.BasicFolderSection#refresh()
	 */
	@Override
    public void refresh() {
		SWTControlUtil.setText(sizeText, clone != null ? getSizeText(clone.attr.size) : ""); //$NON-NLS-1$
		SWTControlUtil.setText(accessedText, clone != null ? getDateText(clone.attr.atime) : ""); //$NON-NLS-1$
		super.refresh();
    }
}
