/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.views.monitor.console;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.tcf.ui.console.AbstractConsole;
import org.eclipse.tcf.te.tcf.ui.views.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.ui.views.help.IContextHelpIds;
import org.eclipse.tcf.te.tcf.ui.views.internal.ImageConsts;
import org.eclipse.tcf.te.tcf.ui.views.nls.Messages;

/**
 * Communication monitor console implementation.
 */
public class Console extends AbstractConsole {

	/**
     * Constructor.
     *
	 * @param peer The peer. Must not be <code>null</code>.
     */
    public Console(final IPeer peer) {
		super(Messages.Monitor_Console_name, UIPlugin.getImageDescriptor(ImageConsts.MONITOR_CONSOLE));

		Assert.isNotNull(peer);

		// Determine name and id of the peer
		final AtomicReference<String> name = new AtomicReference<String>();
		final AtomicReference<String> id = new AtomicReference<String>();

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				name.set(peer.getName());
				id.set(peer.getID());
			}
		};
		if (Protocol.isDispatchThread()) runnable.run();
		else Protocol.invokeAndWait(runnable);

		setName(NLS.bind(Messages.Monitor_Console_name_with_peer, name.get(), id.get()));
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.AbstractConsole#getHelpContextId()
	 */
    @Override
	public String getHelpContextId() {
    	return IContextHelpIds.MONITOR_CONSOLE;
    }
}
