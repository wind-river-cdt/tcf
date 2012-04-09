/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.core.internal.operations;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IConfirmCallback;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.url.TcfURLConnection;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.CacheManager;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.core.nls.Messages;

/**
 * Upload multiple files from local system to a remote system.  
 */
public class OpUpload extends OpStreamOp {
	// The source files to be uploaded.
	File[] srcFiles;
	// The destination URLs to be uploaded to.
	URL[] dstURLs;
	// The confirm callback
	IConfirmCallback confirmCallback;
	// The parent folder map to search files that have same names.
	Map<File, FSTreeNode> parentFolders;
	
	/**
	 * Constructor.
	 * 
	 * @param srcFile The source file to be uploaded.
	 * @param dstURL The destination URL.
	 */
	public OpUpload(File srcFile, URL dstURL) {
		this(new File[]{srcFile}, new URL[]{dstURL});
	}
	
	/**
	 * Constructor.
	 * 
	 * @param srcFiles The source files to be uploaded.
	 * @param dstURLs The destination URLs.
	 */
	public OpUpload(File[] srcFiles, URL[] dstURLs) {
		this(srcFiles, dstURLs, null);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param sourceFiles The source files in the native file system to be uploaded.
	 * @param targetFolder The taret parent folder to upload these files to.
	 */
	public OpUpload(String[]sourceFiles, FSTreeNode targetFolder) {
		this(sourceFiles, targetFolder, null);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param sourceFiles The source files in the native file system to be uploaded.
	 * @param targetFolder The target parent folder to upload these files to.
	 * @param confirmCallback the confirmation callback to confirm overwriting.
	 */
	public OpUpload(File[] srcFiles, URL[] dstURLs, IConfirmCallback confirmCallback) {
		this.srcFiles = srcFiles;
		this.dstURLs = dstURLs;
	    this.confirmCallback = confirmCallback;
	}
	
	/**
	 * Constructor that upload the local cache files of the specified nodes.
	 * 
	 * @param nodes The nodes to be uploaded.
	 */
	public OpUpload(FSTreeNode[] nodes) {
		srcFiles = new File[nodes.length];
		dstURLs = new URL[nodes.length];
		for (int i = 0; i < nodes.length; i++) {
			srcFiles[i] = CacheManager.getInstance().getCacheFile(nodes[i]);
			dstURLs[i] = nodes[i].getLocationURL();
		}
	}
	
	/**
	 * Create an instance with specified files, target folder and a callback.
	 * 
	 * @param sourceFiles the source files being uploaded.
	 * @param targetFolder the target folder to upload the files to.
	 * @param callback the callback that is invoked after uploading.
	 */
	public OpUpload(String[] sourceFiles, FSTreeNode targetFolder, IConfirmCallback confirmCallback) {
	    this.confirmCallback = confirmCallback;
		List<File> fileList = new ArrayList<File>();
		List<URL> urlList = new ArrayList<URL>();
		prepareDirStruct(sourceFiles, fileList, urlList, targetFolder);
		srcFiles = fileList.toArray(new File[fileList.size()]);
		dstURLs = urlList.toArray(new URL[urlList.size()]);
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSOperation#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		super.run(monitor);
		try {
			uploadFiles(srcFiles, dstURLs);
			if(monitor.isCanceled()) throw new InterruptedException();
		} catch (MalformedURLException e) {
			throw new InvocationTargetException(e);
		} catch (IOException e) {
			throw new InvocationTargetException(e);
		} finally {
			monitor.done();
		}
    }
	
	private boolean requireConfirmation(File file) {
		return parentFolders != null && confirmCallback != null && !yes2All && confirmCallback.requires(file) && findNode(file) != null;
	}
	
	/**
	 * Upload the specified file list to the specified locations, reporting the progress 
	 * using the specified monitor.
	 * 
	 * @param files The file list to be uploaded.
	 * @param urls The 
	 * @param monitor
	 * @throws IOException
	 */
	private void uploadFiles(File[] files, URL[] urls) throws IOException {
		BufferedInputStream input = null;
		BufferedOutputStream output = null;
		// The buffer used to download the file.
		byte[] data = new byte[DEFAULT_CHUNK_SIZE];
		// Calculate the total size.
		long totalSize = 0;
		for (File file:files) {
			totalSize += file.length();
		}
		// Calculate the chunk size of one percent.
		int chunk_size = (int) totalSize / 100;
		// The current reading percentage.
		int percentRead = 0;
		// The current length of read bytes.
		long bytesRead = 0;
		for (int i = 0; i < files.length && !monitor.isCanceled(); i++) {
			File file = files[i];
			if (requireConfirmation(file)) {
				int result = confirmCallback.confirms(file);
				switch (result) {
				case IConfirmCallback.YES:
					break;
				case IConfirmCallback.YES_TO_ALL:
					yes2All = true;
					break;
				case IConfirmCallback.NO:
					bytesRead += file.length();
					if (chunk_size != 0) {
						int percent = (int) bytesRead / chunk_size;
						if (percent != percentRead) { // Update the progress.
							monitor.worked(percent - percentRead);
							percentRead = percent; // Remember the percentage.
							// Report the progress.
							monitor.subTask(NLS
							                .bind(Messages.CacheManager_UploadingProgress, new Object[] { file
							                                .getName(), formatSize(bytesRead), formatSize(file
							                                .length()) }));
						}
					}
					continue;
				case IConfirmCallback.CANCEL:
					monitor.setCanceled(true);
					continue;
				}
			}
			try {
				URL url = urls[i];
				TcfURLConnection connection = (TcfURLConnection) url.openConnection();
				connection.setDoInput(false);
				connection.setDoOutput(true);
				input = new BufferedInputStream(new FileInputStream(file));
				output = new BufferedOutputStream(connection.getOutputStream());

				// Total size displayed on the progress dialog.
				String fileLength = formatSize(file.length());
				int length;
				while ((length = input.read(data)) >= 0 && !monitor.isCanceled()) {
					output.write(data, 0, length);
					output.flush();
					bytesRead += length;
					if (chunk_size != 0) {
						int percent = (int) bytesRead / chunk_size;
						if (percent != percentRead) { // Update the progress.
							monitor.worked(percent - percentRead);
							percentRead = percent; // Remember the percentage.
							// Report the progress.
							monitor.subTask(NLS.bind(Messages.CacheManager_UploadingProgress, new Object[]{file.getName(), formatSize(bytesRead), fileLength}));
						}
					}
				}
			} finally {
				if (output != null) {
					try {
						output.close();
					} catch (Exception e) {
					}
				}
				if (input != null) {
					try {
						input.close();
					} catch (Exception e) {
					}
				}
			}
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
	private void prepareDirStruct(String[] sourceFiles, List<File> fileList, List<URL> urlList, FSTreeNode targetFolder) {
		parentFolders = Collections.synchronizedMap(new HashMap<File, FSTreeNode>());
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
			SafeRunner.run(new ISafeRunnable(){
				@Override
                public void run() throws Exception {
					URL folderURL = parent.getLocationURL();
					URL url = new URL(folderURL, file.getName());
					fileList.add(file);
					urlList.add(url);
                }
				@Override
                public void handleException(Throwable exception) {
					// Ignore on purpose
                }});
		} else if(file.isDirectory()) {
			FSTreeNode node = findNode(file);
			if(node == null) {
				final AtomicReference<FSTreeNode> reference = new AtomicReference<FSTreeNode>();
				SafeRunner.run(new ISafeRunnable(){
					@Override
	                public void run() throws Exception {
						OpCreateFolder create = new OpCreateFolder(parent, file.getName());
						create.run(new NullProgressMonitor());
						reference.set(create.getNode());
	                }
					@Override
                    public void handleException(Throwable exception) {
						// Ignore on purpose
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

	/**
	 *  Check if the specified file has a same-named file under its corresponding
	 *  parent folder.
	 *  
	 * @param file The file to checked.
	 * @return the node that has the same name with the file.
	 */
	private FSTreeNode findNode(File file) {
		final FSTreeNode parent = parentFolders.get(file);
		if (parent != null) {
			final List<FSTreeNode> targetChildren = new ArrayList<FSTreeNode>();
			SafeRunner.run(new ISafeRunnable() {
				@Override
				public void run() throws Exception {
					targetChildren.addAll(getChildren(parent));
				}

				@Override
				public void handleException(Throwable exception) {
					// Ignore on purpose
				}
			});
			String name = file.getName();
			for (FSTreeNode child : targetChildren) {
				if (name.equals(child.name)) return child;
			}
		}
	    return null;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IOperation#getName()
	 */
	@Override
    public String getName() {
		String message;
		if(srcFiles.length==1)
			message = NLS.bind(Messages.CacheManager_UploadSingleFile, srcFiles[0].getName());
		else
			message = NLS.bind(Messages.CacheManager_UploadNFiles, Long.valueOf(srcFiles.length));
		return message;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IOperation#getTotalWork()
	 */
	@Override
    public int getTotalWork() {
	    return 100;
    }
}
