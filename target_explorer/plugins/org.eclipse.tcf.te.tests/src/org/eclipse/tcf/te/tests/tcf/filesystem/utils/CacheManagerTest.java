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

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.CacheManager;

public class CacheManagerTest extends UtilsTestBase {

	public void testCachePath() throws Exception {
		IPath path = CacheManager.getInstance().getCachePath(test11File);
		File file = path.toFile();
		String abspath = file.getAbsolutePath();
		assertNotNull(abspath);
	}

	public void testCacheFile() throws Exception {
		File file = CacheManager.getInstance().getCacheFile(test11File);
		String abspath = file.getAbsolutePath();
		assertNotNull(abspath);
	}

	public void testCacheRoot() throws Exception {
		File file = CacheManager.getInstance().getCacheRoot();
		String abspath = file.getAbsolutePath();
		assertNotNull(abspath);
	}
}
