/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.test.url;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.eclipse.tcf.te.tcf.filesystem.internal.url.TcfURLConnection;
import org.eclipse.tcf.te.tcf.filesystem.test.FSPeerTestCase;

@SuppressWarnings("restriction")
public class URLTestBase extends FSPeerTestCase {

	protected String readFileContent() throws IOException {
		log("Read file " + testFile.getLocation() + "...");
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

	protected void writeFileContent(String content) throws IOException {
		log("Write file " + testFile.getLocation() + "...");
		BufferedOutputStream output = null;
		try {
			URL url = testFile.getLocationURL();
			TcfURLConnection connection = (TcfURLConnection) url.openConnection();
			connection.setDoInput(false);
			connection.setDoOutput(true);
			output = new BufferedOutputStream(connection.getOutputStream());
			output.write(content.getBytes());
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
}
