/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * William Chen (Wind River) - [345384] Provide property pages for remote file system nodes
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.nls;

import java.lang.reflect.Field;

import org.eclipse.osgi.util.NLS;

/**
 * File System plug-in externalized strings management.
 */
public class Messages extends NLS {

	// The plug-in resource bundle name
	private static final String BUNDLE_NAME = "org.eclipse.tcf.te.tcf.filesystem.internal.nls.Messages"; //$NON-NLS-1$

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

	// **** Declare externalized string id's down here *****

	public static String AdvancedAttributesDialog_Archive;
	public static String AdvancedAttributesDialog_ArchiveIndex;
	public static String AdvancedAttributesDialog_Compress;
	public static String AdvancedAttributesDialog_Compressed;
	public static String AdvancedAttributesDialog_CompressEncrypt;
	public static String AdvancedAttributesDialog_Device;
	public static String AdvancedAttributesDialog_Directory;
	public static String AdvancedAttributesDialog_Encrypt;
	public static String AdvancedAttributesDialog_Encrypted;
	public static String AdvancedAttributesDialog_FileArchive;
	public static String AdvancedAttributesDialog_FileBanner;
	public static String AdvancedAttributesDialog_FolderArchive;
	public static String AdvancedAttributesDialog_FolderBanner;
	public static String AdvancedAttributesDialog_Hidden;
	public static String AdvancedAttributesDialog_Indexed;
	public static String AdvancedAttributesDialog_IndexFile;
	public static String AdvancedAttributesDialog_IndexFolder;
	public static String AdvancedAttributesDialog_Normal;
	public static String AdvancedAttributesDialog_Offline;
	public static String AdvancedAttributesDialog_ReadOnly;
	public static String AdvancedAttributesDialog_Reparse;
	public static String AdvancedAttributesDialog_ShellTitle;
	public static String AdvancedAttributesDialog_Sparse;
	public static String AdvancedAttributesDialog_System;
	public static String AdvancedAttributesDialog_Temporary;
	public static String AdvancedAttributesDialog_Virtual;

	public static String CacheManager_Bytes;
	public static String CacheManager_DowloadingFile;
	public static String CacheManager_DownloadingError;
	public static String CacheManager_DownloadingProgress;
	public static String CacheManager_KBs;
	public static String CacheManager_MBs;
	public static String CacheManager_UploadingProgress;
	public static String CacheManager_UploadNFiles;
	public static String CacheManager_UploadSingleFile;

	public static String CmmitHandler_Cancel;
	public static String CmmitHandler_CommitAnyway;
	public static String CmmitHandler_ErrorTitle;
	public static String CmmitHandler_FileDeleted;
	public static String CmmitHandler_Merge;
	public static String CmmitHandler_StateChangedDialogTitle;
	public static String CmmitHandler_StateChangedMessage;

	public static String FolderValidator_DirNotExist;
	public static String FolderValidator_NotWritable;
	public static String FolderValidator_SpecifyFolder;
	public static String FSDelete_ButtonCancel;
	public static String FSDelete_ButtonNo;
	public static String FSDelete_ButtonYes;
	public static String FSDelete_ButtonYes2All;
	public static String FSDelete_CannotRemoveFile;
	public static String FSDelete_CannotRemoveFolder;
	public static String FSDelete_ConfirmDelete;
	public static String FSDelete_ConfirmMessage;
	public static String FSDelete_Deleting;
	public static String FSDelete_PrepareToDelete;
	public static String FSDelete_RemovingFileFolder;

	public static String DeleteFilesHandler_DeleteMultipleFilesConfirmation;
	public static String DeleteFilesHandler_DeleteOneFileConfirmation;
	public static String DeleteFilesHandler_ConfirmDialogTitle;
	
	public static String GeneralInformationPage_Accessed;
	public static String GeneralInformationPage_Advanced;
	public static String GeneralInformationPage_Attributes;
	public static String GeneralInformationPage_Computer;
	public static String GeneralInformationPage_File;
	public static String GeneralInformationPage_FileSizeInfo;
	public static String GeneralInformationPage_Folder;
	public static String GeneralInformationPage_Hidden;
	public static String GeneralInformationPage_Location;
	public static String GeneralInformationPage_Modified;
	public static String GeneralInformationPage_Name;
	public static String GeneralInformationPage_ReadOnly;
	public static String GeneralInformationPage_Size;
	public static String GeneralInformationPage_Type;
	public static String GeneralInformationPage_PermissionText;
	public static String GeneralInformationPage_PropertiesChangeFailure;
	public static String GeneralInformationPage_PropertiesChangeTitle;
	public static String GeneralInformationPage_UnknownFileType;

	public static String FSExplorerTreeControl_section_title;

	public static String FSFolderSelectionDialog_MoveDialogMessage;
	public static String FSFolderSelectionDialog_MoveDialogTitle;
	public static String FSTreeControl_column_name_label;
	public static String FSTreeControl_column_size_label;
	public static String FSTreeControl_column_modified_label;

	public static String FSOpenFileDialog_title;
	public static String FSOperation_CopyNOfFile;
	public static String FSOperation_CopyOfFile;

	public static String LocalTypedElement_SavingFile;

	public static String OpenFileHandler_Cancel;
	public static String OpenFileHandler_ConflictingMessage;
	public static String OpenFileHandler_ConflictingTitle;
	public static String OpenFileHandler_Merge;
	public static String OpenFileHandler_OpenAnyway;
	public static String OpenFileHandler_OpeningBinaryNotSupported;
	public static String OpenFileHandler_Warning;

	public static String OpenWithMenu_ChooseEditorForOpening;
	public static String OpenWithMenu_DefaultEditor;
	public static String OpenWithMenu_NoEditorFound;
	public static String OpenWithMenu_OpenWith;

