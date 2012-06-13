/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.nls;

import org.eclipse.osgi.util.NLS;

/**
 * Common UI plugin externalized strings management.
 */
public class Messages extends NLS {

	// The plug-in resource bundle name
	private static final String BUNDLE_NAME = "org.eclipse.tcf.te.ui.nls.Messages"; //$NON-NLS-1$

	/**
	 * Static constructor.
	 */
	static {
		// Load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	// **** Declare externalized string id's down here *****

	public static String CollapseAllAction_Tooltip;
	public static String ConfigFilterAction_PromptMessage;
	public static String ConfigFilterAction_Title;
	public static String ConfigFilterAction_TooltipText;

	public static String NewWizard_dialog_title;

	public static String NewWizardSelectionPage_title;
	public static String NewWizardSelectionPage_description;
	public static String NewWizardSelectionPage_wizards;
	public static String NewWizardSelectionPage_createWizardFailed;

	public static String NewWizardViewerFilter_error_evaluationFailed;

	public static String AbstractTreeControl_HelpTooltip;

	public static String NodePropertiesTableControl_section_title;
	public static String NodePropertiesTableControl_section_title_noSelection;
	public static String NodePropertiesTableControl_column_name_label;
	public static String NodePropertiesTableControl_column_value_label;

	public static String Pending_Label;
	public static String PendingOperation_label;

	public static String EditBrowseTextControl_button_label;

	public static String DefaultStatusHandler_question_title;
	public static String DefaultStatusHandler_warning_title;
	public static String DefaultStatusHandler_error_title;
	public static String DefaultStatusHandler_information_title;
	public static String DefaultStatusHandler_toggleMessage_doNotShowAgain;

	public static String FilteredCheckedListDialog_DesAllText;
	public static String FilteredCheckedListDialog_SelAllText;
	public static String NameValuePairDialog_missingName_error;
	public static String NameValuePairDialog_missingValue_error;
	public static String NameValuePairDialog_usedOrIllegalName_error;

	public static String PreferencePage_label;
	public static String QuickFilterPopup_PromptMessage;
	public static String TreeViewerSearchDialog_AdvancedOptions;
	public static String TreeViewerSearchDialog_All;
	public static String TreeViewerSearchDialog_BFS;
	public static String TreeViewerSearchDialog_BtnBackText;
	public static String TreeViewerSearchDialog_BtnCloseText;
	public static String TreeViewerSearchDialog_BtnSearchText;
	public static String TreeViewerSearchDialog_DepthFirst;
	public static String TreeViewerSearchDialog_DFS;
	public static String TreeViewerSearchDialog_JobName;
	public static String TreeViewerSearchDialog_MainTaskName;
	public static String TreeViewerSearchDialog_NoMoreNodeFound;
	public static String TreeViewerSearchDialog_NoSuchNode;
	public static String TreeViewerSearchDialog_Scope;
	public static String TreeViewerSearchDialog_SearchAlgorithm;
	public static String TreeViewerSearchDialog_SearchNodesUsing;
	public static String TreeViewerSearchDialog_Selected;
	public static String TreeViewerSearchDialog_BreadthFirst;
	public static String ViewerStateManager_MkdirFailed;
	public static String TreeViewerSearchDialog_BtnWrapText;
	public static String TreeViewerSearchDialog_UseOptions;
}
