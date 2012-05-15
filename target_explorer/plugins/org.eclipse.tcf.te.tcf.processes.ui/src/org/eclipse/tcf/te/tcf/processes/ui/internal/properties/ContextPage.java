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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tcf.services.ISysMonitor.SysMonitorContext;
import org.eclipse.tcf.te.tcf.processes.core.model.ProcessTreeNode;
import org.eclipse.tcf.te.tcf.processes.ui.nls.Messages;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * The property page to display the context IDs of a process.
 */
public class ContextPage extends PropertyPage {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
    @Override
	protected Control createContents(Composite parent) {
		IAdaptable element = getElement();
		Assert.isTrue(element instanceof ProcessTreeNode);

		ProcessTreeNode node = (ProcessTreeNode) element;
		Composite page = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		page.setLayout(gridLayout);
		
		SysMonitorContext context = node.context;
		createField(Messages.ContextPage_File, context == null ? null : context.getFile(), page); 
		createField(Messages.ContextPage_WorkHome, context == null ? null : context.getCurrentWorkingDirectory(), page); 
		createField(Messages.ContextPage_Root, context == null ? null : context.getRoot(), page); 
		createField(Messages.ContextPage_State, context == null ? null : context.getState(), page); 
		createField(Messages.ContextPage_Group, context == null ? null : context.getGroupName(), page); 
		createSeparator(page);
		createField(Messages.ContextPage_ID, context == null ? null : context.getID(), page); 
		createField(Messages.ContextPage_ParentID, context == null ? null : context.getParentID(), page); 
		createField(Messages.ContextPage_GroupID, context == null ? null : Long.valueOf(context.getPGRP()), page); 
		createField(Messages.ContextPage_PID, context == null ? null : Long.valueOf(context.getPID()), page); 
		createField(Messages.ContextPage_PPID, context == null ? null : Long.valueOf(context.getPPID()), page); 
		createField(Messages.ContextPage_TTYGRPID, context == null ? null : Long.valueOf(context.getTGID()), page); 
		createField(Messages.ContextPage_TracerPID, context == null ? null : Long.valueOf(context.getTracerPID()), page); 
		createField(Messages.ContextPage_UserID, context == null ? null : Long.valueOf(context.getUID()), page); 
		createField(Messages.ContextPage_UserGRPID, context == null ? null : Long.valueOf(context.getUGID()), page); 
		createSeparator(page);
		createField(Messages.ContextPage_Virtual, context == null ? null : Long.valueOf(context.getVSize()), page); 
		createField(Messages.ContextPage_Pages, context == null ? null : Long.valueOf(context.getPSize()), page); 
		createField(Messages.ContextPage_Resident, context == null ? null : Long.valueOf(context.getRSS()), page); 

		return page;
	}
	/**
	 * Create a horizontal separator between field sections.
	 *
	 * @param parent
	 *            The parent composite of the separator.
	 */
	protected void createSeparator(Composite parent) {
		Label label = new Label(parent, SWT.SEPARATOR | SWT.SHADOW_ETCHED_IN | SWT.HORIZONTAL);
		GridData data = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
	}
	/**
	 * Create a field displaying the a specific value with a specific label.
	 *
	 * @param text
	 *            The label text for the field.
	 * @param value
	 *            The value to be displayed.
	 * @param parent
	 *            The parent composite of the field.
	 */
	protected void createField(String text, Object value, Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(text);
		GridData data = new GridData();
		data.horizontalAlignment = SWT.LEFT;
		data.verticalAlignment = SWT.TOP;
		label.setLayoutData(data);
		Text txt = new Text(parent, SWT.WRAP | SWT.READ_ONLY);
		data = new GridData();
		data.verticalAlignment = SWT.TOP;
		data.widthHint = 300;
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = GridData.FILL;		
		txt.setLayoutData(data);
		txt.setBackground(txt.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		txt.setText(value == null ? "" : value.toString()); //$NON-NLS-1$
	}	
}
