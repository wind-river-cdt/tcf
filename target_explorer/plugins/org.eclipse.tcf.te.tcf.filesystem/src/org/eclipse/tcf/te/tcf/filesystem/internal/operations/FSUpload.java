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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.tcf.filesystem.internal.utils.CacheManager;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.nls.Messages;

/**
 * Upload multiple files from local system to a remote system.  
 */
public class FSUpload extends FSUIOperation {
	// The source files to be uploaded.
	String[] sourceFiles;
	// The target folder to which these files are moved to.
	FSTreeNode targetFolder;
	// The callback invoked after uploading.
	ICallback callback;
	
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
			CacheManager.getInstance().uploadFiles(monitor, files, urls);
			if (monitor.isCanceled())
				throw new InterruptedException();
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
}
