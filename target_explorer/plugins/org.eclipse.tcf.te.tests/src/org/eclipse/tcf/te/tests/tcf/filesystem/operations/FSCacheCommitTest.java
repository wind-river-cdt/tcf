/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tests.tcf.filesystem.operations;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.CacheManager;

public class FSCacheCommitTest extends OperationTestBase {

	@Override
    protected void setUp() throws Exception {
	    super.setUp();
		File file = CacheManager.getCacheFile(testFile);
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write("Hello, world, this is called from client!"); //$NON-NLS-1$
		writer.close();
    }

	protected String readFileContent() throws IOException {
		printDebugMessage("Read file " + testFile.getLocation() + "..."); //$NON-NLS-1$ //$NON-NLS-2$
		URL url = testFile.getLocationURL();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuilder buffer = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}
			return buffer.toString();
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				}
				catch (Exception e) {
				}
			}
		}
	}

	public void testCommit() throws Exception {
		commitCache(testFile);
		String content = readFileContent();
		assertEquals("Hello, world, this is called from client!", content); //$NON-NLS-1$
	}
}
