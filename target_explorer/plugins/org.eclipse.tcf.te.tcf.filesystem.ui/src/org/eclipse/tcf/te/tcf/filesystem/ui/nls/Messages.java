/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * William Chen (Wind River) - [345384] Provide property pages for remote file system nodes
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.nls;

import java.lang.reflect.Field;

import org.eclipse.osgi.util.NLS;

/**
 * File System plug-in externalized strings management.
 */
public class Messages extends NLS {

	// The plug-in resource bundle name
	private static final String BUNDLE_NAME = "org.eclipse.tcf.te.tcf.filesystem.ui.nls.Messages"; //$NON-NLS-1$

	/**
	 * Static constructor.
	 */
	static {
		// Load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	/**
	 * Returns if or if not this NLS manager contains a constant for
	 * the given externalized strings key.
	 *
	 * @param key The externalized strings key or <code>null</code>.
	 * @return <code>True</code> if a constant for the given key exists, <code>false</code> otherwise.
	 */
	public static boolean hasString(String key) {
		if (key != null) {
			try {
				Field field = Messages.class.getDeclaredField(key);
				return field != null;
			} catch (NoSuchFieldException e) { /* ignored on purpose */ }
		}

		return false;
	}

	/**
	 * Returns the corresponding string for the given externalized strings
	 * key or <code>null</code> if the key does not exist.
	 *
	 * @param key The externalized strings key or <code>null</code>.
	 * @return The corresponding string or <code>null</code>.
	 */
	public static String getString(String key) {
		if (key != null) {
			try {
				Field field = Messages.class.getDeclaredField(key);
				if (field != null) {
					return (String)field.get(null);
				}
			} catch (Exception e) { /* ignored on purpose */ }
		}

		return null;
	}
	public static String FSFolderSelectionDialog_MoveDialogMessage;
	public static String FSFolderSelectionDialog_MoveDialogTitle;
	public static String FSOpenFileDialog_message;
	public static String FSOpenFileDialog_title;
	public static String FSDelete_ConfirmDelete;
	public static String FSDelete_ConfirmMessage;
	public static String FSDelete_ButtonCancel;
	public static String FSDelete_ButtonNo;
	public static String FSDelete_ButtonYes;
	public static String FSDelete_ButtonYes2All;
	public static String DeleteFilesHandler_DeleteMultipleFilesConfirmation;
	public static String DeleteFilesHandler_DeleteOneFileConfirmation;
	public static String DeleteFilesHandler_ConfirmDialogTitle;
	public static String FSRenamingAssistant_NameAlreadyExists;
	public static String FSRenamingAssistant_SpecifyNonEmptyName;
	public static String FSRenamingAssistant_UnixIllegalCharacters;
	public static String FSRenamingAssistant_WinIllegalCharacters;
	public static String LocalTypedElement_SavingFile;
	public static String MergeEditorInput_LocalFile;
	public static String MergeEditorInput_RemoteFile;
	public static String MergeEditorInput_CompareLeftAndRight;
	public static String MergeEditorInput_CompareWithLocalCache;
	public static String MergeInput_CopyNotSupported;
	public static String RemoteTypedElement_GettingRemoteContent;
	public static String RemoteTypedElement_DowloadingFile;
	public static String FSDropTargetListener_ConfirmMoveTitle;
	public static String FSDropTargetListener_MovingWarningMultiple;
	public static String FSDropTargetListener_MovingWarningSingle;
	public static String FSExplorerEditorPage_PageTitle;
	public static String FSUpload_Cancel;
	public static String FSUpload_No;
	public static String FSUpload_OverwriteConfirmation;
	public static String FSUpload_OverwriteTitle;
	public static String FSUpload_Yes;
	public static String FSUpload_YesToAll;
	public static String FSOperation_ConfirmDialogCancel;
	public static String FSOperation_ConfirmDialogNo;
	public static String FSOperation_ConfirmDialogYes;
	public static String FSOperation_ConfirmDialogYesToAll;
	public static String FSOperation_ConfirmFileReplace;
	public static String FSOperation_ConfirmFileReplaceMessage;
	public static String FSOperation_ConfirmFolderReplaceMessage;
	public static String FSOperation_ConfirmFolderReplaceTitle;
	public static String OpenFileHandler_OpeningBinaryNotSupported;
	public static String OpenFileHandler_Warning;
	public static String OpenWithMenu_ChooseEditorForOpening;
	public static String OpenWithMenu_DefaultEditor;
	public static String OpenWithMenu_NoEditorFound;
	public static String OpenWithMenu_OpenWith;
	public static String FSRename_RenameFileFolderTitle;
	public static String RenameFilesHandler_TitleRename;
	public static String RenameFilesHandler_TitleRenameFile;
	public static String RenameFilesHandler_TitleRenameFolder;
	public static String RenameFilesHandler_PromptNewName;
	public static String RenameFilesHandler_RenamePromptMessage;
	public static String PreferencePage_AutoSavingText;
	public static String PreferencePage_CopyOwnershipText;
	public static String PreferencePage_CopyPermissionText;
	public static String PreferencePage_PersistExpanded;
	public static String PreferencePage_RenamingOptionText;
	public static String AdvancedAttributesDialog_FileBanner;
	public static String AdvancedAttributesDialog_FolderBanner;
	public static String AdvancedAttributesDialog_CompressEncrypt;
	public static String AdvancedAttributesDialog_ArchiveIndex;
	public static String AdvancedAttributesDialog_IndexFile;
	public static String AdvancedAttributesDialog_IndexFolder;
	public static String AdvancedAttributesDialog_FileArchive;
	public static String AdvancedAttributesDialog_FolderArchive;
	public static String AdvancedAttributesDialog_Encrypt;
	public static String AdvancedAttributesDialog_Compress;
	public static String AdvancedAttributesDialog_ShellTitle;
	public static String GeneralInformationPage_Accessed;
	public static String GeneralInformationPage_Advanced;
	public static String GeneralInformationPage_Attributes;
	public static String GeneralInformationPage_Computer;
	public static String GeneralInformationPage_FileSizeInfo;
	public static String GeneralInformationPage_Hidden;
	public static String GeneralInformationPage_Location;
	public static String GeneralInformationPage_Modified;
	public static String GeneralInformationPage_Name;
	public static String GeneralInformationPage_ReadOnly;
	public static String GeneralInformationPage_Size;
	public static String GeneralInformationPage_Type;
	public static String GeneralInformationPage_PermissionText;
	public static String PermissionsGroup_Executable;
	public static String PermissionsGroup_GroupPermissions;
	public static String PermissionsGroup_OtherPermissions;
	public static String PermissionsGroup_Readable;
	public static String PermissionsGroup_UserPermissions;
	public static String PermissionsGroup_Writable;
	public static String BasicFolderSection_BasicInfoText;
	public static String LinuxPermissionsSection_Permissions;
	public static String WindowsAttributesSection_Attributes;
	public static String FolderValidator_DirNotExist;
	public static String FolderValidator_NotWritable;
	public static String FolderValidator_SpecifyFolder;
	public static String NameValidator_SpecifyFolder;
	public static String NewFileWizard_NewFileWizardTitle;
	public static String NewFileWizardPage_NewFileWizardPageDescription;
	public static String NewFileWizardPage_NewFileWizardPageNameLabel;
	public static String NewFileWizardPage_NewFileWizardPageTitle;
	public static String NewFolderWizard_NewFolderWizardTitle;
	public static String NewFolderWizardPage_NewFolderWizardPageDescription;
	public static String NewFolderWizardPage_NewFolderWizardPageNameLabel;
	public static String NewFolderWizardPage_NewFolderWizardPageTitle;
	public static String NewNodeWizardPage_PromptFolderLabel;
	public static String TargetSelectionPage_Description;
	public static String TargetSelectionPage_Targets;
	public static String TargetSelectionPage_Title;
}
