/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.peers;

import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.core.TransientPeer;
import org.eclipse.tcf.te.runtime.events.ChangeEvent;
import org.eclipse.tcf.te.runtime.events.EventManager;

/**
 * Peer implementation.
 */
public class Peer extends TransientPeer {

	/**
	 * Constructor.
	 *
	 * @param attrs The peer attributes. Must not be <code>null</code>
	 */
	public Peer(Map<String, String> attrs) {
		super(attrs);
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
			EventManager.getInstance().fireEvent(new ChangeEvent(this, "updateAttributes", ro_attrs, rw_attrs)); //$NON-NLS-1$
		}
	}
}
