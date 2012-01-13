/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.nls;

import org.eclipse.osgi.util.NLS;

/**
 * Target Explorer TCF processes extensions UI plug-in externalized strings management.
 */
public class Messages extends NLS {

	// The plug-in resource bundle name
	private static final String BUNDLE_NAME = "org.eclipse.tcf.te.tcf.processes.ui.nls.Messages"; //$NON-NLS-1$

	/**
	 * Static constructor.
	 */
	static {
		// Load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	// **** Declare externalized string id's down here *****

	public static String ProcessExplorerTreeControl_section_title;

	public static String ProcessesTreeControl_column_name_label;
	public static String ProcessesTreeControl_column_pid_label;
	public static String ProcessesTreeControl_column_ppid_label;
	public static String ProcessesTreeControl_column_state_label;
	public static String ProcessesTreeControl_column_user_label;

	public static String ProcessLabelProvider_NullNameNodeLabel;
	public static String ProcessLabelProvider_RootNodeLabel;

	public static String ProcessPreferencePage_BiggerThanZero;
	public static String ProcessPreferencePage_DefineMoreThanOne;

	public static String ProcessPreferencePage_EditButtonLabel;
	public static String ProcessPreferencePage_InvalidNumber;
	public static String ProcessPreferencePage_MaxMRUCount;
	public static String ProcessPreferencePage_MRUCountLabel;
	public static String ProcessPreferencePage_NameLabel;
	public static String ProcessPreferencePage_NewButtonLabel;
	public static String ProcessPreferencePage_PageDescription;
	public static String ProcessPreferencePage_RemoveButtonLabel;
	public static String ProcessPreferencePage_ValueLabel;
	public static String ProcessSelectionDialog_title;

	public static String LaunchObjectDialog_title;
	public static String LaunchObjectDialog_image_label;
	public static String LaunchObjectDialog_arguments_label;
	public static String LaunchObjectDialog_group_label;
	public static String LaunchObjectDialog_lineseparator_label;
	public static String LaunchObjectDialog_lineseparator_default;
	public static String LaunchObjectDialog_lineseparator_lf;
	public static String LaunchObjectDialog_lineseparator_crlf;
	public static String LaunchObjectDialog_lineseparator_cr;

	public static String LaunchProcessesCommandHandler_error_title;

	public static String AbstractChannelCommandHandler_statusDialog_title;

	public static String AdvancedPropertiesSection_Name;
	public static String AdvancedPropertiesSection_Value;

	public static String BasicContextSection_File;
	public static String BasicContextSection_Group;
	public static String BasicContextSection_Root;
	public static String BasicContextSection_State;
	public static String BasicContextSection_Title;
	public static String BasicContextSection_User;
	public static String BasicContextSection_WorkDir;
	public static String BasicInformationSection_Name;
	public static String BasicInformationSection_State;
	public static String BasicInformationSection_Title;
	public static String BasicInformationSection_Type;
	public static String BasicInformationSection_User;

	public static String ConfigIntervalDynamicContribution_Custom;

	public static String ContextIDSection_ContextIDs;
	public static String ContextIDSection_GroupID;
	public static String ContextIDSection_ID;
	public static String ContextIDSection_ParentID;
	public static String ContextIDSection_PID;
	public static String ContextIDSection_PPID;
	public static String ContextIDSection_TracerPID;
	public static String ContextIDSection_TTY_GRPID;
	public static String ContextIDSection_UserGRPID;
	public static String ContextIDSection_UserID;

	public static String EditSpeedGradeDialog_DialogMessage;
	public static String EditSpeedGradeDialog_DialogTitle;
	public static String EditSpeedGradeDialog_GradeSameValue;
	public static String EditSpeedGradeDialog_NameLabel;
	public static String EditSpeedGradeDialog_ValueLabel;

	public static String IDSection_InternalID;
	public static String IDSection_InternalPPID;
	public static String IDSection_ParentID;
	public static String IDSection_ProcessID;
	public static String IDSection_Title;

	public static String IntervalConfigDialog_BiggerThanZero;
	public static String IntervalConfigDialog_ChoiceOneLabel;
	public static String IntervalConfigDialog_ChoiceTwoLabel;
	public static String IntervalConfigDialog_DialogTitle;
	public static String IntervalConfigDialog_InvalidNumber;
	public static String IntervalConfigDialog_NonEmpty;
	public static String IntervalConfigDialog_SECOND_ABBR;
	public static String IntervalConfigDialog_SECONDS;
	public static String IntervalConfigDialog_SelectSpeed;
	public static String IntervalConfigDialog_SPEED;
	public static String IntervalConfigDialog_ZeroWarning;

	public static String MemorySection_PSize;
	public static String MemorySection_RSS;
	public static String MemorySection_Title;
	public static String MemorySection_VSize;

	public static String NewSpeedGradeDialog_DialogMessage;
	public static String NewSpeedGradeDialog_DialogTitle;
	public static String NewSpeedGradeDialog_EnterName;
	public static String NewSpeedGradeDialog_GradeExists;
	public static String NewSpeedGradeDialog_GradeSameValue;
	public static String NewSpeedGradeDialog_NameLabel;
	public static String NewSpeedGradeDialog_ValueLabel;

	public static String AttachStep_error_title;
	public static String AttachStep_error_connect;
	public static String AttachStep_error_getContext;
	public static String AttachStep_error_attach;
	public static String AttachStep_error_openChannel;

	public static String DetachStep_error_title;
	public static String DetachStep_error_disconnect;
	public static String DetachStep_error_getContext;
	public static String DetachStep_error_detach;
	public static String DetachStep_error_openChannel;
}
