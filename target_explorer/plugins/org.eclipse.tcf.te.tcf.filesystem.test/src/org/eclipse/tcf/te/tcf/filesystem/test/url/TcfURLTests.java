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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.tcf.te.tcf.filesystem.test.FSTestCase;

public class TcfURLTests extends FSTestCase {

	public void testWinURLFormat() throws MalformedURLException {
		String string = "tcf:/TCP:127.0.0.1:1534/C:/temp/hello.txt";
		URL url = new URL(string);
		assertEquals("tcf", url.getProtocol());
		assertEquals("TCP:127.0.0.1:1534", url.getHost());
		assertEquals("C:/temp/hello.txt", url.getPath());
	}

	public void testWinURLFormatNegative() {
		try {
			String string = "tcf:/TCP:127.0.0.1:1534/C:/hello:txt";
			new URL(string);
			assertTrue("FAILED", false);
		}
		catch (MalformedURLException e) {
		}
	}

	public void testUnixURLFormat() throws MalformedURLException {
		String string = "tcf:/TCP:127.0.0.1:1534/folk/wchen/hello.txt";
		URL url = new URL(string);
		assertEquals("tcf", url.getProtocol());
		assertEquals("TCP:127.0.0.1:1534", url.getHost());
		assertEquals("/folk/wchen/hello.txt", url.getPath());
	}

	public void testUnixURLFormatNegative() {
		try {
			String string = "tcf://TCP:127.0.0.1:1534/folk/wchen/hello.txt";
			new URL(string);
			assertTrue("FAILED", false);
		}
		catch (MalformedURLException e) {
		}
	}

	public void testUDPURLFormat() throws MalformedURLException {
		String string = "tcf:/UDP:127.0.0.1:1534/C:/temp/hello.txt";
		URL url = new URL(string);
		assertEquals("tcf", url.getProtocol());
		assertEquals("UDP:127.0.0.1:1534", url.getHost());
		assertEquals("C:/temp/hello.txt", url.getPath());
	}
}
