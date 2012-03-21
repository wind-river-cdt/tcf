/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tests.tcf.filesystem.url;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.tcf.te.tests.tcf.TcfTestCase;

public class TcfURLTests extends TcfTestCase {

	public void testWinURLFormat() throws MalformedURLException {
		String string = "tcf:/TCP:127.0.0.1:1534/C:/temp/hello.txt"; //$NON-NLS-1$
		URL url = new URL(string);
		assertEquals("tcf", url.getProtocol()); //$NON-NLS-1$
		assertEquals("TCP:127.0.0.1:1534", url.getHost()); //$NON-NLS-1$
		assertEquals("C:/temp/hello.txt", url.getPath()); //$NON-NLS-1$
	}

	@SuppressWarnings("unused")
    public void testWinURLFormatNegative() {
		try {
			String string = "tcf:/TCP:127.0.0.1:1534/C:/hello:txt"; //$NON-NLS-1$
			new URL(string);
			assertTrue("FAILED", false); //$NON-NLS-1$
		}
		catch (MalformedURLException e) {
		}
	}

	public void testUnixURLFormat() throws MalformedURLException {
		String string = "tcf:/TCP:127.0.0.1:1534/folk/wchen/hello.txt"; //$NON-NLS-1$
		URL url = new URL(string);
		assertEquals("tcf", url.getProtocol()); //$NON-NLS-1$
		assertEquals("TCP:127.0.0.1:1534", url.getHost()); //$NON-NLS-1$
		assertEquals("/folk/wchen/hello.txt", url.getPath()); //$NON-NLS-1$
	}

	@SuppressWarnings("unused")
    public void testUnixURLFormatNegative() {
		try {
			String string = "tcf://TCP:127.0.0.1:1534/folk/wchen/hello.txt"; //$NON-NLS-1$
			new URL(string);
			assertTrue("FAILED", false); //$NON-NLS-1$
		}
		catch (MalformedURLException e) {
		}
	}

	public void testUDPURLFormat() throws MalformedURLException {
		String string = "tcf:/UDP:127.0.0.1:1534/C:/temp/hello.txt"; //$NON-NLS-1$
		URL url = new URL(string);
		assertEquals("tcf", url.getProtocol()); //$NON-NLS-1$
		assertEquals("UDP:127.0.0.1:1534", url.getHost()); //$NON-NLS-1$
		assertEquals("C:/temp/hello.txt", url.getPath()); //$NON-NLS-1$
	}
}
