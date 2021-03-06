/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.locator.internal;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.model.factory.AbstractFactoryDelegate2;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.internal.nodes.InvalidPeerModel;
import org.eclipse.tcf.te.tcf.locator.nodes.PeerModel;

/**
 * Locator model node factory delegate implementation.
 */
public class ModelNodeFactoryDelegate extends AbstractFactoryDelegate2 {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.interfaces.factory.IFactoryDelegate#newInstance(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
    @Override
	public <V extends IModelNode> V newInstance(Class<V> nodeInterface) {
		if (IPeerModel.class.equals(nodeInterface)) {
			return (V) new InvalidPeerModel();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.interfaces.factory.IFactoryDelegate2#newInstance(java.lang.Class, java.lang.Object[])
	 */
	@SuppressWarnings("unchecked")
    @Override
	public <V extends IModelNode> V newInstance(final Class<V> nodeInterface, final Object[] args) {
		if (args == null) return newInstance(nodeInterface);

		if (IPeerModel.class.equals(nodeInterface)) {
			// Peer model constructor has 2 arguments, ILocatorModel and IPeer
			if (args.length == 2 && args[0] instanceof ILocatorModel && args[1] instanceof IPeer) {
				final AtomicReference<V> node = new AtomicReference<V>();

				Runnable runnable = new Runnable() {
					@Override
                    public void run() {
						node.set((V) new PeerModel((ILocatorModel)args[0], (IPeer)args[1]));
					}
				};
				if (Protocol.isDispatchThread()) runnable.run();
				else Protocol.invokeAndWait(runnable);

				return node.get();
			}
		}

	    return null;
	}
}
