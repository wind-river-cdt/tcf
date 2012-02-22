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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.tcf.core.TransientPeer;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.JSON;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext;
import org.eclipse.tcf.te.tcf.launch.core.activator.CoreBundleActivator;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.services.ILocatorModelLookupService;
import org.eclipse.tcf.te.tcf.locator.internal.nodes.InvalidPeerModel;
import org.eclipse.tcf.te.tcf.locator.model.Model;
import org.eclipse.tcf.te.tcf.locator.nodes.PeerModel;

/**
 * Peer model step context adapter implementation.
 */
@SuppressWarnings("restriction")
public class StepContextAdapter extends PlatformObject implements IStepContext {
	// Reference to the wrapped peer model
	/* default */ IPeerModel peerModel;

	/**
     * Constructor.
     *
     * @param peerModel The peer model. Must not be <code>null</code>.
     */
    public StepContextAdapter(IPeerModel peerModel) {
    	super();
    	Assert.isNotNull(peerModel);
    	this.peerModel = peerModel;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.interfaces.IModelNodeProvider#getModelNode()
	 */
	@Override
	public IModelNode getModelNode() {
		return peerModel;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext#getId()
	 */
	@Override
	public String getId() {
		return peerModel != null ? peerModel.getPeerId() : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext#getName()
	 */
	@Override
	public String getName() {
		final AtomicReference<String> name = new AtomicReference<String>();

		if (peerModel != null) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					name.set(peerModel.getName());
				}
			};

			if (Protocol.isDispatchThread()) runnable.run();
			else Protocol.invokeAndWait(runnable);
		}

		return name.get();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext#getInfo(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer)
	 */
	@Override
	public String getInfo(IPropertiesContainer data) {
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext#exists()
	 */
	@Override
	public boolean exists() {
		final AtomicBoolean isGhost = new AtomicBoolean();

		if (peerModel != null) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					isGhost.set(peerModel.getBooleanProperty(IModelNode.PROPERTY_IS_GHOST));
				}
			};

			if (Protocol.isDispatchThread()) runnable.run();
			else Protocol.invokeAndWait(runnable);
		}

	    return peerModel != null && !isGhost.get();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext#encode()
	 */
	@Override
	public String encode() {
		final AtomicReference<String> encoded = new AtomicReference<String>();

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					Map<String, String> attrs = new HashMap<String, String>(peerModel.getPeer().getAttributes());

					// Remove all transient attributes
					String[] keys = attrs.keySet().toArray(new String[attrs.keySet().size()]);
					for (String key : keys) {
						if (key.endsWith(".transient")) { //$NON-NLS-1$
							attrs.remove(key);
						}
					}

	                encoded.set(JSON.toJSON(attrs));
                }
                catch (IOException e) {
					if (Platform.inDebugMode()) {
						IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
													"StepContextAdapter encode failure: " + e.getLocalizedMessage(), e); //$NON-NLS-1$
						Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
					}
                }
			}
		};

		if (Protocol.isDispatchThread()) runnable.run();
		else Protocol.invokeAndWait(runnable);

	    return encoded.get() != null ? encoded.get() : ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext#getEncodedClassName()
	 */
	@Override
	public String getEncodedClassName() {
	    return IPeerModel.class.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext#decode(java.lang.String)
	 */
	@Override
	public void decode(final String value) throws IOException {
		Assert.isNotNull(value);

		final AtomicReference<IOException> error = new AtomicReference<IOException>();

		Runnable runnable = new Runnable() {
            @Override
			public void run() {
				try {
					Object o = JSON.parseOne(value.getBytes("UTF-8")); //$NON-NLS-1$
					// The decoded object should be a map
					if (o instanceof Map) {
						@SuppressWarnings("unchecked")
                        Map<String, String> attrs = (Map<String, String>)o;

						// Get the id of the decoded attributes
						String id = attrs.get("ID"); //$NON-NLS-1$
						if (id == null) throw new IOException("StepContextAdapter#decode: Mandatory attribure 'ID' is missing."); //$NON-NLS-1$

						// If the ID is matching the associated peer model, than we are done here
						if (peerModel != null && !(peerModel instanceof InvalidPeerModel) && peerModel.getPeerId().equals(id)) {
							return;
						}

						// Lookup the id within the model
						IPeerModel candidate = Model.getModel().getService(ILocatorModelLookupService.class).lkupPeerModelById(id);
						if (candidate != null) {
							peerModel = candidate;
							return;
						}

						// Not found in the model -> create a ghost object
						IPeer peer = new TransientPeer(attrs);
						peerModel = new PeerModel(Model.getModel(), peer);
						peerModel.setProperty(IModelNode.PROPERTY_IS_GHOST, true);
					} else {
						throw new IOException("StepContextAdapter#decode: Object not of map type."); //$NON-NLS-1$
					}
				} catch (IOException e) {
					error.set(e);
				}
			}
		};

		if (Protocol.isDispatchThread()) runnable.run();
		else Protocol.invokeAndWait(runnable);

		if (error.get() != null) throw error.get();
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

		if (Protocol.isDispatchThread()) runnable.run();
		else Protocol.invokeAndWait(runnable);

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
		if (IModelNode.class.isAssignableFrom(adapter)) {
			return peerModel;
		}

		if (IPeer.class.equals(adapter)) {
			return peerModel.getPeer();
		}

		return null;
	}
}
