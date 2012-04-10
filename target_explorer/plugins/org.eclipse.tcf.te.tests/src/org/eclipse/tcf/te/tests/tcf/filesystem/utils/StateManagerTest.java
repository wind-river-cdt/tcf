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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpCacheUpdate;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.CacheManager;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.StateManager;
import org.eclipse.tcf.te.tcf.filesystem.core.model.CacheState;

public class StateManagerTest extends UtilsTestBase {

	@Override
    protected void setUp() throws Exception {
	    super.setUp();
	    OpCacheUpdate update = new OpCacheUpdate(testFile);
	    update.run(new NullProgressMonitor());
    }

	public void testCacheStateConsistent() throws Exception {
	    OpCacheUpdate update = new OpCacheUpdate(testFile);
	    update.run(new NullProgressMonitor());
		CacheState cacheState = StateManager.getCacheState(testFile);
		assertEquals(CacheState.consistent, cacheState);
	}
	
	public void testCacheStateModified() throws Exception {
	    OpCacheUpdate update = new OpCacheUpdate(testFile);
	    update.run(new NullProgressMonitor());
	    File file = CacheManager.getCacheFile(testFile);
	    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
	    writer.write("hello, world"); //$NON-NLS-1$
	    writer.close();
		CacheState cacheState = StateManager.getCacheState(testFile);
	    assertEquals(CacheState.modified, cacheState);
	}
	
	public void testCacheStateOutdated() throws Exception {
	    OpCacheUpdate update = new OpCacheUpdate(testFile);
	    update.run(new NullProgressMonitor());
		writeFileContent("hello,world!"); //$NON-NLS-1$
	    StateManager.refreshState(testFile);
		CacheState cacheState = StateManager.getCacheState(testFile);
		assertEquals(CacheState.outdated, cacheState);
	}
	
	public void testCacheStateConflict() throws Exception {
	    OpCacheUpdate update = new OpCacheUpdate(testFile);
	    update.run(new NullProgressMonitor());
		writeFileContent("hello,world!"); //$NON-NLS-1$
	    StateManager.refreshState(testFile);
	    File file = CacheManager.getCacheFile(testFile);
	    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
	    writer.write("hello, world"); //$NON-NLS-1$
	    writer.close();
		CacheState cacheState = StateManager.getCacheState(testFile);
		assertEquals(CacheState.conflict, cacheState);
	}
}
