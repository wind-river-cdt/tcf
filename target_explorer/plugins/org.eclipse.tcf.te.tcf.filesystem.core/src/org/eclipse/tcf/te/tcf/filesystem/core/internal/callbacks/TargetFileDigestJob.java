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
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.tcf.te.tcf.filesystem.core.activator.CorePlugin;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpStreamOp;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;

/**
 * The job that computes the digest of the cache file in the background.
 */
public class TargetFileDigestJob extends Job {
	// The digest of which is going to be computed.
	FSTreeNode node;
	
	/**
	 * Create a job to compute the digest of its target file.
	 * 
	 * @param node The file system node.
	 */
	public TargetFileDigestJob(FSTreeNode node) {
	    super("Update target digest");
	    this.node = node;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
    protected IStatus run(IProgressMonitor monitor) {
		BufferedInputStream input = null;
		try {
			URL url = node.getLocationURL();
			MessageDigest digest = MessageDigest.getInstance("MD5");
			input = new BufferedInputStream(new DigestInputStream(url.openStream(), digest));
			// The buffer used to download the file.
			byte[] data = new byte[OpStreamOp.DEFAULT_CHUNK_SIZE];
			while (input.read(data) >= 0);
			return new DigestStatus(digest.digest());
		}
		catch(Exception e) {
			IStatus status = new Status(IStatus.ERROR, CorePlugin.getUniqueIdentifier(), e.getLocalizedMessage(), e);
			return status;
		}
		finally {
			if (input != null) {
				try {input.close();} catch (Exception e) {}
			}
		}
    }
}
