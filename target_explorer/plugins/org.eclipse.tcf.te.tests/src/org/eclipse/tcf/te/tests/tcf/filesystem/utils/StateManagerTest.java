/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tests.tcf.filesystem.utils;

import org.eclipse.tcf.te.tcf.filesystem.internal.utils.StateManager;
import org.eclipse.tcf.te.tcf.filesystem.model.CacheState;

@SuppressWarnings("restriction")
public class StateManagerTest extends UtilsTestBase {

	@Override
    protected void setUp() throws Exception {
	    super.setUp();
	    StateManager.getInstance().refreshState(testFile);
    }

	public void testCacheState() {
		CacheState cacheState = StateManager.getInstance().getCacheState(testFile);
		assertEquals(CacheState.consistent, cacheState);
	}
}
