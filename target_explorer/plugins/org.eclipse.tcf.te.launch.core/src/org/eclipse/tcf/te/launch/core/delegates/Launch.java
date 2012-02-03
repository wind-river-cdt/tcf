/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.delegates;

import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;

/**
 * Default launch implementation.
 * <p>
 * The launch can be adapted to {@link IPropertiesContainer} to exchange user defined data
 * between the launch steps.
 */
public final class Launch extends org.eclipse.debug.core.Launch {

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

	// Reference to an optional terminate delegate
	private ITerminate terminateDelegate;
	// Reference to an optional disconnect delegate
	private IDisconnect disconnectDelegate;

	/**
	 * Constructor.
	 *
	 * @param configuration The launch configuration that was launched.
	 * @param mode The launch mode.
	 * @param locator The source locator to use for this launch or <code>null</code>.
	 */
	public Launch(ILaunchConfiguration configuration, String mode, ISourceLocator locator) {
		super(configuration, mode, locator);
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

	/**
	 * Associates the given terminate delegate.
	 *
	 * @param terminateDelegate The terminate delegate.
	 */
	public void setTerminateDelegate(ITerminate terminateDelegate) {
		this.terminateDelegate = terminateDelegate;
	}

	/**
	 * Associates the given disconnect delegate.
	 *
	 * @param disconnectDelegate The disconnect delegate.
	 */
	public void setDisconnectDelegate(IDisconnect disconnectDelegate) {
		this.disconnectDelegate = disconnectDelegate;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.Launch#canTerminate()
	 */
	@Override
	public boolean canTerminate() {
		if (terminateDelegate != null) {
			return terminateDelegate.canTerminate();
		}
		return super.canTerminate();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.Launch#terminate()
	 */
	@Override
	public void terminate() throws DebugException {
		if (terminateDelegate != null) {
			terminateDelegate.terminate();
		}
		super.terminate();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.Launch#isTerminated()
	 */
	@Override
	public boolean isTerminated() {
		if (terminateDelegate != null) {
			return terminateDelegate.isTerminated();
		}
		return super.isTerminated();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.Launch#fireTerminate()
	 */
	@Override
	public void fireTerminate() {
		super.fireTerminate();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.Launch#canDisconnect()
	 */
	@Override
	public boolean canDisconnect() {
		if (disconnectDelegate != null) {
			return disconnectDelegate.canDisconnect();
		}
		return super.canDisconnect();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.Launch#disconnect()
	 */
	@Override
	public void disconnect() throws DebugException {
		if (disconnectDelegate != null) {
			disconnectDelegate.disconnect();
		}
		super.disconnect();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.Launch#isDisconnected()
	 */
	@Override
	public boolean isDisconnected() {
		if (disconnectDelegate != null) {
			return disconnectDelegate.isDisconnected();
		}
		return super.isDisconnected();
	}
}
