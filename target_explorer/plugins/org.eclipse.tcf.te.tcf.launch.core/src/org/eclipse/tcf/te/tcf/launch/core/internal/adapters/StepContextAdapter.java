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

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext;

/**
 * Peer model step context adapter implementation.
 */
public class StepContextAdapter extends PlatformObject implements IStepContext {
	// Reference to the launch
	private final ILaunch launch;

	/**
	 * Constructor.
	 *
	 * @param launch The launch. Must not be <code>null</code>.
	 */
	public StepContextAdapter(ILaunch launch) {
		super();
		Assert.isNotNull(launch);
		this.launch = launch;
	}

	/**
	 * Returns the launch.
	 * @return The launch.
	 */
	public ILaunch getLaunch() {
		return launch;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext#getId()
	 */
	@Override
	public String getId() {
		return launch != null && launch.getLaunchConfiguration() != null ? launch.getLaunchConfiguration().getName() : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext#getSecondaryId()
	 */
	@Override
	public String getSecondaryId() {
		return launch != null ? launch.getLaunchMode() : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext#getName()
	 */
	@Override
	public String getName() {
		final AtomicReference<String> name = new AtomicReference<String>();

		if (launch != null) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					name.set(getId());
				}
			};

			if (Protocol.isDispatchThread()) {
				runnable.run();
			}
			else {
				Protocol.invokeAndWait(runnable);
			}
		}

		return name.get();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext#getContextObject()
	 */
	@Override
	public Object getContextObject() {
		return launch;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext#getInfo(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer)
	 */
	@Override
	public String getInfo(IPropertiesContainer data) {
		try {
			return getName() + "(" + launch.getLaunchMode() + ") - " + launch.getLaunchConfiguration().getType().getName(); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch (CoreException e) {
		}
		return getName() + "(" + launch.getLaunchMode() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(final Class adapter) {
		// NOTE: The getAdapter(...) method can be invoked from many place and
		//       many threads where we cannot control the calls. Therefore, this
		//       method is allowed be called from any thread.
		final AtomicReference<Object> object = new AtomicReference<Object>();
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				object.set(doGetAdapter(adapter));
			}
		};

		if (Protocol.isDispatchThread()) {
			runnable.run();
		}
		else {
			Protocol.invokeAndWait(runnable);
		}

		return object.get() != null ? object.get() : super.getAdapter(adapter);
	}

	/**
	 * Returns an object which is an instance of the given class associated with this object.
	 * Returns <code>null</code> if no such object can be found.
	 * <p>
	 * This method must be called within the TCF dispatch thread!
	 *
	 * @param adapter The adapter class to look up.
	 * @return The adapter or <code>null</code>.
	 */
	protected Object doGetAdapter(Class<?> adapter) {
		if (ILaunch.class.equals(adapter)) {
			return launch;
		}

		if (ILaunchConfiguration.class.equals(adapter)) {
			return launch.getLaunchConfiguration();
		}

		if (ILaunchConfigurationType.class.equals(adapter)) {
			try {
				return launch.getLaunchConfiguration().getType();
			}
			catch (CoreException e) {
			}
		}

		return null;
	}
}
