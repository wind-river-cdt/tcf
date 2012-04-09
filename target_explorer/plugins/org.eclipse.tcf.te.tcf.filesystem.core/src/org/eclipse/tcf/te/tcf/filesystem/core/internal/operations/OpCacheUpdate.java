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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.CacheManager;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.PersistenceManager;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.StateManager;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.core.nls.Messages;

public class OpCacheUpdate extends OpStreamOp {
	
	protected FSTreeNode node;

	public OpCacheUpdate(FSTreeNode node) {
		this.node = node;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		monitor.beginTask(NLS.bind(Messages.CacheManager_DowloadingFile, node.name), 100);
		OutputStream output = null;
		try {
			// Write the data to its local cache file.
			File file = CacheManager.getInstance().getCachePath(node).toFile();
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
				SafeRunner.run(new ISafeRunnable() {
					@Override
                    public void handleException(Throwable e) {
						// Ignore exception
                    }
					@Override
					public void run() throws Exception {
						File file = CacheManager.getInstance().getCachePath(node).toFile();
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
}
