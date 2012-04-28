/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.osgi;

import org.eclipse.tcf.osgi.services.IValueAddService;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.te.tcf.core.Tcf;

/**
 * TCF framework value-add OSGi service implementation.
 */
public class ValueAddService implements IValueAddService {

	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstance {
		public static ValueAddService instance = new ValueAddService();
	}

	/**
	 * Constructor.
	 */
	ValueAddService() {
		super();
	}

	/**
	 * Returns the singleton instance of the service.
	 */
	public static ValueAddService getInstance() {
		return LazyInstance.instance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.osgi.services.IValueAddService#getRedirectionPath(org.eclipse.tcf.protocol.IPeer, org.eclipse.tcf.osgi.services.IValueAddService.DoneGetRedirectionPath)
	 */
	@Override
    public void getRedirectionPath(IPeer peer, IValueAddService.DoneGetRedirectionPath done) {
		Tcf.getChannelManager().getRedirectionPath(peer, done);
	}

}
