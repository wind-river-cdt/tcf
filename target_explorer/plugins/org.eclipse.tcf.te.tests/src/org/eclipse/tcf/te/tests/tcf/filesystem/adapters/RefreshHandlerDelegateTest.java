/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tests.tcf.filesystem.adapters;

import org.eclipse.core.runtime.Platform;
import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
import org.eclipse.tcf.te.tests.tcf.filesystem.FSPeerTestCase;
import org.eclipse.tcf.te.ui.views.interfaces.handler.IRefreshHandlerDelegate;

public class RefreshHandlerDelegateTest extends FSPeerTestCase {
	private IRefreshHandlerDelegate delegate;

    @Override
	protected void setUp() throws Exception {
		super.setUp();
		delegate = (IRefreshHandlerDelegate) Platform.getAdapterManager().getAdapter(testFolder, IRefreshHandlerDelegate.class);
		assertNotNull(delegate);
	}

	public void testCanRefresh() {
		assertTrue(delegate.canRefresh(testFolder));
	}

	public void testRefresh() throws Exception {
		PropertiesContainer props = new PropertiesContainer();
		delegate.refresh(testFolder, props, null);
	}
}
