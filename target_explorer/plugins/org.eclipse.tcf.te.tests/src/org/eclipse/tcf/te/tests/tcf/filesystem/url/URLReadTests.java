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

public class URLReadTests extends URLTestBase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		writeFileContent("Hello, world!"); //$NON-NLS-1$
	}

	public void testReadFile() throws Exception {
		String content = readFileContent();
		assertEquals("Hello, world!", content); //$NON-NLS-1$
	}
}
