/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.core.internal.operations;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DecimalFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.core.nls.Messages;

public class OpStreamOp extends Operation {
	// The formatter used to format the size displayed while downloading.
	protected static final DecimalFormat SIZE_FORMAT = new DecimalFormat("#,##0.##"); //$NON-NLS-1$
	// The default chunk size of the buffer used during downloading files.
	protected static final int DEFAULT_CHUNK_SIZE = 5 * 1024;
	
	public OpStreamOp() {
	}
	
	/**
	 * Check if the file exists and set its last modified time if it does. Record
	 * the failure message if it fails.
	 * 
	 * @param file The file to be set.
	 * @param lastModified the last modified time.
	 */
	protected void setLastModifiedChecked(final File file, final long lastModified) {
		if (file.exists()) {
			SafeRunner.run(new ISafeRunnable(){
				@Override
                public void run() throws Exception {
					if (!file.setLastModified(lastModified)) {
						throw new Exception(NLS.bind(Messages.CacheManager_SetLastModifiedFailed, file.getAbsolutePath()));
					}
                }
				@Override
                public void handleException(Throwable exception) {
					// Ignore on purpose
                }});
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
	protected void download2OutputStream(FSTreeNode node, OutputStream output, IProgressMonitor monitor) throws IOException {
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
	 * Check if the file exists and set its read-only attribute if it does. Record
	 * the failure message if it fails.
	 * 
	 * @param file The file to be set.
	 */
	protected void setReadOnlyChecked(final File file) {
		if (file.exists()) {
			SafeRunner.run(new ISafeRunnable(){
				@Override
                public void run() throws Exception {
					if (!file.setReadOnly()) {
						throw new Exception(NLS.bind(Messages.CacheManager_SetReadOnlyFailed, file.getAbsolutePath()));
					}
                }

				@Override
                public void handleException(Throwable exception) {
	                // Ignore on purpose
                }});
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
	protected String formatSize(long size) {
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
