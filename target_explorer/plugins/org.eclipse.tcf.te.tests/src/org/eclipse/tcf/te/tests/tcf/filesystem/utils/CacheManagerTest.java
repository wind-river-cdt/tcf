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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.tcf.te.tcf.filesystem.internal.utils.CacheManager;

@SuppressWarnings("restriction")
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

	public void testDownload() throws Exception {
		writeFileContent("Hello, world!"); //$NON-NLS-1$
		CacheManager.getInstance().download(testFile);
		File file = CacheManager.getInstance().getCacheFile(testFile);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String content=""; //$NON-NLS-1$
		String line;
		while ((line = reader.readLine()) != null) {
			content += line;
		}
		reader.close();
		assertEquals("Hello, world!", content); //$NON-NLS-1$
	}

	public void testUpload() throws Exception {
		File file = CacheManager.getInstance().getCacheFile(testFile);
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write("Hello, world, this is called from client!"); //$NON-NLS-1$
		writer.close();
		CacheManager.getInstance().uploadFiles(new NullProgressMonitor(), new File[]{file}, new URL[]{testFile.getLocationURL()}, null);
		String content = readFileContent();
		assertEquals("Hello, world, this is called from client!", content); //$NON-NLS-1$
	}
}
