/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.services.contexts.internal;

import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IService;
import org.eclipse.tcf.protocol.IServiceProvider;

/**
 * TCF contexts service provider implementation.
 */
public class ServiceProvider implements IServiceProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.protocol.IServiceProvider#getLocalService(org.eclipse.tcf.protocol.IChannel)
	 */
	@Override
	public IService[] getLocalService(IChannel channel) {
		return new IService[] { new ContextsProxy(channel) };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.protocol.IServiceProvider#getServiceProxy(org.eclipse.tcf.protocol.IChannel, java.lang.String)
	 */
	@Override
	public IService getServiceProxy(IChannel channel, String service_name) {
		return null;
	}

}
