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
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IOperation;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.CacheManager;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;

/**
 * The operation to calculate the message digest of a cache file.
 */
public class OpCacheFileDigest implements IOperation {
	// The digest of which is going to be computed.
	FSTreeNode node;
	// The computing result
	byte[] digest;
	
	/**
	 * Create an operation to compute the digest of its local cache file.
	 * 
	 * @param node The file system node.
	 */
	public OpCacheFileDigest(FSTreeNode node) {
		this.node = node;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IOperation#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		File file = CacheManager.getCacheFile(node);
		BufferedInputStream input = null;
		try {
			long totalSize = file.length();
			int chunk_size = (int) totalSize / 100;
			int percentRead = 0;
			long bytesRead = 0;
			MessageDigest digest = MessageDigest.getInstance(MD_ALG);
			input = new BufferedInputStream(new DigestInputStream(new FileInputStream(file), digest));
			byte[] data = new byte[OpStreamOp.DEFAULT_CHUNK_SIZE];
			int length;
			while ((length = input.read(data)) >= 0){
				bytesRead += length;
				if (chunk_size != 0) {
					int percent = (int) bytesRead / chunk_size;
					if (percent != percentRead) { // Update the progress.
						monitor.worked(percent - percentRead);
						percentRead = percent; // Remember the percentage.
					}
				}
			}
			this.digest = digest.digest();
		}
        catch (NoSuchAlgorithmException e) {
			throw new InvocationTargetException(e);
        }
        catch (IOException e) {
			throw new InvocationTargetException(e);
        }
		finally {
			if (input != null) {
				try {input.close();} catch (Exception e) {}
			}
		}
	}
	
	/**
	 * Get the computing result.
	 * 
	 * @return The message digest of this cache file.
	 */
	public byte[] getDigest() {
		return digest;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IOperation#getName()
	 */
	@Override
	public String getName() {
		return "Update cache digest"; //$NON-NLS-1$
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
