/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * William Chen (Wind River)- [345387] Open the remote files with a proper editor
 * William Chen (Wind River)- [345552] Edit the remote files with a proper editor
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.DecimalFormat;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.dialogs.TimeTriggeredProgressMonitorDialog;
import org.eclipse.tcf.te.tcf.filesystem.interfaces.IConfirmCallback;
import org.eclipse.tcf.te.tcf.filesystem.internal.url.TcfURLConnection;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.nls.Messages;
import org.eclipse.ui.PlatformUI;

/**
 * The local file system cache used to manage the temporary files downloaded
 * from a remote file system.
 */
public class CacheManager {
	public static final char PATH_ESCAPE_CHAR = '$';

	// The default chunk size of the buffer used during downloading files.
	private static final int DEFAULT_CHUNK_SIZE = 5 * 1024;

	// The formatter used to format the size displayed while downloading.
	private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("#,##0.##"); //$NON-NLS-1$

	// The singleton instance.
	private static volatile CacheManager instance;

	/**
	 * Get the singleton cache manager.
	 *
	 * @return The singleton cache manager.
	 */
	public static CacheManager getInstance() {
		if (instance == null) {
			instance = new CacheManager();
		}
		return instance;
	}

	/**
	 * Create a cache manager.
	 */
	private CacheManager() {
	}

	/**
	 * Get the local path of a node's cached file.
	 * <p>
	 * The preferred location is within the plugin's state location, in
	 * example <code>&lt;state location&gt;agent_<hashcode_of_peerId>/remote/path/to/the/file...</code>.
	 * <p>
	 * If the plug-in is loaded in a RCP workspace-less environment, the
	 * fall back strategy is to use the users home directory.
	 *
	 * @param node
	 *            The file/folder node.
	 * @return The local path of the node's cached file.
	 */
	public IPath getCachePath(FSTreeNode node) {
        File location = getCacheRoot();
		String agentId = node.peerNode.getPeerId();
		// Use Math.abs to avoid negative hash value.
		String agent = agentId.replace(':', PATH_ESCAPE_CHAR);
		IPath agentDir = new Path(location.getAbsolutePath()).append(agent);
		File agentDirFile = agentDir.toFile();
		mkdirChecked(agentDirFile);
		return appendNodePath(agentDir, node);
	}

	/**
	 * Check and make a directory if it does not exist. Record
	 * the failure message if making fails.
	 *  
	 * @param file The file to be deleted.
	 */
	void mkdirChecked(final File dir) {
		if(!dir.exists()) {
			SafeRunner.run(new SafeRunnable(){
				@Override
                public void run() throws Exception {
					if (!dir.mkdir()) {
						throw new Exception(NLS.bind(Messages.CacheManager_MkdirFailed, dir.getAbsolutePath()));
					}
                }});
		}
	}
	
	/**
	 * Check if the file exists and delete if it does. Record
	 * the failure message if deleting fails.
	 * 
	 * @param file The file to be deleted.
	 */
	void deleteFileChecked(final File file) {
		if (file.exists()) {
			SafeRunner.run(new SafeRunnable(){
				@Override
                public void run() throws Exception {
					if (!file.delete()) {
						throw new Exception(NLS.bind(Messages.FSOperation_DeletingFileFailed, file.getAbsolutePath()));
					}
                }});
		}
	}
	
	/**
	 * Check if the file exists and set its last modified time if it does. Record
	 * the failure message if it fails.
	 * 
	 * @param file The file to be set.
	 * @param lastModified the last modified time.
	 */
	void setLastModifiedChecked(final File file, final long lastModified) {
		if (file.exists()) {
			SafeRunner.run(new SafeRunnable(){
				@Override
                public void run() throws Exception {
					if (!file.setLastModified(lastModified)) {
						throw new Exception(NLS.bind(Messages.CacheManager_SetLastModifiedFailed, file.getAbsolutePath()));
					}
                }});
		}
	}
	
