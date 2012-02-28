/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.launch.core.internal.adapters;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.interfaces.IDisposable;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext;

/**
 * Adapter factory implementation.
 */
public class AdapterFactory implements IAdapterFactory {
	// Maintain a map of step context adapters per peer model
	/* default */ Map<ILaunch, IStepContext> adapters = new HashMap<ILaunch, IStepContext>();

	private static final Class<?>[] CLASSES = new Class[] {
		IStepContext.class
	};

	/**
	 * Constructor.
	 */
	public AdapterFactory() {
		final ILaunchListener  listener = new ILaunchListener() {
			@Override
			public void launchRemoved(ILaunch launch) {
				IStepContext adapter = adapters.remove(launch);
				if (adapter instanceof IDisposable) {
					((IDisposable)adapter).dispose();
				}
			}
			@Override
			public void launchChanged(ILaunch launch) {
				IStepContext adapter = adapters.remove(launch);
				if (adapter instanceof IDisposable) {
					((IDisposable)adapter).dispose();
				}
			}
			@Override
			public void launchAdded(ILaunch launch) {
			}
		};

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				DebugPlugin.getDefault().getLaunchManager().addLaunchListener(listener);
			}
		};

		if (Protocol.isDispatchThread()) {
			runnable.run();
		}
		else {
			Protocol.invokeAndWait(runnable);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof ILaunch) {
			if (IStepContext.class.equals(adapterType)) {
				// Lookup the adapter
				IStepContext adapter = adapters.get(adaptableObject);
				// No adapter yet -> create a new one for this peer
				if (adapter == null) {
					adapter = new StepContextAdapter((ILaunch)adaptableObject);
					adapters.put((ILaunch)adaptableObject, adapter);
				}
				return adapter;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	public Class[] getAdapterList() {
		return CLASSES;
	}

}
