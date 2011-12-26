/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.navigator.nodes;

import org.eclipse.core.runtime.Assert;

/**
 * A node grouping discovered child peers of a proxy peer.
 * <p>
 * Instances of this class are immutable.
 */
public class PeerRedirectorGroupNode {
	public final String peerId;

	/**
     * Constructor.
     */
    public PeerRedirectorGroupNode(String peerId) {
    	Assert.isNotNull(peerId);
    	this.peerId = peerId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
    	if (obj instanceof PeerRedirectorGroupNode) {
    		return peerId.equals(((PeerRedirectorGroupNode)obj).peerId);
    	}
        return super.equals(obj);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return peerId.hashCode();
    }
}