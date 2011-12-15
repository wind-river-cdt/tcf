/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.views.scriptpad.console;

import org.eclipse.tcf.te.tcf.ui.console.AbstractConsole;
import org.eclipse.tcf.te.tcf.ui.views.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.ui.views.help.IContextHelpIds;
import org.eclipse.tcf.te.tcf.ui.views.internal.ImageConsts;
import org.eclipse.tcf.te.tcf.ui.views.nls.Messages;

/**
 * Script Pad console implementation.
 */
public class Console extends AbstractConsole {

	/**
     * Constructor.
     */
    public Console() {
		super(Messages.Console_name, UIPlugin.getImageDescriptor(ImageConsts.SCRIPT_PAD_CONSOLE));
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.AbstractConsole#getHelpContextId()
	 */
    @Override
	public String getHelpContextId() {
    	return IContextHelpIds.SCRIPT_PAD_CONSOLE_VIEW;
    }
}
