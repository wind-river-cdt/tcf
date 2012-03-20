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

public class URLWriteTests extends URLTestBase {
	public void testWriteFile() throws Exception {
		writeFileContent("Test writing");
		String content = readFileContent();
		assertEquals("Test writing", content);
	}
}
