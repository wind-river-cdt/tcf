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
package org.eclipse.tcf.te.tcf.filesystem.core.nls;

import org.eclipse.osgi.util.NLS;

/**
 * File System plug-in externalized strings management.
 */
public class Messages extends NLS {

	// The plug-in resource bundle name
	private static final String BUNDLE_NAME = "org.eclipse.tcf.te.tcf.filesystem.core.nls.Messages"; //$NON-NLS-1$

	/**
	 * Static constructor.
	 */
	static {
		// Load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	// **** Declare externalized string id's down here *****

	public static String FSTreeNodeContentProvider_rootNode_label;
	public static String FSTreeNode_TypeFile;
	public static String FSTreeNode_TypeFileFolder;
	public static String FSTreeNode_TypeLocalDisk;
	public static String FSTreeNode_TypeSystemFile;
	public static String FSTreeNode_TypeUnknownFile;

	public static String Operation_DeletingFileFailed;
	public static String Operation_NotResponding;
	public static String Operation_OpeningChannelFailureMessage;
	public static String Operation_NoFileSystemError;
	public static String Operation_CannotOpenDir;
	public static String Operation_CopyNOfFile;
	public static String Operation_CopyOfFile;
	public static String Operation_CannotCreateDirectory;
	public static String Operation_TimeoutOpeningChannel;

	public static String OpCopy_Copying;
	public static String OpCopy_CannotCopyFile;
	public static String OpCopy_CopyingFile;

	public static String OpCreate_TaskName;

	public static String TcfURLConnection_NoFileHandleReturned;
	public static String TcfURLConnection_NoPeerFound;
	public static String TcfURLConnection_NoSuchTcfAgent;

	public static String OpDelete_Deleting;
	public static String OpDelete_PrepareToDelete;
	public static String OpDelete_RemovingFileFolder;

	public static String OpDownload_Downloading;
	public static String OpDownload_DownloadingMultipleFiles;
	public static String OpDownload_DownloadingSingleFile;

	public static String OpMove_Moving;
	public static String OpMove_FileExistsError;
	public static String OpMove_FolderExistsError;
	public static String OpMove_CannotMove;
	public static String OpMove_MovingFile;

	public static String OpOutStreamOp_DownloadingProgress;

	public static String OpRefresh_RefreshJobTitle;

	public static String OpRename_CannotRename;
	public static String OpRename_TitleRename;

	public static String OpStreamOp_SetReadOnlyFailed;
	public static String OpStreamOp_Bytes;
	public static String OpStreamOp_KBs;
	public static String OpStreamOp_MBs;

	public static String OpUpload_UploadingProgress;
	public static String OpUpload_UploadNFiles;
	public static String OpUpload_UploadSingleFile;

	public static String TcfInputStream_NoDataAvailable;
	public static String TcfInputStream_StreamClosed;
	public static String TcfOutputStream_StreamClosed;
	public static String TcfURLStreamHandlerService_ErrorURLFormat;
	public static String TcfURLStreamHandlerService_IllegalCharacter;
	public static String TcfURLStreamHandlerService_OnlyDiskPartError;

	public static String CacheManager_MkdirFailed;

	public static String FileTransferService_error_mkdirFailed;
	public static String BlockingFileSystemProxy_TimeoutOpeningFile;
	public static String BlockingFileSystemProxy_TimeoutClosingFile;
	public static String BlockingFileSystemProxy_TimeoutReadingFile;
	public static String BlockingFileSystemProxy_TimeoutWritingFile;
	public static String BlockingFileSystemProxy_TimeoutStat;
	public static String BlockingFileSystemProxy_TimeoutLstat;
	public static String BlockingFileSystemProxy_TimeoutFstat;
	public static String BlockingFileSystemProxy_TimeoutSetStat;
	public static String BlockingFileSystemProxy_TimeoutFSetStat;
	public static String BlockingFileSystemProxy_TimeoutOpeningDir;
	public static String BlockingFileSystemProxy_TimeoutReadingDir;
	public static String BlockingFileSystemProxy_TimeoutMakingDir;
	public static String BlockingFileSystemProxy_TimeoutRemovingDir;
	public static String BlockingFileSystemProxy_TimeoutListingRoots;
	public static String BlockingFileSystemProxy_TimeoutRemovingFile;
	public static String BlockingFileSystemProxy_TimeoutGettingRealPath;
	public static String BlockingFileSystemProxy_TimeoutRenamingFile;
	public static String BlockingFileSystemProxy_TimeoutReadingLink;
	public static String BlockingFileSystemProxy_TimeoutSymLink;
	public static String BlockingFileSystemProxy_TimeoutCopying;
	public static String BlockingFileSystemProxy_TimeoutGettingUser;
}
