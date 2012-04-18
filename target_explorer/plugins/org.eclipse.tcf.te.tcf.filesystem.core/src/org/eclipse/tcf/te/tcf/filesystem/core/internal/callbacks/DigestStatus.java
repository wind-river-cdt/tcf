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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tcf.te.tcf.filesystem.core.activator.CorePlugin;
/**
 * An extended status object to return digest data computed by a job. 
 */
public class DigestStatus extends Status {
	// The digest data result.
	private byte[] digest;
	
	/**
	 * Create a digest status with the specified digest data as an OK status.
	 * @param digest
	 */
	public DigestStatus(byte[] digest) {
        super(IStatus.OK, CorePlugin.getUniqueIdentifier(), "OK"); //$NON-NLS-1$
        this.digest = digest;
    }
	
	/**
	 * Create a digest status with an error status.
	 */
	public DigestStatus() {
		super(IStatus.ERROR, CorePlugin.getUniqueIdentifier(), "FAILED"); //$NON-NLS-1$
	}
	
	/**
	 * Return the digest data or null if the computing fails.
	 * 
	 * @return The digest data.
	 */
	public byte[] getDigest() {
		return digest;
	}
}
