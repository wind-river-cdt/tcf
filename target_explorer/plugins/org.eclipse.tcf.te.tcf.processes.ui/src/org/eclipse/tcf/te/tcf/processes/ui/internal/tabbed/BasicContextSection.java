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
import org.eclipse.tcf.services.ISysMonitor;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProvider;
import org.eclipse.tcf.te.tcf.processes.core.model.ProcessTreeNode;
import org.eclipse.tcf.te.tcf.processes.ui.nls.Messages;
import org.eclipse.tcf.te.tcf.ui.tabbed.BaseTitledSection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * The property section to display the basic context information of a process.
 */
public class BasicContextSection extends BaseTitledSection {
	// The process context to be displayed.
	protected ISysMonitor.SysMonitorContext context;
	// The text field for the executable file.
	protected Text fileText;
	// The text field for the working directory.
	protected Text workDirText;
	// The text field for the root directory.
	protected Text rootText;
	// The state of the process.
	protected Text stateText;
	// The owner of the process.
	protected Text userText;
	// The owner group of the process.
	protected Text groupText;
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection#createControls(org.eclipse.swt.widgets.Composite, org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	@Override
    public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
	    super.createControls(parent, aTabbedPropertySheetPage);
	    fileText = createWrapTextField(null, Messages.BasicContextSection_File);
		workDirText = createWrapTextField(fileText, Messages.BasicContextSection_WorkDir);
		rootText = createWrapTextField(workDirText, Messages.BasicContextSection_Root);
		stateText = createTextField(rootText, Messages.BasicContextSection_State);
		userText = createTextField(stateText, Messages.BasicContextSection_User);
		groupText = createTextField(userText, Messages.BasicContextSection_Group);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection#updateData(org.eclipse.tcf.te.ui.interfaces.IPropertyChangeProvider)
	 */
	@Override
    protected void updateInput(IPeerModelProvider input) {
        Assert.isTrue(input instanceof ProcessTreeNode);
        ProcessTreeNode node = (ProcessTreeNode) input;
        Assert.isNotNull(node.context);
        context = node.context;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#refresh()
	 */
	@Override
    public void refresh() {
		fileText.setText(context.getFile() == null ? "" : context.getFile()); //$NON-NLS-1$
		workDirText.setText(context.getCurrentWorkingDirectory() == null ? "" : context.getCurrentWorkingDirectory()); //$NON-NLS-1$
		rootText.setText(context.getRoot() == null ? "" : context.getRoot()); //$NON-NLS-1$
		stateText.setText(context.getState() == null ? "" : context.getState()); //$NON-NLS-1$
		userText.setText(context.getUserName() == null ? "" : context.getUserName()); //$NON-NLS-1$
		groupText.setText(context.getGroupName() == null ? "" : context.getGroupName()); //$NON-NLS-1$
		super.refresh();
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection#getText()
	 */
	@Override
	protected String getText() {
		return Messages.BasicContextSection_Title; 
	}
}
