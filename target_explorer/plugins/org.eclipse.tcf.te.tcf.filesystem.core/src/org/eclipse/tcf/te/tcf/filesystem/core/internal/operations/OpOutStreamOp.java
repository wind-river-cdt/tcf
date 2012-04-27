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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.core.nls.Messages;

/**
 * The operation class that download a file node to a specified output stream. 
 */
public class OpOutStreamOp extends OpStreamOp {
	// The output stream to write to.
	private OutputStream output;
	// The file node to download from.
	private FSTreeNode node;
	
	/**
	 * Create an operation instance to download the specified file to the specified
	 * output stream.
	 * 
	 * @param node The file to be downloaded.
	 * @param output The output stream to write to.
	 */
	public OpOutStreamOp(FSTreeNode node, OutputStream output) {
		this.node = node;
		this.output = output;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.Operation#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		super.run(monitor);
		InputStream input = null;
		// Open the input stream of the node using the tcf stream protocol.
		try {
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
						monitor.subTask(NLS.bind(Messages.OpOutStreamOp_DownloadingProgress, formatSize(bytesRead), total_size));
					}
				}
			}
			if(monitor.isCanceled()) throw new InterruptedException();
		}
		catch (IOException e) {
			throw new InvocationTargetException(e);
		}
		finally {
			if (input != null) {
				try {
					input.close();
				}
				catch (Exception e) {
				}
			}
			monitor.done();
		}
	}
}
