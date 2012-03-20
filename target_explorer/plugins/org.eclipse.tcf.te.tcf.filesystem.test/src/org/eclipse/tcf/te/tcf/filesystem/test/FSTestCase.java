/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.test;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.te.tests.tcf.TcfTestCase;
import org.osgi.framework.Bundle;

public class FSTestCase extends TcfTestCase {
	private static final String FS_PLUGIN_ID = "org.eclipse.tcf.te.tcf.filesystem";
	private static final IProgressMonitor NPM = new NullProgressMonitor();
	private static IPeer globalPeer;

	@Override
	protected void setUp() throws Exception {
		if (globalPeer == null) {
			super.setUp();
			globalPeer = peer;
		}
		Bundle bundle = Platform.getBundle(FS_PLUGIN_ID);
		assertNotNull(bundle);
		if (bundle.getState() != Bundle.ACTIVE) {
			bundle.start();
		}
	}

	@Override
    protected void tearDown() throws Exception {
    }

	protected void log(String message) {
		//System.out.println(message);
	}

	protected IProgressMonitor getProgressMonitor() {
		return NPM;
	}
}
