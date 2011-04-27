/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.locator.nodes;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.te.tcf.locator.interfaces.IModelListener;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModelProperties;


/**
 * Default peer model implementation.
 */
public class PeerModel extends PlatformObject implements IPeerModel {
	// Reference to the parent locator model
	private final ILocatorModel fParentModel;

	/**
	 * The custom properties map. The keys are always strings, the value might be any object.
	 */
	private Map<String, Object> fProperties = new LinkedHashMap<String, Object>();

	/**
	 * Constructor.
	 *
	 * @param parent The parent locator model. Must be not <code>null</code>.
	 */
	public PeerModel(ILocatorModel parent) {
		this(parent, null);
	}

	/**
	 * Constructor.
	 *
	 * @param parent The parent locator model. Must be not <code>null</code>.
	 * @param peer The peer or <code>null</code>.
	 */
	public PeerModel(ILocatorModel parent, IPeer peer) {
		super();

		assert parent != null;
		fParentModel = parent;

		setProperty(IPeerModelProperties.PROP_INSTANCE, peer);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel#getModel()
	 */
	public ILocatorModel getModel() {
		return (ILocatorModel)getAdapter(ILocatorModel.class);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel#getPeer()
	 */
	public IPeer getPeer() {
		return (IPeer)getAdapter(IPeer.class);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings({ "rawtypes" })
	@Override
	public Object getAdapter(final Class adapter) {
		// NOTE: The getAdapter(...) method can be invoked from many place and
		//       many threads where we cannot control the calls. Therefore, this
		//       method is the only one which is allowed to call from any thread.
		final Object[] object = new Object[1];
		if (Protocol.isDispatchThread()) {
			object[0] = doGetAdapter(adapter);
		} else {
			Protocol.invokeAndWait(new Runnable() {
				public void run() {
					object[0] = doGetAdapter(adapter);
				}
			});
		}
		return object[0] != null ? object[0] : super.getAdapter(adapter);
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
		assert Protocol.isDispatchThread();

		if (adapter.isAssignableFrom(ILocatorModel.class)) {
			return fParentModel;
		}

		Object peer = getProperty(IPeerModelProperties.PROP_INSTANCE);
		if (peer != null && adapter.isAssignableFrom(peer.getClass())) {
			return peer;
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel#getProperties()
	 */
	public Map<String, Object> getProperties() {
		assert Protocol.isDispatchThread();
		return Collections.unmodifiableMap(new HashMap<String, Object>(fProperties));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel#getProperty(java.lang.String)
	 */
	public Object getProperty(String key) {
		assert Protocol.isDispatchThread();

		if (!fProperties.containsKey(key)
				&& getPeer() != null && getPeer().getAttributes().containsKey(key)) {
			return getPeer().getAttributes().get(key);
		}

		return fProperties.get(key);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel#getBooleanProperty(java.lang.String)
	 */
	public final boolean getBooleanProperty(String key) {
		Object value = getProperty(key);
		if (value instanceof Boolean) {
			return ((Boolean)value).booleanValue();
		}
		if (value instanceof String) {
			String val = ((String)value).trim();
			return "TRUE".equalsIgnoreCase(val) || "1".equals(val) || "Y".equalsIgnoreCase(val) ||  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"JA".equalsIgnoreCase(val) || "YES".equalsIgnoreCase(val); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel#getLongProperty(java.lang.String)
	 */
	public final long getLongProperty(String key) {
		Object value = getProperty(key);
		if (value instanceof Long) {
			return ((Long)value).longValue();
		}
		else if (value instanceof Integer) {
			return ((Integer)value).intValue();
		}
		else if (value != null) {
			try {
				return Long.decode(value.toString()).longValue();
			}
			catch (Exception e) {}
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel#getIntProperty(java.lang.String)
	 */
	public final int getIntProperty(String key) {
		Object value = getProperty(key);
		try {
			return value instanceof Integer ? ((Integer)value).intValue() :
				(value != null ? Integer.decode(value.toString()).intValue() : -1);
		}
		catch (Exception e) {
			return -1;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel#getStringProperty(java.lang.String)
	 */
	public final String getStringProperty(String key) {
		Object value = getProperty(key);
		return value instanceof String ? (String)value :
			(value != null ? value.toString() : null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel#getFloatProperty(java.lang.String)
	 */
	public final float getFloatProperty(String key) {
		Object value = getProperty(key);
		try {
			return value instanceof Float ? ((Float)value).floatValue() :
				(value != null ? Float.parseFloat(value.toString()) : Float.NaN);
		}
		catch (Exception e) {
			return Float.NaN;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel#getDoubleProperty(java.lang.String)
	 */
	public final double getDoubleProperty(String key) {
		Object value = getProperty(key);
		try {
			return value instanceof Double ? ((Double)value).doubleValue() :
				(value != null ? Double.parseDouble(value.toString()) : Double.NaN);
		}
		catch (Exception e) {
			return Double.NaN;
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel#setProperties(java.util.Map)
	 */
	public final void setProperties(Map<String, Object> properties) {
		assert Protocol.isDispatchThread() && properties != null;

		fProperties.clear();
		fProperties.putAll(properties);

		final IModelListener[] listeners = fParentModel.getListener();
		if (listeners.length > 0) {
			Protocol.invokeLater(new Runnable() {
				@SuppressWarnings("synthetic-access")
				public void run() {
					for (IModelListener listener : listeners) {
						listener.peerModelChanged(fParentModel, PeerModel.this);
					}
				}
			});
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel#setProperty(java.lang.String, boolean)
	 */
	public final boolean setProperty(String key, boolean value) {
		boolean oldValue = getBooleanProperty(key);
		if (oldValue != value) {
			return setProperty(key, Boolean.valueOf(value));
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel#setProperty(java.lang.String, long)
	 */
	public final boolean setProperty(String key, long value) {
		long oldValue = getLongProperty(key);
		if (oldValue != value) {
			return setProperty(key, Long.valueOf(value));
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel#setProperty(java.lang.String, int)
	 */
	public final boolean setProperty(String key, int value) {
		int oldValue = getIntProperty(key);
		if (oldValue != value) {
			return setProperty(key, Integer.valueOf(value));
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel#setProperty(java.lang.String, float)
	 */
	public final boolean setProperty(String key, float value) {
		float oldValue = getFloatProperty(key);
		if (oldValue != value) {
			return setProperty(key, Float.valueOf(value));
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel#setProperty(java.lang.String, double)
	 */
	public final boolean setProperty(String key, double value) {
		double oldValue = getDoubleProperty(key);
		if (oldValue != value) {
			return setProperty(key, Double.valueOf(value));
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel#setProperty(java.lang.String, java.lang.Object)
	 */
	public boolean setProperty(String key, Object value) {
		assert Protocol.isDispatchThread();

		Object oldValue = fProperties.get(key);
		if ((oldValue == null && value != null) || (oldValue != null && !oldValue.equals(value))) {
			if (value != null) {
				fProperties.put(key, value);
			} else {
				fProperties.remove(key);
			}

			final IModelListener[] listeners = fParentModel.getListener();
			if (listeners.length > 0) {
				Protocol.invokeLater(new Runnable() {
					@SuppressWarnings("synthetic-access")
					public void run() {
						for (IModelListener listener : listeners) {
							listener.peerModelChanged(fParentModel, PeerModel.this);
						}
					}
				});
			}

			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel#clearProperties()
	 */
	public final void clearProperties() {
		assert Protocol.isDispatchThread();
		fProperties.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel#isProperty(java.lang.String, long)
	 */
	public final boolean isProperty(String key, long value) {
		return getLongProperty(key) == value;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel#isProperty(java.lang.String, boolean)
	 */
	public final boolean isProperty(String key, boolean value) {
		return getBooleanProperty(key) == value;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel#isProperty(java.lang.String, int)
	 */
	public final boolean isProperty(String key, int value) {
		return getIntProperty(key) == value;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel#isProperty(java.lang.String, float)
	 */
	public final boolean isProperty(String key, float value) {
		return getFloatProperty(key) == value;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel#isProperty(java.lang.String, double)
	 */
	public final boolean isProperty(String key, double value) {
		return getDoubleProperty(key) == value;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel#isPropertyIgnoreCase(java.lang.String, java.lang.String)
	 */
	public final boolean isPropertyIgnoreCase(String key, String value) {
		String property = getStringProperty(key);
		return (property == null && value == null) || (property != null && property.equalsIgnoreCase(value));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.locator.core.interfaces.nodes.IPeerModel#isProperty(java.lang.String, java.lang.Object)
	 */
	public final boolean isProperty(String key, Object value) {
		Object property = getProperty(key);
		return (property == null && value == null) || (property != null && property.equals(value));
	}
}