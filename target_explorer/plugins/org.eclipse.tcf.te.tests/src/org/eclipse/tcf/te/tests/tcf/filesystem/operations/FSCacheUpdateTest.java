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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;

import org.eclipse.tcf.te.tcf.filesystem.core.internal.url.TcfURLConnection;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.CacheManager;

public class FSCacheUpdateTest extends OperationTestBase {

	@Override
    protected void setUp() throws Exception {
	    super.setUp();
		printDebugMessage("Write file " + testFile.getLocation() + "..."); //$NON-NLS-1$ //$NON-NLS-2$
		BufferedOutputStream output = null;
		try {
			URL url = testFile.getLocationURL();
			TcfURLConnection connection = (TcfURLConnection) url.openConnection();
			connection.setDoInput(false);
			connection.setDoOutput(true);
			output = new BufferedOutputStream(connection.getOutputStream());
			output.write("Hello, world!".getBytes()); //$NON-NLS-1$
			output.flush();
		}
		finally {
			if (output != null) {
				try {
					output.close();
				}
				catch (Exception e) {
				}
			}
		}
    }
	
	public void testUpdate() throws Exception {
		updateCache(testFile);
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
}
