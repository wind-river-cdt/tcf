/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.dialogs;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.te.tcf.ui.nls.Messages;


/**
 * Agent selection dialog implementation to choose the proxy to redirect the communication through.
 */
public class RedirectAgentSelectionDialog extends AgentSelectionDialog {

	/**
	 * Constructor.
	 *
	 * @param services The list of (remote) services the agents must provide to be selectable, or <code>null</code>.
	 */
	public RedirectAgentSelectionDialog(String[] services) {
		super(services);
	}

	/**
	 * Constructor.
	 *
	 * @param parent The parent shell used to view the dialog, or <code>null</code>.
	 * @param services The list of (remote) services the agents must provide to be selectable, or <code>null</code>.
	 */
	public RedirectAgentSelectionDialog(Shell parent, String[] services) {
		super(parent, services);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.ui.dialogs.AgentSelectionDialog#getDialogTitle()
	 */
	@Override
	protected String getDialogTitle() {
	    return Messages.RedirectAgentSelectionDialog_dialogTitle;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.ui.dialogs.AgentSelectionDialog#getTitle()
	 */
	@Override
	protected String getTitle() {
	    return Messages.RedirectAgentSelectionDialog_title;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.ui.dialogs.AgentSelectionDialog#getDefaultMessage()
	 */
	@Override
	protected String getDefaultMessage() {
	    return Messages.RedirectAgentSelectionDialog_message;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.ui.dialogs.AgentSelectionDialog#supportsMultiSelection()
	 */
	@Override
	protected boolean supportsMultiSelection() {
	    return false;
	}
}