	/**
	 * Check if the file exists and set its read-only attribute if it does. Record
	 * the failure message if it fails.
	 * 
	 * @param file The file to be set.
	 */
	void setReadOnlyChecked(final File file) {
		if (file.exists()) {
			SafeRunner.run(new SafeRunnable(){
				@Override
                public void run() throws Exception {
					if (!file.setReadOnly()) {
						throw new Exception(NLS.bind(Messages.CacheManager_SetReadOnlyFailed, file.getAbsolutePath()));
					}
                }});
		}
	}
	
	/**
	 * Get the local file of the specified node.
	 *
	 * <p>
	 * The preferred location is within the plugin's state location, in
	 * example <code>&lt;state location&gt;agent_<hashcode_of_peerId>/remote/path/to/the/file...</code>.
	 * <p>
	 * If the plug-in is loaded in a RCP workspace-less environment, the
	 * fall back strategy is to use the users home directory.
	 *
	 * @param node
	 *            The file/folder node.
	 * @return The file object of the node's local cache.
	 */
	public File getCacheFile(FSTreeNode node){
		return getCachePath(node).toFile();
	}

	/**
	 * Get the cache file system's root directory on the local host's
	 * file system.
	 *
	 * @return The root folder's location of the cache file system.
	 */
	public File getCacheRoot() {
		File location;
        try {
        	location = UIPlugin.getDefault().getStateLocation().toFile();
        }catch (IllegalStateException e) {
            // An RCP workspace-less environment (-data @none)
        	location = new File(System.getProperty("user.home"), ".tcf"); //$NON-NLS-1$ //$NON-NLS-2$
        	location = new File(location, "fs"); //$NON-NLS-1$
        }

        // Create the location if it not exist
		mkdirChecked(location);
		return location;
	}

	/**
	 * Append the path with the specified node's context path.
	 *
	 * @param path
	 *            The path to be appended.
	 * @param node
	 *            The file/folder node.
	 * @return The path to the node.
	 */
	private IPath appendNodePath(IPath path, FSTreeNode node) {
		if (!node.isRoot() && node.parent!=null) {
			path = appendNodePath(path, node.parent);
			return appendPathSegment(node, path, node.name);
		}
		if (node.isWindowsNode()) {
			String name = node.name;
			name = name.substring(0, name.length()-1);
			name = name.replace(':', PATH_ESCAPE_CHAR);
			return appendPathSegment(node, path, name);
		}
		return path;
	}

	/**
	 * Append the path with the segment "name". Create a directory
	 * if the node is a directory which does not yet exist.
	 *
	 * @param node The file/folder node.
	 * @param path The path to appended.
	 * @param name The segment's name.
	 * @return The path with the segment "name" appended.
	 */
	private IPath appendPathSegment(FSTreeNode node, IPath path, String name) {
		IPath newPath = path.append(name);
		File newFile = newPath.toFile();
		if (node.isDirectory()) {
			mkdirChecked(newFile);
		}
		return newPath;
	}

	/**
	 * Download the data of the file from the remote file system.
	 *	Must be called within a UI thread.
	 * @param node
	 *            The file node.
	 *
	 * @return true if it is successful, false there're errors or it is
	 *         canceled.
	 */
	public boolean download(final FSTreeNode node) {
		Assert.isNotNull(Display.getCurrent());
		IRunnableWithProgress runnable = getDownloadRunnable(node);
		Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		TimeTriggeredProgressMonitorDialog dialog = new TimeTriggeredProgressMonitorDialog(
				parent, 250);
		dialog.setCancelable(true);
		File file = getCachePath(node).toFile();
		try {
			dialog.run(true, true, runnable);
			return true;
		} catch (InvocationTargetException e) {
			// Something's gone wrong. Roll back the downloading and display the
			// error.
			deleteFileChecked(file);
			PersistenceManager.getInstance().removeBaseTimestamp(node.getLocationURI());
			displayError(parent, e);
		} catch (InterruptedException e) {
			// It is canceled. Just roll back the downloading result.
			deleteFileChecked(file);
			PersistenceManager.getInstance().removeBaseTimestamp(node.getLocationURI());
		}
		return false;
	}

