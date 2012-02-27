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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.SafeRunnable;
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
	Map<File, FSTreeNode> parentFolders;
	
	/**
	 * Create an instance with specified files, target folder and a callback.
	 * 
	 * @param sourceFiles the source files being uploaded.
	 * @param targetFolder the target folder to upload the files to.
	 * @param callback the callback that is invoked after uploading.
	 */
	public FSUpload(String[] sourceFiles, final FSTreeNode targetFolder, ICallback callback) {
	    super(Messages.FSUpload_UploadTitle);
	    this.sourceFiles = new String[sourceFiles.length];
    	System.arraycopy(sourceFiles, 0, this.sourceFiles, 0, sourceFiles.length);
	    this.targetFolder = targetFolder;
	    this.callback = callback;
	    parentFolders = Collections.synchronizedMap(new HashMap<File, FSTreeNode>());
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
			List<File> fileList = new ArrayList<File>();
			List<URL> urlList = new ArrayList<URL>();
			prepareDirStruct(fileList, urlList);
			File[] files = fileList.toArray(new File[fileList.size()]);
			URL[] urls = urlList.toArray(new URL[urlList.size()]);
			CacheManager.getInstance().uploadFiles(monitor, files, urls, this);
		} catch (MalformedURLException e) {
			throw new InvocationTargetException(e);
		} catch (IOException e) {
			throw new InvocationTargetException(e);
		} finally {
			monitor.done();
		}
    }

	/**
	 * Prepare the directory structure on the remote target, creating necessary intermediate directories
	 * and found all files that should be uploaded. The resulting files to be uploaded should be stored
	 * to the file list. The resulting corresponding target file URLs should be stored in the url list.
	 * 
	 * @param fileList The file list to store the files that should be uploaded.
	 * @param urlList The list containing the corresponding urls.
	 */
	private void prepareDirStruct(List<File> fileList, List<URL> urlList) {
		List<File> files = new ArrayList<File>();
		for(String path: sourceFiles) {
			files.add(new File(path));
		}
		// Find the root nodes of these files.
		List<File> topFiles = getTopFiles(files);
		for(File topFile : topFiles) {
			appendFile(topFile, fileList, urlList, targetFolder);
		}
    }

	/**
	 * Append the specified file object to the file list and url list. If the file object is a file 
	 * then append it to the two lists. If the file object is a directory, then recursively
	 * add its children and grand children to the two list. During this process, the parents of
	 * these files and directories traversed should be put into the parent folders map so that
	 * it could be queried to check if it has a file/directory with a same name.
	 * 
	 * @param file The file to be added
	 * @param fileList The file list
	 * @param urlList The url list
	 * @param parent The current parent node
	 */
	private void appendFile(final File file, final List<File> fileList, final List<URL> urlList, final FSTreeNode parent)  {
		parentFolders.put(file, parent);
		if(file.isFile()) {
			SafeRunner.run(new SafeRunnable(){
				@Override
                public void run() throws Exception {
					URL folderURL = parent.getLocationURL();
					URL url = new URL(folderURL, file.getName());
					fileList.add(file);
					urlList.add(url);
                }});
		} else if(file.isDirectory()) {
			FSTreeNode node = findNode(file);
			if(node == null) {
				final AtomicReference<FSTreeNode> reference = new AtomicReference<FSTreeNode>();
				SafeRunner.run(new SafeRunnable(){
					@Override
	                public void run() throws Exception {
						FSCreateFolder create = new FSCreateFolder(parent, file.getName());
						create.run(new NullProgressMonitor());
						reference.set(create.getNode());
	                }});
				node = reference.get();
			}
			File[] children = file.listFiles();
			for(File child : children) {
				appendFile(child, fileList, urlList, node);
			}
		}
    }

	/**
	 * Get the root files of the specified files/folders in the list.
	 * 
	 * @param files The files to be checked.
	 * @return Root nodes of these files that has no parent.
	 */
	private List<File> getTopFiles(List<File>files) {
		List<File> result = new ArrayList<File>();
		for(File file : files) {
			if(!hasFileAncestor(file, files)) {
				result.add(file);
			}
		}
	    return result;
    }

	/**
	 * Check if the target file has an ancestral parent in the specified list.
	 * 
	 * @param target The target file to be checked.
	 * @param files The file list to be searched.
	 * @return true if it has an ancestral parent.
	 */
	private boolean hasFileAncestor(File target, List<File> files) {
		for(File file : files) {
			if(isFileAncestor(file, target)) {
				return true;
			}
		}
	    return false;
    }

	/**
	 * Check if the specified "file" is an ancestral parent of the "target" file.
	 * 
	 * @param file The ancestral file.
	 * @param target The target file.
	 * @return true if "file" is an ancestral parent of "target"
	 */
	private boolean isFileAncestor(File file, File target) {
		if(target == null) return false;
		File parent = target.getParentFile();
		if(file.equals(parent)) return true;
	    return isFileAncestor(file, parent);
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.interfaces.IConfirmCallback#requires(java.lang.Object)
	 */
	@Override
    public boolean requires(Object object) {
		return findNode((File) object) != null;
    }

	/**
	 *  Check if the specified file has a same-named file under its corresponding
	 *  parent folder.
	 *  
	 * @param file The file to checked.
	 * @return the node that has the same name with the file.
	 */
	private FSTreeNode findNode(File file) {
		final FSTreeNode parent = parentFolders.get(file);
		final List<FSTreeNode> targetChildren = new ArrayList<FSTreeNode>();
		SafeRunner.run(new SafeRunnable(){
			@Override
            public void run() throws Exception {
				targetChildren.addAll(getChildren(parent));
            }});
		String name = file.getName();
		for(FSTreeNode child:targetChildren) {
			if(name.equals(child.name))
				return child;
		}
	    return null;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.interfaces.IConfirmCallback#confirms(java.lang.Object)
	 */
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
