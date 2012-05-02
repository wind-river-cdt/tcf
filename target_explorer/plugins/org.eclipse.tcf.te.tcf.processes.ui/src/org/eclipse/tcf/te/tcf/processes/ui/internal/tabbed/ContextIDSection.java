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
 * The property section to display the context IDs of a process.
 */
public class ContextIDSection extends BaseTitledSection {
	// The system monitor context for the selected process node.
	protected ISysMonitor.SysMonitorContext context;
	// The text field to display the id of the process context.
	protected Text idText;
	// The text field to display the parent id of the process context.
	protected Text parentIdText;
	// The text field to display the process group id.
	protected Text pgrpText;
	// The text field to display the process id.
	protected Text pidText;
	// The text field to display the parent process id.
	protected Text ppidText;
	// The text field to display the process TTY group ID.
	protected Text tgidText;
	// The text field to display the tracer process's id.
	protected Text tracerPidText;
	// The text field to display the user id of the process.
	protected Text uidText;
	// The text field to display the user group id of the process.
	protected Text ugidText;
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection#createControls(org.eclipse.swt.widgets.Composite, org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	@Override
    public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
	    super.createControls(parent, aTabbedPropertySheetPage);
	    idText = createTextField(null, Messages.ContextIDSection_ID);
		parentIdText = createTextField(idText, Messages.ContextIDSection_ParentID);
		pgrpText = createTextField(parentIdText, Messages.ContextIDSection_GroupID);
		pidText = createTextField(pgrpText, Messages.ContextIDSection_PID);
		ppidText = createTextField(pidText, Messages.ContextIDSection_PPID);
		tgidText = createTextField(ppidText, Messages.ContextIDSection_TTY_GRPID);
		tracerPidText = createTextField(tgidText, Messages.ContextIDSection_TracerPID);
		uidText = createTextField(tracerPidText, Messages.ContextIDSection_UserID);
		ugidText = createTextField(uidText, Messages.ContextIDSection_UserGRPID);
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection#updateData(org.eclipse.tcf.te.ui.interfaces.IPropertyChangeProvider)
	 */
	@Override
    protected void updateInput(IPeerModelProvider input) {
        Assert.isTrue(input instanceof ProcessTreeNode);
        ProcessTreeNode node = (ProcessTreeNode) input;
        context = node.context;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#refresh()
	 */
	@Override
    public void refresh() {
		this.idText.setText(context == null ? "" : (context.getID() != null ? "" : context.getID())); //$NON-NLS-1$ //$NON-NLS-2$
		this.parentIdText.setText(context == null ? "" : (context.getParentID()==null?"":context.getParentID())); //$NON-NLS-1$ //$NON-NLS-2$
		this.pgrpText.setText(context == null ? "" : (""+context.getPGRP())); //$NON-NLS-1$ //$NON-NLS-2$
		this.pidText.setText(context == null ? "" : (""+context.getPID())); //$NON-NLS-1$ //$NON-NLS-2$
		this.ppidText.setText(context == null ? "" : (""+context.getPPID())); //$NON-NLS-1$ //$NON-NLS-2$
		this.tgidText.setText(context == null ? "" : (""+context.getTGID())); //$NON-NLS-1$ //$NON-NLS-2$
		this.tracerPidText.setText(context == null ? "" : (""+context.getTracerPID())); //$NON-NLS-1$ //$NON-NLS-2$
		this.uidText.setText(context == null ? "" : (""+context.getUID())); //$NON-NLS-1$ //$NON-NLS-2$
		this.ugidText.setText(context == null ? "" : ("" +context.getUGID())); //$NON-NLS-1$ //$NON-NLS-2$
		super.refresh();
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection#getText()
	 */
	@Override
	protected String getText() {
		return Messages.ContextIDSection_ContextIDs;
	}

}
