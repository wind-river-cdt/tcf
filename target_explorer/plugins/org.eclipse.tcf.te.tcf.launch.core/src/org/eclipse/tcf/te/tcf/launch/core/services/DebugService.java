/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.launch.core.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.tcf.internal.debug.launch.TCFLaunchDelegate;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.concurrent.util.ExecutorsUtil;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.services.AbstractService;
import org.eclipse.tcf.te.runtime.services.interfaces.IDebugService;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;

/**
 * Debug service implementations for TCF contexts.
 */
@SuppressWarnings("restriction")
public class DebugService extends AbstractService implements IDebugService {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.services.interfaces.IDebugService#attach(java.lang.Object, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
	 */
	@Override
	public void attach(final Object context, final IPropertiesContainer data, final ICallback callback) {
		Assert.isNotNull(context);
		Assert.isNotNull(data);
		Assert.isNotNull(callback);

		// Get the peer to attach
		IPeer peer = null;
		if (context instanceof IPeerModel) {
			peer = ((IPeerModel)context).getPeer();
		}
		else if (context instanceof IPeer) {
			peer = (IPeer)context;
		}

		if (peer != null) {
			// Make sure that the attach is executed in a non TCF dispatch thread
			if (!Protocol.isDispatchThread()) {
				attachPeer(peer, data, callback);
			} else {
				final IPeer finPeer = peer;
				ExecutorsUtil.execute(new Runnable() {
					@Override
					public void run() {
						attachPeer(finPeer, data, callback);
					}
				});
			}
		}
		else {
			callback.done(this, Status.OK_STATUS);
		}
	}

	/**
	 * Attach to the given peer.
	 *
	 * @param peer The peer. Must not be <code>null</code>.
	 * @param data The data properties to parameterize the attach. Must not be <code>null</code>.
	 * @param callback The callback to invoke once the operation completed. Must not be <code>null</code>.
	 */
	protected void attachPeer(IPeer peer, IPropertiesContainer data, ICallback callback) {
		Assert.isNotNull(peer);
		Assert.isNotNull(data);
		Assert.isNotNull(callback);

		// The launch configuration to execute
		ILaunchConfiguration lc = null;
		ILaunch l = null;

		// Look for existing launch configurations
		ILaunchConfiguration[] configs = findExistingConfigs(peer);
		if (configs.length > 0) {
			// Check if any of the matching launches has an active launch
			ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
			for (ILaunchConfiguration config : configs) {
				for (ILaunch launch : launches) {
					if (config.equals(launch.getLaunchConfiguration())) {
						lc = config;
						l = launch;
						break;
					}
				}
				if (lc != null) break;
			}
			// If there is none with an active launch, take the first one
			if (lc == null) lc = configs[0];
		} else {
			// No existing launch configuration -> create a new one
			lc = createNewConfig(peer);
		}

		if (lc != null && l == null) {
			try {
				l = lc.launch(ILaunchManager.DEBUG_MODE, new NullProgressMonitor(), false, true);
				callback.setProperty("launch", l); //$NON-NLS-1$
				callback.done(this, Status.OK_STATUS);
			} catch (CoreException e) {
				callback.done(this, e.getStatus());
			}
		} else {
			callback.setProperty("launch", l); //$NON-NLS-1$
			callback.done(this, Status.OK_STATUS);
		}
	}

	/**
	 * Creates a new launch configuration for the given peer.
	 *
	 * @param peer The peer. Must not be <code>null</code>.
	 * @return The new launch configuration instance.
	 */
	protected ILaunchConfiguration createNewConfig(final IPeer peer) {
		Assert.isNotNull(peer);

		// The result
		ILaunchConfiguration lc = null;

		// Determine the ID of the given peer
		final AtomicReference<String> peerId = new AtomicReference<String>();
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				peerId.set(peer.getID());
			}
		};
		if (Protocol.isDispatchThread()) runnable.run();
		else Protocol.invokeAndWait(runnable);

		// Get the launch manager
		ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
		// Get the "Target Communication Framework" launch configuration type
		ILaunchConfigurationType lct = lm.getLaunchConfigurationType("org.eclipse.tcf.debug.LaunchConfigurationType"); //$NON-NLS-1$
		if (lct != null) {
			try {
				// Calculate the launch configuration name based on the peer id
				String name = lm.generateLaunchConfigurationName(peerId.get());
				// Create a new launch configuration working copy
				ILaunchConfigurationWorkingCopy wc = lct.newInstance(null, name);
				// And fill in the launch configuration attributes
				wc.setAttribute(TCFLaunchDelegate.ATTR_PEER_ID, peerId.get());
				wc.setAttribute(TCFLaunchDelegate.ATTR_RUN_LOCAL_AGENT, false);
				wc.setAttribute(TCFLaunchDelegate.ATTR_USE_LOCAL_AGENT, false);
				// Save the working copy
				lc = wc.doSave();
			} catch (CoreException e) {
				if (Platform.inDebugMode()) e.printStackTrace();
			}
		}


		return lc;
	}

	/**
	 * Find existing launch configurations created for the given peer.
	 *
	 * @param peer The peer. Must not be <code>null</code>.
	 * @return The list of existing launch configurations or an empty list.
	 */
	protected ILaunchConfiguration[] findExistingConfigs(final IPeer peer) {
		Assert.isNotNull(peer);

		// The result list
		List<ILaunchConfiguration> configs = new ArrayList<ILaunchConfiguration>();

		// Determine the ID of the given peer
		final AtomicReference<String> peerId = new AtomicReference<String>();
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				peerId.set(peer.getID());
			}
		};
		if (Protocol.isDispatchThread()) runnable.run();
		else Protocol.invokeAndWait(runnable);

		// Get the launch manager
		ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
		// Get the "Target Communication Framework" launch configuration type
		ILaunchConfigurationType lct = lm.getLaunchConfigurationType("org.eclipse.tcf.debug.LaunchConfigurationType"); //$NON-NLS-1$
		if (lct != null) {
			try {
				// Get all launch configurations of our type
				ILaunchConfiguration[] candidates = lm.getLaunchConfigurations(lct);
				for (ILaunchConfiguration candidate : candidates) {
					// If the peer id is matching, it is a valid candidate
                    String lcPeerId = candidate.getAttribute(TCFLaunchDelegate.ATTR_PEER_ID, (String)null);
                    if (lcPeerId != null && lcPeerId.equals(peerId.get())) {
                    	configs.add(candidate);
                    }
				}
			} catch (CoreException e) {
				if (Platform.inDebugMode()) e.printStackTrace();
			}
		}

		return configs.toArray(new ILaunchConfiguration[configs.size()]);
	}
}
