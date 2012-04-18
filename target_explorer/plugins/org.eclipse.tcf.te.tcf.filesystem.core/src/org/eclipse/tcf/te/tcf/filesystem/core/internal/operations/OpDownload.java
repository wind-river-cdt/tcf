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
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.url.TcfURLConnection;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.CacheManager;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.core.nls.Messages;

/**
 * Download multiple files from local system to a remote system.  
 */
public class OpDownload extends OpStreamOp {
	// The destination files to be downloaded to.
	protected File[] dstFiles;
	// The source nodes to be downloaded from.
	protected FSTreeNode[] srcNodes;
	
	/**
	 * Create a download operation to download a file node
	 * to a local file.
	 *  
	 * @param dstFile The local file to be downloaded to.
	 * @param srcNode The source node to be downloaded from.
	 */
	public OpDownload(File dstFile, FSTreeNode srcNode) {
		this(new File[]{dstFile}, new FSTreeNode[]{srcNode});
	}

	/**
	 * Create a download operation to download file nodes
	 * to local files.
	 *  
	 * @param dstFiles The local files to be downloaded to.
	 * @param srcNodes The source nodes to be downloaded from.
	 */
	public OpDownload(File[] dstFiles, FSTreeNode[] srcNodes) {
		this.dstFiles = dstFiles;
		this.srcNodes = srcNodes;
	}
	
	/**
	 * Create a download operation to download specified nodes
	 * to its local cache files.
	 * 
	 * @param srcNodes The source file nodes to be downloaded.
	 */
	public OpDownload(FSTreeNode... srcNodes) {
		this.srcNodes = srcNodes;
		this.dstFiles = new File[srcNodes.length];
		for (int i = 0; i < srcNodes.length; i++) {
			this.dstFiles[i] = CacheManager.getCacheFile(srcNodes[i]);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.Operation#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		super.run(monitor);
		try {
			downloadFiles(dstFiles, srcNodes);
			if(monitor.isCanceled()) throw new InterruptedException();
		} catch (MalformedURLException e) {
			throw new InvocationTargetException(e);
		} catch (IOException e) {
			throw new InvocationTargetException(e);
		} finally {
			monitor.done();
		}
    }
	
	/**
	 * Download the specified file list to the specified locations, reporting the progress 
	 * using the specified monitor.
	 * 
	 * @param dstFiles The file list to be downloaded to.
	 * @param srcNodes The node list to be downloaded from.
	 * @param monitor The monitor that reports progress.
	 * @throws IOException The exception reported during downloading.
	 */
	private void downloadFiles(File[] dstFiles, FSTreeNode[] srcNodes) throws IOException {
		// The buffer used to download the file.
		byte[] data = new byte[DEFAULT_CHUNK_SIZE];
		// Calculate the total size.
		long totalSize = 0;
		for (FSTreeNode node:srcNodes) {
			totalSize += node.attr == null ? 0L : node.attr.size;
		}
		// Calculate the chunk size of one percent.
		int chunk_size = (int) totalSize / 100;
		// The current reading percentage.
		int percentRead = 0;
		// The current length of read bytes.
		long bytesRead = 0;
		for (int i = 0; i < srcNodes.length && !monitor.isCanceled(); i++) {
			FSTreeNode node = srcNodes[i];
			long size = node.attr == null ? 0L : node.attr.size;
			MessageDigest digest = null;
			BufferedInputStream input = null;
			BufferedOutputStream output = null;
			try {
				URL url = node.getLocationURL();
				TcfURLConnection connection = (TcfURLConnection) url.openConnection();
				try {
					digest = MessageDigest.getInstance(MD_ALG);
					input = new BufferedInputStream(new DigestInputStream(connection.getInputStream(), digest));
				}
				catch (NoSuchAlgorithmException e) {
					input = new BufferedInputStream(connection.getInputStream());
				}
				output = new BufferedOutputStream(new FileOutputStream(dstFiles[i]));

				// Total size displayed on the progress dialog.
				String fileLength = formatSize(size);
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
							monitor.subTask(NLS.bind(Messages.OpDownload_Downloading, new Object[]{node.name, formatSize(bytesRead), fileLength}));
						}
					}
				}
			}
			finally {
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
				if(digest != null) {
					updateNodeDigest(node, digest.digest());
				}
			}
		}
	}

	/**
	 * Update the node's digest using the digest data.
	 * 
	 * @param node The node whose digest should updated.
	 * @param digest The digest data.
	 */
	protected void updateNodeDigest(FSTreeNode node, byte[] digest) {
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IOperation#getName()
	 */
	@Override
    public String getName() {
		String message;
		if(dstFiles.length==1)
			message = NLS.bind(Messages.OpDownload_DownloadingSingleFile, dstFiles[0].getName());
		else
			message = NLS.bind(Messages.OpDownload_DownloadingMultipleFiles, Long.valueOf(dstFiles.length));
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
