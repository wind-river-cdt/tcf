/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.help;

import org.eclipse.tcf.te.tcf.ui.activator.UIPlugin;

/**
 * Context help id definitions.
 */
public interface IContextHelpIds {

	/**
	 * TCF UI plug-in common context help id prefix.
	 */
	public final static String PREFIX = UIPlugin.getUniqueIdentifier() + "."; //$NON-NLS-1$

	// ***** Wizards and Wizard Pages *****

	/**
	 * New TCF target wizard main page.
	 */
	public final static String NEW_TARGET_WIZARD_PAGE = PREFIX + "NewTargetWizardPage"; //$NON-NLS-1$

	// ***** Editors and Editor Pages *****

	/**
	 * Target Explorer details editor page: Node properties
	 */
	public final static String NODE_PROPERTIES_EDITOR_PAGE = PREFIX + "NodePropertiesEditorPage"; //$NON-NLS-1$

	/**
	 * Peer overview editor page.
	 */
	public final static String OVERVIEW_EDITOR_PAGE = PREFIX + "OverviewEditorPage"; //$NON-NLS-1$

	// ***** Dialogs and Dialog Pages *****

	/**
	 * TCF agent selection dialog.
	 */
	public final static String AGENT_SELECTION_DIALOG = PREFIX + "AgentSelectionDialog"; //$NON-NLS-1$

	// ***** Message dialog boxes *****

	/**
	 * Delete command handler: Delete operation failed.
	 */
	public final static String MESSAGE_DELETE_FAILED = PREFIX + ".status.messageDeleteFailed"; //$NON-NLS-1$

	/**
	 * Rename command handler: Rename operation failed.
	 */
	public final static String MESSAGE_RENAME_FAILED = PREFIX + ".status.messageRenameFailed"; //$NON-NLS-1$

	/**
	 * Redirect command handler: Redirect operation failed.
	 */
	public final static String MESSAGE_REDIRECT_FAILED = PREFIX + ".status.messageRedirectFailed"; //$NON-NLS-1$

	/**
	 * Reset redirect command handler: Reset redirect operation failed.
	 */
	public final static String MESSAGE_RESET_REDIRECT_FAILED = PREFIX + ".status.messageResetRedirectFailed"; //$NON-NLS-1$

	/**
	 * Offline command handler: Make offline operation failed.
	 */
	public final static String MESSAGE_MAKEOFFLINE_FAILED = PREFIX + ".status.messageMakeOfflineFailed"; //$NON-NLS-1$
}
