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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessTreeNode;
import org.eclipse.tcf.te.tcf.processes.ui.nls.Messages;
import org.eclipse.tcf.te.ui.interfaces.IPropertyChangeProvider;
import org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * The property section to display the basic information of a process.
 */
public class BasicInformationSection extends BaseTitledSection {
	// The process tree node to be displayed.
	protected ProcessTreeNode node;
	// The text field for the name of the process.
	protected Text nameText;
	// The text field for the type of the process.
	protected Text typeText;
	// The text field for the state of the process.
	protected Text stateText;
	// The text field for the ownere of the process.
	protected Text userText;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection#createControls(org.eclipse.swt.widgets.Composite, org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	@Override
    public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
	    super.createControls(parent, aTabbedPropertySheetPage);
		nameText = createWrapTextField(null, Messages.BasicInformationSection_Name);
		typeText = createTextField(nameText, Messages.BasicInformationSection_Type);
		stateText = createTextField(typeText, Messages.BasicInformationSection_State);
		userText = createTextField(stateText, Messages.BasicInformationSection_User);
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection#updateData(org.eclipse.tcf.te.ui.interfaces.IPropertyChangeProvider)
	 */
	@Override
    protected void updateInput(IPropertyChangeProvider input) {
        Assert.isTrue(input instanceof ProcessTreeNode);
        this.node = (ProcessTreeNode) input;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#refresh()
	 */
	@Override
    public void refresh() {
		nameText.setText(node.name == null ? Messages.ProcessLabelProvider_RootNodeLabel : node.name);
		typeText.setText(node.type == null ? "" : node.type); //$NON-NLS-1$
		stateText.setText(node.state == null ? "" : node.state); //$NON-NLS-1$
		userText.setText(node.username == null ? "" : node.username); //$NON-NLS-1$
		super.refresh();
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection#getText()
	 */
	@Override
	protected String getText() {
		return Messages.BasicInformationSection_Title; 
	}
}
