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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.tcf.te.tests.CoreTestCase;

public class TcfURLTests extends CoreTestCase {

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
	
	@SuppressWarnings("unused")
    public void testSpaceInURINegative() {
		try{
			String string = "tcf:/TCP:127.0.0.1:1534/C:/Documents and Settings/hello.txt"; //$NON-NLS-1$
			new URI(string);
			assertTrue("FAILED", false); //$NON-NLS-1$
		}catch(URISyntaxException e){
			// Right and Ignore
		}
	}
	
	public void testSpaceInURL() throws MalformedURLException, URISyntaxException {
		String string = "tcf:/TCP:127.0.0.1:1534/C:/Documents and Settings/hello.txt";  //$NON-NLS-1$
		URL url = new URL(string);
		assertEquals("tcf", url.getProtocol()); //$NON-NLS-1$
		assertEquals("TCP:127.0.0.1:1534", url.getHost()); //$NON-NLS-1$
		assertEquals("C:/Documents and Settings/hello.txt", url.getPath()); //$NON-NLS-1$
	}
	
	public void testURLtoURI() throws MalformedURLException, URISyntaxException {
		String string = "tcf:/TCP:127.0.0.1:1534/C:/Documents and Settings/hello.txt";  //$NON-NLS-1$
		URL url = new URL(string);
		URI uri = url.toURI();
		assertEquals("tcf:/TCP:127.0.0.1:1534/C:/Documents+and+Settings/hello.txt", uri.toString()); //$NON-NLS-1$
	}
	
	public void testURItoURL() throws MalformedURLException, URISyntaxException {
		String string = "tcf:/TCP:127.0.0.1:1534/C:/Documents+and+Settings/hello.txt";  //$NON-NLS-1$
		URI uri = new URI(string);
		URL url = uri.toURL();
		assertEquals("tcf", url.getProtocol()); //$NON-NLS-1$
		assertEquals("TCP:127.0.0.1:1534", url.getHost()); //$NON-NLS-1$
		assertEquals("C:/Documents and Settings/hello.txt", url.getPath()); //$NON-NLS-1$
	}
}
