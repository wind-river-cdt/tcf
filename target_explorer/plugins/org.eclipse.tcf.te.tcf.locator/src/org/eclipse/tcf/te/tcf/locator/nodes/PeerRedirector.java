/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.locator.nodes;

import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.core.TransientPeer;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerRedirector;

/**
 * Peer redirector implementation.
 * <p>
 * If a peer is discovery by querying the <i>remote</i> locator service of a known peer, than the
 * communication with the remotely discovered peer is channeled through the proxy peer (== parent).
 */
public class PeerRedirector extends TransientPeer implements IPeerRedirector {
	// Reference to the parent peer which is serving as proxy
	private final IPeer parent;

	/**
	 * Constructor.
	 *
	 * @param parent The parent peer. Must not be <code>null</code>.
	 * @param attrs The peer attributes of the remote discovered peer. Must not be <code>null</code>
	 *            .
	 */
	public PeerRedirector(IPeer parent, Map<String, String> attrs) {
		super(attrs);

		Assert.isNotNull(parent);
		this.parent = parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.core.TransientPeer#openChannel()
	 */
	@Override
	public IChannel openChannel() {
		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
		IChannel c = parent.openChannel();
		c.redirect(getID());
		return c;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerRedirector#getParent()
	 */
	@Override
	public IPeer getParent() {
		return parent;
	}

	/**
	 * Update the peer attributes.
	 *
	 * @param attrs The new peer attributes. Must not be <code>null</code>.
	 */
	public void updateAttributes(Map<String, String> attrs) {
		Assert.isNotNull(attrs);

		if (!attrs.equals(ro_attrs)) {
			Assert.isTrue(attrs.get(ATTR_ID).equals(rw_attrs.get(ATTR_ID)));
			rw_attrs.clear();
			rw_attrs.putAll(attrs);
		}
	}

}
