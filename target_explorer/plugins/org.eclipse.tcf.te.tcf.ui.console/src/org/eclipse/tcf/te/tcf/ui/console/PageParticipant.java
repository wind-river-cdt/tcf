/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.console;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.tcf.te.tcf.ui.console.nls.Messages;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * Console page participant implementation.
 */
public class PageParticipant extends PlatformObject implements IConsolePageParticipant {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsolePageParticipant#activated()
	 */
	@Override
    public void activated() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsolePageParticipant#deactivated()
	 */
	@Override
    public void deactivated() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsolePageParticipant#dispose()
	 */
	@Override
    public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsolePageParticipant#init(org.eclipse.ui.part.IPageBookViewPage, org.eclipse.ui.console.IConsole)
	 */
	@Override
    public void init(IPageBookViewPage page, IConsole console) {
		if (page == null || page.getSite() == null || page.getSite().getActionBars() == null || page.getSite().getActionBars().getToolBarManager() == null) return;

		// Create the command contribution item parameters
		CommandContributionItemParameter parameters;
		parameters = new CommandContributionItemParameter(page.getSite(),
		                                                  "org.eclipse.tcf.te.tcf.ui.console.commands.remove", //$NON-NLS-1$
		                                                  "org.eclipse.tcf.te.tcf.ui.console.command.remove", //$NON-NLS-1$
														  CommandContributionItem.STYLE_PUSH);
		parameters.label = Messages.PageParticipant_command_remove_label;
		parameters.tooltip = Messages.PageParticipant_command_remove_label;

		// Create the contribution item and append to the LAUNCH_GROUP
		page.getSite().getActionBars().getToolBarManager().appendToGroup(IConsoleConstants.LAUNCH_GROUP, new CommandContributionItem(parameters));
	}
}
