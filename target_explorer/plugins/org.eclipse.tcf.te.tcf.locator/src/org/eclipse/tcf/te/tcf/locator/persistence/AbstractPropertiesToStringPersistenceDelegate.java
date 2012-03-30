/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.tcf.te.tcf.locator.persistence;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.protocol.JSON;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.persistence.AbstractPropertiesPersistenceDelegate;

/**
 * AbstractPropertiesToStringPersistenceDelegate
 * @author tobias.schwarz@windriver.com
 */
public abstract class AbstractPropertiesToStringPersistenceDelegate extends AbstractPropertiesPersistenceDelegate {

	/**
	 * Constructor.
	 */
	public AbstractPropertiesToStringPersistenceDelegate() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.properties.PropertiesToURIPersistenceDelegate#write(java.lang.Object, java.lang.Object, java.lang.String)
	 */
	@Override
	public Object write(final Object context, Object container, String key) throws IOException {
		Assert.isNotNull(context);
		Assert.isNotNull(container);

		if (container instanceof String || String.class.equals(container)) {
			final AtomicReference<String> encoded = new AtomicReference<String>();

			Runnable runnable = new Runnable() {
				@SuppressWarnings("synthetic-access")
				@Override
				public void run() {
					try {
						encoded.set(JSON.toJSON(toMap(context)));
					}
					catch (IOException e) {
					}
				}
			};

			if (Protocol.isDispatchThread()) {
				runnable.run();
			}
			else {
				Protocol.invokeAndWait(runnable);
			}
			return encoded.get();
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.properties.PropertiesToURIPersistenceDelegate#read(java.lang.Object, java.lang.Object, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object read(final Object context, final Object container, String key) throws IOException {
		Assert.isNotNull(context);
		Assert.isNotNull(container);

		if (container instanceof String) {
			final AtomicReference<Object> decoded = new AtomicReference<Object>();

			Runnable runnable = new Runnable() {
				@SuppressWarnings("synthetic-access")
				@Override
				public void run() {
					try {
						Object o = JSON.parseOne(((String)container).getBytes("UTF-8")); //$NON-NLS-1$
						if (o instanceof Map) {
							decoded.set(fromMap((Map<String,Object>)o, container));
						}
					} catch (IOException e) {
					}
				}
			};

			if (Protocol.isDispatchThread()) {
				runnable.run();
			}
			else {
				Protocol.invokeAndWait(runnable);
			}

			return decoded.get();
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate#delete(java.lang.Object, java.lang.Object, java.lang.String)
	 */
	@Override
	public boolean delete(Object context, Object container, String key) throws IOException {
		return false;
	}
}
