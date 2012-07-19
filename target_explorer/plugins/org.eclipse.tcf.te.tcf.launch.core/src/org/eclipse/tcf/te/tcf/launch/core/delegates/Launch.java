/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.launch.core.delegates;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.tcf.internal.debug.model.TCFLaunch;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;

/**
 * Default tcf launch implementation.
 * <p>
 * The launch can be adapted to {@link IPropertiesContainer} to exchange user defined data
 * between the launch steps.
 */
public final class Launch extends TCFLaunch {

	/**
	 * Non-notifying properties container used for data exchange between the steps.
	 */
	private final IPropertiesContainer properties = new PropertiesContainer() {
		@Override
		public Object getAdapter(Class adapter) {
			if (ILaunch.class.equals(adapter)) {
				return Launch.this;
			}
			return super.getAdapter(adapter);
		}
	};

	/**
	 * Constructor.
	 *
	 * @param configuration The launch configuration that was launched.
	 * @param mode The launch mode.
	 */
	public Launch(ILaunchConfiguration configuration, String mode) {
		super(configuration, mode);
	}

	/**
	 * Attach the tcf debugger to the given peer model node.
	 *
	 * @param node The peer model node. Must not be <code>null</code>.
	 */
	public void attachDebugger(IPeerModel node) {
		Assert.isNotNull(node);

		final String name = node.getPeer().getName();

		// The debugger is using it's own channel as the implementation
		// calls for channel.terminate(...) directly
		Map<String, Boolean> flags = new HashMap<String, Boolean>();
		flags.put(IChannelManager.FLAG_FORCE_NEW, Boolean.TRUE);
		Tcf.getChannelManager().openChannel(node.getPeer(), flags, new IChannelManager.DoneOpenChannel() {
			@Override
			public void doneOpenChannel(Throwable error, IChannel channel) {
				if (error == null && channel != null) {
					launchTCF(getLaunchMode(), name, channel);
				}
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.Launch#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (IPropertiesContainer.class.equals(adapter)) {
			return properties;
		}

		// Must force adapters to be loaded: (Defect WIND00243348, and Eclipse bug 197664).
		Platform.getAdapterManager().loadAdapter(this, adapter.getName());

		return super.getAdapter(adapter);
	}
}
