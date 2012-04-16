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

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.CacheManager;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.FileState;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.PersistenceManager;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;

/**
 * The operation class that updates the local cache to target file systems. 
 */
public class OpCacheUpdate extends OpDownload {

	/**
	 * Create an instance of an OpCacheUpdate which
	 * updates the specified nodes.
	 * 
	 * @param nodes The nodes to be updated.
	 */
	public OpCacheUpdate(FSTreeNode... nodes) {
		super(nodes);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpDownload#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		for (FSTreeNode node : srcNodes) {
			// Write the data to its local cache file.
			File file = CacheManager.getCachePath(node).toFile();
			if (file.exists() && !file.canWrite()) {
				// If the file exists and is read-only, delete it.
				deleteFileChecked(file);
			}
		}
		try {
			super.run(monitor);
		} finally {
			if (!monitor.isCanceled()) {
				for (FSTreeNode node : srcNodes) {
					File file = CacheManager.getCachePath(node).toFile();
					if (file.exists()) {
						// If downloading is successful, update the attributes of the file and
						// set the last modified time to that of its corresponding file.
						if (!node.isWritable()) setReadOnlyChecked(file);
					}
				}
			}
			monitor.done();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpDownload#updateNodeDigest(org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode, byte[])
	 */
	@Override
	protected void updateNodeDigest(FSTreeNode node, byte[] digest) {
		FileState fdigest = PersistenceManager.getInstance().getFileDigest(node);
		fdigest.reset(digest);
    }
}