	public static String FSCopy_CannotCopyFile;
	public static String FSCopy_CopyFileFolderTitle;
	public static String FSCopy_Copying;
	public static String FSCopy_CopyingFile;
	public static String FSCopy_PrepareToCopy;

	public static String FSMove_CannotMove;
	public static String FSMove_FileExistsError;
	public static String FSMove_FolderExistsError;
	public static String FSMove_MovingFile;
	public static String FSMove_Moving;
	public static String FSMove_PrepareToMove;
	public static String FSMove_MoveFileFolderTitle;

	public static String FSDelete_DeleteFileFolderTitle;

	public static String FSRename_CannotRename;
	public static String FSRename_RenameFileFolderTitle;

	public static String FSOperation_CannotCreateDirectory;
	public static String FSOperation_ConfirmDialogCancel;
	public static String FSOperation_ConfirmDialogNo;
	public static String FSOperation_ConfirmDialogYes;
	public static String FSOperation_ConfirmDialogYesToAll;
	public static String FSOperation_ConfirmFileReplace;
	public static String FSOperation_ConfirmFileReplaceMessage;
	public static String FSOperation_ConfirmFolderReplaceMessage;
	public static String FSOperation_ConfirmFolderReplaceTitle;
	public static String FSOperation_NoFileSystemError;
	public static String FSOperation_CannotOpenDir;
	public static String FSOperation_CannotReadDir;
	public static String FSOperation_TimedOutWhenOpening;

	public static String PermissionsGroup_Executable;
	public static String PermissionsGroup_GroupPermissions;
	public static String PermissionsGroup_OtherPermissions;
	public static String PermissionsGroup_Readable;
	public static String PermissionsGroup_UserPermissions;
	public static String PermissionsGroup_Writable;

	public static String RemoteTypedElement_GettingRemoteContent;
	public static String RenameFilesHandler_PromptNewName;
	public static String RenameFilesHandler_RenamePromptMessage;
	public static String RenameFilesHandler_TitleRename;
	public static String RenameFilesHandler_TitleRenameFile;
	public static String RenameFilesHandler_TitleRenameFolder;
	public static String FSRenamingAssistant_NameAlreadyExists;
	public static String FSRenamingAssistant_NoNodeSelected;
	public static String FSRenamingAssistant_SpecifyNonEmptyName;
	public static String FSRenamingAssistant_UnixIllegalCharacters;
	public static String FSRenamingAssistant_WinIllegalCharacters;

	public static String SaveAllListener_Cancel;
	public static String SaveAllListener_Merge;
	public static String SaveAllListener_SaveAnyway;
	public static String SaveAllListener_SingularMessage;
	public static String SaveAllListener_StateChangedDialogTitle;

	public static String SaveListener_Cancel;
	public static String SaveListener_Merge;
	public static String SaveListener_SaveAnyway;
	public static String SaveListener_StateChangedDialogTitle;
	public static String SaveListener_StateChangedMessage;

	public static String StateManager_CannotGetFileStateMessage2;
	public static String StateManager_CannotGetFileStatMessage;
	public static String StateManager_CannotSetFileStateMessage;
	public static String StateManager_CannotSetFileStateMessage2;
	public static String StateManager_CommitFailureTitle;
	public static String StateManager_RefreshFailureTitle;
	public static String StateManager_TCFNotProvideFSMessage;
	public static String StateManager_TCFNotProvideFSMessage2;
	public static String StateManager_UpdateFailureTitle;

	public static String TargetExplorerPreferencePage_AutoSavingText;
	public static String TargetExplorerPreferencePage_CopyOwnershipText;
	public static String TargetExplorerPreferencePage_CopyPermissionText;
	public static String TargetExplorerPreferencePage_RenamingOptionText;
	public static String TargetSelectionPage_Description;
	public static String TargetSelectionPage_Targets;
	public static String TargetSelectionPage_Title;
	public static String TcfInputStream_CloseTimeout;
	public static String TcfInputStream_NoDataAvailable;
	public static String TcfInputStream_NoFileReturned;
	public static String TcfInputStream_NoFSServiceAvailable;
	public static String TcfInputStream_OpenFileTimeout;
	public static String TcfInputStream_OpenTCFTimeout;
	public static String TcfInputStream_ReadTimeout;
	public static String TcfInputStream_StreamClosed;

	public static String TcfOutputStream_StreamClosed;
	public static String TcfOutputStream_WriteTimeout;

	public static String TcfURLConnection_CloseFileTimeout;
	public static String TcfURLConnection_NoFileHandleReturned;
	public static String TcfURLConnection_NoFSServiceAvailable;
	public static String TcfURLConnection_NoSuchTcfAgent;
	public static String TcfURLConnection_OpenFileTimeout;
	public static String TcfURLConnection_OpenTCFChannelTimeout;

	public static String OpeningChannelFailureMessage;
	public static String OpeningChannelFailureTitle;

	public static String UpdateHandler_Cancel;
	public static String UpdateHandler_Merge;
	public static String UpdateHandler_StateChangedDialogTitle;
	public static String UpdateHandler_StateChangedMessage;
	public static String UpdateHandler_UpdateAnyway;

	public static String UserManager_CannotGetUserAccountMessage;
	public static String UserManager_CannotGetUserAccountMessage2;
	public static String UserManager_TCFNotProvideFSMessage;
	public static String UserManager_UserAccountTitle;

	public static String MergeEditorInput_CompareLeftAndRight;
	public static String MergeEditorInput_CompareWithLocalCache;
	public static String MergeEditorInput_LocalFile;
	public static String MergeEditorInput_RemoteFile;
	public static String MergeInput_CopyNotSupported;
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
}