	public IRunnableWithProgress getDownloadRunnable(final FSTreeNode node) {
	    return new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask(NLS.bind(Messages.CacheManager_DowloadingFile, node.name), 100);
				OutputStream output = null;
				try {
					// Write the data to its local cache file.
					File file = getCachePath(node).toFile();
					if(file.exists() && !file.canWrite()){
						// If the file exists and is read-only, delete it.
						deleteFileChecked(file);
					}
					output = new BufferedOutputStream(new FileOutputStream(file));
					download2OutputStream(node, output, monitor);
					if (monitor.isCanceled())
						throw new InterruptedException();
				} catch (IOException e) {
					throw new InvocationTargetException(e);
				} finally {
					if (output != null) {
						try {
							output.close();
						} catch (Exception e) {
						}
					}
					if(!monitor.isCanceled()){
						SafeRunner.run(new SafeRunnable() {
							@Override
		                    public void handleException(Throwable e) {
								// Ignore exception
		                    }
							@Override
							public void run() throws Exception {
								File file = getCachePath(node).toFile();
								if (file.exists()) {
									// If downloading is successful, update the attributes of the file and
									// set the last modified time to that of its corresponding file.
									PersistenceManager.getInstance().setBaseTimestamp(node.getLocationURI(), node.attr.mtime);
									setLastModifiedChecked(file, node.attr.mtime);
									if (!node.isWritable()) setReadOnlyChecked(file);
									StateManager.getInstance().refreshState(node);
								}
							}
						});
					}
					monitor.done();
				}
			}
		};
    }

	/**
	 * Upload the local files to the remote file system.
	 * Must be called within UI thread.
	 * @param nodes
	 *            The files' location. Not null.
	 *
	 * @return true if it is successful, false there're errors or it is
	 *         canceled.
	 */
	public boolean upload(final FSTreeNode[] nodes, final boolean sync) {
		Assert.isNotNull(Display.getCurrent());
		Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		try {
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						String message;
						if(nodes.length==1)
							message = NLS.bind(Messages.CacheManager_UploadSingleFile, nodes[0].name);
						else
							message = NLS.bind(Messages.CacheManager_UploadNFiles, Long.valueOf(nodes.length));
						monitor.beginTask(message, 100);
						uploadFiles(monitor, sync, nodes);
						if (monitor.isCanceled())
							throw new InterruptedException();
					} catch(IOException e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				}
			};
			TimeTriggeredProgressMonitorDialog dialog = new TimeTriggeredProgressMonitorDialog(parent, 250);
			dialog.setCancelable(true);
			dialog.run(true, true, runnable);
			return true;
		} catch (InvocationTargetException e) {
			// Something's gone wrong. Roll back the downloading and display the error.
			displayError(parent, e);
		} catch (InterruptedException e) {
			// It is canceled. Just roll back the downloading result.
		}
		return false;
	}

	/**
	 * Display the error in an error dialog.
	 *
	 * @param parentNode
	 *            the file node.
	 * @param parent
	 *            the parent shell.
	 * @param e
	 *            The error exception.
	 */
	private void displayError(Shell parent, InvocationTargetException e) {
		Throwable throwable = e.getTargetException() != null ? e.getTargetException() : e;
		MessageDialog.openError(parent, Messages.CacheManager_DownloadingError, throwable.getLocalizedMessage());
	}

	/**
	 * Upload the specified files using the monitor to report the progress.
	 *
	 * @param peers
	 *            The local files' peer files.
	 * @param locals
	 *            The local files to be uploaded.
	 * @param monitor
	 *            The monitor used to report the progress.
	 * @throws Exception
	 *             an Exception thrown during downloading and storing data.
	 */
	void uploadFiles(IProgressMonitor monitor, final boolean sync,  final FSTreeNode[] nodes) throws IOException {
		File[] files = new File[nodes.length];
		URL[] urls = new URL[nodes.length];
		for (int i = 0; i < nodes.length; i++) {
			files[i] = getCacheFile(nodes[i]);
			urls[i] = nodes[i].getLocationURL();
		}
		try {
			// Upload the files to the remote location by the specified URLs.
			uploadFiles(monitor, files, urls, null);
		}
		finally {
			// Once upload is successful, synchronize the modified time.
			for (int i = 0; i < nodes.length; i++) {
				final FSTreeNode node = nodes[i];
				SafeRunner.run(new SafeRunnable() {
					@Override
					public void handleException(Throwable e) {
						// Ignore exception
					}

					@Override
					public void run() throws Exception {
						PersistenceManager.getInstance().setBaseTimestamp(node.getLocationURI(), node.attr.mtime);
						if (sync) {
							File file = getCacheFile(node);
							setLastModifiedChecked(file, node.attr.mtime);
						}
						StateManager.getInstance().refreshState(node);
					}
				});
			}
		}
	}
	
	/**
	 * Upload the specified files using the monitor to report the progress.
	 *
	 * @param files  The local file objects.
	 * @param urls The remote file's URL location.
	 * @param monitor The monitor used to report the progress.
	 * @param callback confirmation callback.
	 * @throws Exception an Exception thrown during downloading and storing data.
	 */
	public void uploadFiles(IProgressMonitor monitor, File[] files, URL[] urls, IConfirmCallback callback) throws IOException {
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
		boolean yes2all = false;
		for (int i = 0; i < files.length && !monitor.isCanceled(); i++) {
			File file = files[i];
			if(callback != null && !yes2all) {
				if(callback.requires(file)) {
					int result = callback.confirms(file);
					switch(result) {
					case IConfirmCallback.YES:
						break;
					case IConfirmCallback.YES_TO_ALL:
						yes2all = true;
						break;
					case IConfirmCallback.NO:
						bytesRead += file.length();
						if (chunk_size != 0) {
							int percent = (int) bytesRead / chunk_size;
							if (percent != percentRead) { // Update the progress.
								monitor.worked(percent - percentRead);
								percentRead = percent; // Remember the percentage.
								// Report the progress.
								monitor.subTask(NLS.bind(Messages.CacheManager_UploadingProgress, new Object[]{file.getName(), formatSize(bytesRead), formatSize(file.length())}));
							}
						}
						continue;
					case IConfirmCallback.CANCEL:
						monitor.setCanceled(true);
						continue;
					}
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
	 * Download the specified file into an output stream using the monitor to report the progress.
	 *
	 * @param node
	 *            The file to be downloaded.
	 * @param output
	 * 				The output stream.
	 * @param monitor
	 *            The monitor used to report the progress.
	 * @throws IOException
	 *             an IOException thrown during downloading and storing data.
	 */
	public void download2OutputStream(FSTreeNode node, OutputStream output, IProgressMonitor monitor) throws IOException {
		InputStream input = null;
		// Open the input stream of the node using the tcf stream protocol.
		try{
			URL url = node.getLocationURL();
			InputStream in = url.openStream();
			input = new BufferedInputStream(in);
			// The buffer used to download the file.
			byte[] data = new byte[DEFAULT_CHUNK_SIZE];
			// Calculate the chunk size of one percent.
			int chunk_size = (int) node.attr.size / 100;
			// Total size displayed on the progress dialog.
			String total_size = formatSize(node.attr.size);

			int percentRead = 0;
			long bytesRead = 0;
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
						monitor.subTask(NLS.bind(Messages.CacheManager_DownloadingProgress, formatSize(bytesRead), total_size));
					}
				}
			}
		}finally{
			if (input != null) {
				try {
					input.close();
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * Use the SIZE_FORMAT to format the file's size. The rule is: 1. If the
	 * size is less than 1024 bytes, then show it as "####" bytes. 2. If the
	 * size is less than 1024 KBs, while more than 1 KB, then show it as
	 * "####.##" KBs. 3. If the size is more than 1 MB, then show it as
	 * "####.##" MBs.
	 *
	 * @param size
	 *            The file size to be displayed.
	 * @return The string representation of the size.
	 */
	private static String formatSize(long size) {
		double kbSize = size / 1024.0;
		if (kbSize < 1.0) {
			return SIZE_FORMAT.format(size) + Messages.CacheManager_Bytes;
		}
		double mbSize = kbSize / 1024.0;
		if (mbSize < 1.0)
			return SIZE_FORMAT.format(kbSize) + Messages.CacheManager_KBs;
		return SIZE_FORMAT.format(mbSize) + Messages.CacheManager_MBs;
	}
}
