/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.locator.internal.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.services.AbstractService;
import org.eclipse.tcf.te.runtime.services.interfaces.IPropertiesAccessService;
import org.eclipse.tcf.te.runtime.services.interfaces.constants.IPropertiesAccessServiceConstants;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;

/**
 * Peer model properties access service implementation.
 */
public class PropertiesAccessService extends AbstractService implements IPropertiesAccessService {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.services.interfaces.IPropertiesAccessService#getTargetAddress(java.lang.Object)
	 */
	@Override
	public Map<String, String> getTargetAddress(Object context) {
		final Map<String, String> result = new HashMap<String, String>();

		if (context instanceof IPeerModel) {
			final IPeerModel peerModel = (IPeerModel) context;

			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					Map<String, String> attributes = peerModel.getPeer().getAttributes();

					String value = attributes.get(IPeer.ATTR_NAME);
					if (value != null && !"".equals(value.trim())) { //$NON-NLS-1$
						result.put(IPropertiesAccessServiceConstants.PROP_NAME, value);
					}

					value = attributes.get(IPeer.ATTR_IP_HOST);
					if (value != null && !"".equals(value.trim())) { //$NON-NLS-1$
						result.put(IPropertiesAccessServiceConstants.PROP_ADDRESS, value);
					}

					value = attributes.get(IPeer.ATTR_IP_PORT);
					if (value != null && !"".equals(value.trim())) { //$NON-NLS-1$
						result.put(IPropertiesAccessServiceConstants.PROP_PORT, value);
					}
				}
			};

			if (Protocol.isDispatchThread()) runnable.run();
			else Protocol.invokeAndWait(runnable);
		}

		return !result.isEmpty() ? Collections.unmodifiableMap(result) : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.services.interfaces.IPropertiesAccessService#getProperty(java.lang.Object, java.lang.String)
	 */
	@Override
	public Object getProperty(final Object context, final String key) {
		Assert.isNotNull(context);
		Assert.isNotNull(key);

		final AtomicReference<Object> value = new AtomicReference<Object>();
		if (context instanceof IPeerModel) {
			final IPeerModel peerModel = (IPeerModel) context;

			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					value.set(peerModel.getProperty(key));
				}
			};

			if (Protocol.isDispatchThread()) runnable.run();
			else Protocol.invokeAndWait(runnable);
		}

	    return value.get();
	}

}
