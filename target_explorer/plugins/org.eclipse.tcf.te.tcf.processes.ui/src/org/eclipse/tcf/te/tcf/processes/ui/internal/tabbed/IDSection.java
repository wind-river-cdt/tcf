/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
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
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProvider;
import org.eclipse.tcf.te.tcf.processes.core.model.ProcessTreeNode;
import org.eclipse.tcf.te.tcf.processes.ui.nls.Messages;
import org.eclipse.tcf.te.tcf.ui.tabbed.BaseTitledSection;
import org.eclipse.tcf.te.ui.swt.SWTControlUtil;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * The property section to display the IDs of a process.
 */
public class IDSection extends BaseTitledSection {
	// The process tree node selected.
	protected ProcessTreeNode node;
	// The text field to display the process id.
	protected Text pidText;
	// The text field to display the parent process id.
	protected Text ppidText;
	// The  text field to display the internal process id.
	protected Text ipidText;
	// The text field to display the internal parent process id.
	protected Text ippidText;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection#createControls(org.eclipse.swt.widgets.Composite, org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	@Override
    public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
	    super.createControls(parent, aTabbedPropertySheetPage);
	    pidText = createTextField(null, Messages.IDSection_ProcessID);
		ppidText = createTextField(pidText, Messages.IDSection_ParentID);
		ipidText = createTextField(ppidText, Messages.IDSection_InternalID);
		ippidText = createTextField(ipidText, Messages.IDSection_InternalPPID);
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection#updateData(org.eclipse.tcf.te.ui.interfaces.IPropertyChangeProvider)
	 */
	@Override
    protected void updateInput(IPeerModelProvider input) {
        Assert.isTrue(input instanceof ProcessTreeNode);
        this.node = (ProcessTreeNode) input;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#refresh()
	 */
	@Override
    public void refresh() {
		SWTControlUtil.setText(pidText, node != null ? Long.toString(node.pid) : ""); //$NON-NLS-1$
		SWTControlUtil.setText(ppidText, node != null ? Long.toString(node.ppid) : ""); //$NON-NLS-1$
		SWTControlUtil.setText(ipidText, node != null && node.id != null ? node.id : ""); //$NON-NLS-1$
		SWTControlUtil.setText(ippidText, node != null && node.parentId != null ? node.parentId : ""); //$NON-NLS-1$
		super.refresh();
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection#getText()
	 */
	@Override
	protected String getText() {
		return Messages.IDSection_Title;
	}
}
