/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.tabbed;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.ui.nls.Messages;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProvider;
import org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * The property section for the attributes of a file/folder on Windows.
 */
public class WindowsAttributesSection extends BaseTitledSection {
	// The original node.
	protected FSTreeNode node;
	// The copy node.
	protected FSTreeNode clone;
	// The check box for "Read Only" attribute.
	protected Button readOnlyButton;
	// The check box for "Hidden" attribute.
	protected Button hiddenButton;
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection#createControls(org.eclipse.swt.widgets.Composite, org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	@Override
    public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
	    super.createControls(parent, aTabbedPropertySheetPage);
		
		readOnlyButton = getWidgetFactory().createButton(composite, Messages.GeneralInformationPage_ReadOnly, SWT.CHECK);
		FormData data = new FormData();
		data.left = new FormAttachment(0, ITabbedPropertyConstants.HMARGIN );
		data.right = new FormAttachment(50, 0);
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		readOnlyButton.setLayoutData(data);

		hiddenButton = getWidgetFactory().createButton(composite, Messages.GeneralInformationPage_Hidden, SWT.CHECK);
		data = new FormData();
		data.left = new FormAttachment(readOnlyButton, ITabbedPropertyConstants.HSPACE);
		data.right = new FormAttachment(100, -ITabbedPropertyConstants.HMARGIN );
		data.top = new FormAttachment(readOnlyButton, 0, SWT.CENTER);
		hiddenButton.setLayoutData(data);
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection#updateData(org.eclipse.tcf.te.ui.interfaces.IPropertyChangeProvider)
	 */
	@Override
    protected void updateInput(IPeerModelProvider input) {
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
		readOnlyButton.setSelection(clone.isReadOnly());
		hiddenButton.setSelection(clone.isHidden());
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection#getText()
	 */
	@Override
    protected String getText() {
	    return Messages.WindowsAttributesSection_Attributes; 
    }
}
