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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.url.TcfURLConnection;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.CacheManager;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.PersistenceManager;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.StateManager;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.core.nls.Messages;

public class OpCacheCommit extends OpStreamOp {
	protected FSTreeNode[] nodes;
	private boolean sync;
	public OpCacheCommit(FSTreeNode[] nodes, boolean sync) {
		this.nodes = nodes;
		this.sync = sync;
    }
	
	@Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		File[] files = new File[nodes.length];
		URL[] urls = new URL[nodes.length];
		for (int i = 0; i < nodes.length; i++) {
			files[i] = CacheManager.getInstance().getCacheFile(nodes[i]);
			urls[i] = nodes[i].getLocationURL();
		}
		try {
			// Upload the files to the remote location by the specified URLs.
			uploadFiles(monitor, files, urls);
		}
		catch(IOException e) {
			throw new InvocationTargetException(e);
		} finally {
			// Once upload is successful, synchronize the modified time.
			for (int i = 0; i < nodes.length; i++) {
				final FSTreeNode node = nodes[i];
				SafeRunner.run(new ISafeRunnable() {
					@Override
					public void handleException(Throwable e) {
						// Ignore exception
					}

					@Override
					public void run() throws Exception {
						PersistenceManager.getInstance().setBaseTimestamp(node.getLocationURI(), node.attr.mtime);
						if (sync) {
							File file = CacheManager.getInstance().getCacheFile(node);
							setLastModifiedChecked(file, node.attr.mtime);
						}
						StateManager.getInstance().refreshState(node);
					}
				});
			}
		}
    }

	public void uploadFiles(IProgressMonitor monitor, File[] files, URL[] urls) throws IOException {
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

}
