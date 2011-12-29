/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.locator.nodes;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.interfaces.workingsets.IWorkingSetElement;
import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
import org.eclipse.tcf.te.tcf.locator.interfaces.IModelListener;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProperties;


/**
 * Default peer model implementation.
 */
public class PeerModel extends PropertiesContainer implements IPeerModel, IWorkingSetElement {
	// Reference to the parent locator model
	private final ILocatorModel model;
	// Reference to the parent peer model node
	private IPeerModel node;
	// Reference to the peer id (cached for performance optimization)
	private String peerId;

	/**
	 * Constructor.
	 *
	 * @param model The parent locator model. Must not be <code>null</code>.
	 * @param peer The peer or <code>null</code>.
	 */
	public PeerModel(ILocatorModel model, IPeer peer) {
		super();

		Assert.isNotNull(model);
		this.model = model;

		// Set the default properties before enabling the change events.
		// The properties changed listeners should not be called from the
		// constructor.
		setProperty(IPeerModelProperties.PROP_INSTANCE, peer);

		// Initialize the peer id
		peerId = peer.getID();
		Assert.isNotNull(peerId);

		// Enable change events
		setChangeEventsEnabled(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.properties.PropertiesContainer#checkThreadAccess()
	 */
	@Override
	protected final boolean checkThreadAccess() {
	    return Protocol.isDispatchThread();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel#getModel()
	 */
	@Override
	public ILocatorModel getModel() {
		return (ILocatorModel)getAdapter(ILocatorModel.class);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel#getPeer()
	 */
	@Override
	public IPeer getPeer() {
		return (IPeer)getAdapter(IPeer.class);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.workingsets.IWorkingSetElement#getElementId()
	 */
	@Override
	public String getElementId() {
		return peerId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel#getPeerId()
	 */
	@Override
	public String getPeerId() {
	    return peerId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel#setParentNode(org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel)
	 */
	@Override
	public void setParentNode(IPeerModel node) {
		Assert.isTrue(checkThreadAccess(), "Illegal Thread Access"); //$NON-NLS-1$
		this.node = node;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel#getParentNode()
	 */
	@Override
	public IPeerModel getParentNode() {
		Assert.isTrue(checkThreadAccess(), "Illegal Thread Access"); //$NON-NLS-1$
	    return node;
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
		Assert.isTrue(checkThreadAccess(), "Illegal Thread Access"); //$NON-NLS-1$

		if (adapter.isAssignableFrom(ILocatorModel.class)) {
			return model;
		}

		Object peer = getProperty(IPeerModelProperties.PROP_INSTANCE);
		if (peer != null && adapter.isAssignableFrom(peer.getClass())) {
			return peer;
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.properties.PropertiesContainer#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder buffer = new StringBuilder(getClass().getSimpleName());

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				IPeer peer = getPeer();
				buffer.append(": id=" + peer.getID()); //$NON-NLS-1$
				buffer.append(", name=" + peer.getName()); //$NON-NLS-1$
			}
		};

		if (Protocol.isDispatchThread()) runnable.run();
		else Protocol.invokeAndWait(runnable);

		buffer.append(", " + super.toString()); //$NON-NLS-1$
		return buffer.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.properties.PropertiesContainer#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PeerModel) {
			return getPeerId().equals(((PeerModel)obj).getPeerId());
		}
	    return super.equals(obj);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.properties.PropertiesContainer#hashCode()
	 */
	@Override
	public int hashCode() {
	    return getPeerId().hashCode();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.properties.PropertiesContainer#postSetProperties(java.util.Map)
	 */
	@Override
	protected void postSetProperties(Map<String, Object> properties) {
		Assert.isTrue(checkThreadAccess(), "Illegal Thread Access"); //$NON-NLS-1$
		Assert.isNotNull(properties);
		Assert.isNotNull(getPeer());

		// New properties applied. Update the element id
		peerId = getPeer().getID();
		Assert.isNotNull(peerId);

		if (changeEventsEnabled()) {
			final IModelListener[] listeners = model.getListener();
			if (listeners.length > 0) {
				Protocol.invokeLater(new Runnable() {
					@Override
					@SuppressWarnings("synthetic-access")
					public void run() {
						for (IModelListener listener : listeners) {
							listener.peerModelChanged(model, PeerModel.this);
						}
					}
				});
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.properties.PropertiesContainer#postSetProperty(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void postSetProperty(String key, Object value, Object oldValue) {
		Assert.isTrue(checkThreadAccess(), "Illegal Thread Access"); //$NON-NLS-1$
		Assert.isNotNull(key);
		Assert.isNotNull(getPeer());

		// If the peer instance changed, update the element id
		if (IPeerModelProperties.PROP_INSTANCE.equals(key)) {
			peerId = getPeer().getID();
			Assert.isNotNull(peerId);
		}

		// Notify registered listeners that the peer changed. Property
		// changes for property slots ending with ".silent" are suppressed.
		if (changeEventsEnabled() && !key.endsWith(".silent")) { //$NON-NLS-1$
			final IModelListener[] listeners = model.getListener();
			if (listeners.length > 0) {
				Protocol.invokeLater(new Runnable() {
					@Override
					@SuppressWarnings("synthetic-access")
					public void run() {
						for (IModelListener listener : listeners) {
							listener.peerModelChanged(model, PeerModel.this);
						}
					}
				});
			}
		}
	}
}
