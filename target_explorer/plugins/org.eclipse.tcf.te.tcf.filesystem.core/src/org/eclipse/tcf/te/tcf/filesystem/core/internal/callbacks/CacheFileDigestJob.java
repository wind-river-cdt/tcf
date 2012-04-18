/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.core.internal.callbacks;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpStreamOp;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.CacheManager;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.FileState;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.PersistenceManager;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;

/**
 * The job that computes the digest of the cache file in the background.
 */
public class CacheFileDigestJob extends Job {
	// The digest of which is going to be computed.
	FSTreeNode node;
	
	/**
	 * Create a job to compute the digest of its local cache file.
	 * 
	 * @param node The file system node.
	 */
	public CacheFileDigestJob(FSTreeNode node) {
		super("Update cache digest"); //$NON-NLS-1$
		this.node = node;
		final FileState filedigest = PersistenceManager.getInstance().getFileDigest(node);
		this.addJobChangeListener(new JobChangeAdapter(){
			@Override
            public void done(IJobChangeEvent event) {
				IStatus status = event.getResult();
				if(status.isOK() && status instanceof DigestStatus) {
					filedigest.updateCacheDigest(((DigestStatus) status).getDigest());
				}
            }
		});
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
    protected IStatus run(IProgressMonitor monitor) {
		final File file = CacheManager.getCacheFile(node);
		BufferedInputStream input = null;
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
			input = new BufferedInputStream(new DigestInputStream(new FileInputStream(file), digest));
			byte[] data = new byte[OpStreamOp.DEFAULT_CHUNK_SIZE];
			while (input.read(data) >= 0){}
			return new DigestStatus(digest.digest());
		}
		catch (Exception e) {}
		finally {
			if (input != null) {
				try {input.close();} catch (Exception e) {}
			}
		}
		return new DigestStatus();
    }
}
