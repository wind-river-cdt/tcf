/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.services.contexts.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.interfaces.IDisposable;
import org.eclipse.tcf.te.tcf.services.contexts.interfaces.IContexts;

/**
 * Contexts service proxy implementation.
 */
public class ContextsProxy implements IContexts, IDisposable {
	// The peer instance the proxy is associated with
	/* default */ final IPeer peer;

	// The list of context handler delegates
	private final List<IContexts.IDelegate> delegates = new ArrayList<IContexts.IDelegate>();

    /**
	 * Constructor.
	 *
	 * @param peer The peer. Must not be <code>null</code>.
	 */
	public ContextsProxy(IPeer peer) {
		Assert.isNotNull(peer);
		this.peer = peer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.protocol.IService#getName()
	 */
	@Override
    public String getName() {
		return NAME;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.IDisposable#dispose()
	 */
	@Override
	public void dispose() {
		// Clear out the delegates list
		delegates.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.services.contexts.interfaces.IContexts#getDelegate(java.lang.String)
	 */
	@Override
	public IDelegate getDelegate(String contextID) {
		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
		Assert.isNotNull(contextID);

		// Make a snapshot of the registered delegates
		IContexts.IDelegate[] candidates = delegates.toArray(new IContexts.IDelegate[delegates.size()]);
		for (IContexts.IDelegate candidate : candidates) {
			if (candidate.canHandle(contextID)) {
				return candidate;
			}
		}

	    return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.services.contexts.interfaces.IContexts#addDelegate(org.eclipse.tcf.te.tcf.services.contexts.interfaces.IContexts.IDelegate)
	 */
	@Override
	public void addDelegate(IDelegate delegate) {
		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
		Assert.isNotNull(delegate);
		if (!delegates.contains(delegate)) delegates.add(delegate);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.services.contexts.interfaces.IContexts#removeDelegate(org.eclipse.tcf.te.tcf.services.contexts.interfaces.IContexts.IDelegate)
	 */
	@Override
	public void removeDelegate(IDelegate delegate) {
		Assert.isTrue(Protocol.isDispatchThread(), "Illegal Thread Access"); //$NON-NLS-1$
		Assert.isNotNull(delegate);
		delegates.remove(delegate);
	}
}
