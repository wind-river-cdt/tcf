/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.operations;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.interfaces.IConfirmCallback;
import org.eclipse.tcf.te.tcf.filesystem.internal.ImageConsts;
import org.eclipse.tcf.te.tcf.filesystem.internal.utils.CacheManager;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.nls.Messages;
import org.eclipse.ui.PlatformUI;

/**
 * Upload multiple files from local system to a remote system.  
 */
public class FSUpload extends FSUIOperation implements IConfirmCallback {
	// The source files to be uploaded.
	String[] sourceFiles;
	// The target folder to which these files are moved to.
	FSTreeNode targetFolder;
	// The callback invoked after uploading.
	ICallback callback;
	// The current children
	List<FSTreeNode> targetChildren;
	
	/**
	 * Create an instance with specified files, target folder and a callback.
	 * 
	 * @param sourceFiles the source files being uploaded.
	 * @param targetFolder the target folder to upload the files to.
	 * @param callback the callback that is invoked after uploading.
	 */
	public FSUpload(String[] sourceFiles, FSTreeNode targetFolder, ICallback callback) {
	    super(Messages.FSUpload_UploadTitle);
	    this.sourceFiles = sourceFiles;
	    this.targetFolder = targetFolder;
	    this.callback = callback;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSOperation#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		try {
			String message;
			if(sourceFiles.length==1)
				message = NLS.bind(Messages.CacheManager_UploadSingleFile, sourceFiles[0]);
			else
				message = NLS.bind(Messages.CacheManager_UploadNFiles, Long.valueOf(sourceFiles.length));
			monitor.beginTask(message, 100);
			File[] files = new File[sourceFiles.length];
			URL[] urls = new URL[sourceFiles.length];
			URL folderURL = targetFolder.getLocationURL();
			for (int i=0;i<files.length;i++) {
				files[i] = new File(sourceFiles[i]);
				urls[i] = new URL(folderURL, files[i].getName());
			}
		    this.targetChildren = getChildren(targetFolder);
			CacheManager.getInstance().uploadFiles(monitor, files, urls, this);
		} catch (Exception e) {
			throw new InvocationTargetException(e);
		} finally {
			monitor.done();
		}
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSUIOperation#doit()
	 */
	@Override
    public IStatus doit() {
	    IStatus status = super.doit();
	    if(callback != null) {
	    	callback.done(this, status);
	    }
	    return status;
    }

	@Override
    public boolean requires(Object object) {
		File file = (File) object;
		String name = file.getName();
		for(FSTreeNode child:targetChildren) {
			if(name.equals(child.name))
				return true;
		}
	    return false;
    }

	@Override
    public int confirms(Object object) {
		final int[] results = new int[1];
		final File file = (File) object;
		Display display = PlatformUI.getWorkbench().getDisplay();
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				String title = Messages.FSUpload_OverwriteTitle;
				String message = NLS.bind(Messages.FSUpload_OverwriteConfirmation, file.getName());
				final Image titleImage = UIPlugin.getImage(ImageConsts.DELETE_READONLY_CONFIRM);
				MessageDialog qDialog = new MessageDialog(parent, title, null, message, 
								MessageDialog.QUESTION, new String[] {Messages.FSUpload_Yes, 
								Messages.FSUpload_YesToAll, Messages.FSUpload_No, Messages.FSUpload_Cancel}, 0) {
					@Override
					public Image getQuestionImage() {
						return titleImage;
					}
				};
				results[0] = qDialog.open();
			}
		});
		return results[0];
    }
}
