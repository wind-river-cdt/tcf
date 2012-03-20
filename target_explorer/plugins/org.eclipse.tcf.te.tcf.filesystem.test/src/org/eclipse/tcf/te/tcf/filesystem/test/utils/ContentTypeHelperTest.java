/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.test.utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.tcf.te.runtime.utils.Host;
import org.eclipse.tcf.te.tcf.filesystem.internal.utils.CacheManager;
import org.eclipse.tcf.te.tcf.filesystem.internal.utils.ContentTypeHelper;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;

@SuppressWarnings("restriction")
public class ContentTypeHelperTest extends UtilsTestBase {
	private FSTreeNode agentNode;
	@Override
    protected void setUp() throws Exception {
	    super.setUp();
		uploadAgent();
		prepareXmlFile();
    }

	private void prepareXmlFile() throws IOException {
	    StringBuilder content = new StringBuilder();
		content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		content.append("<root></root>\n");
		writeFileContent(content.toString());
    }
	
    private void uploadAgent() throws MalformedURLException, IOException {
	    IPath path = getAgentLocation();
		path = path.append("agent"); //$NON-NLS-1$
		if (Host.isWindowsHost()) path = path.addFileExtension("exe"); //$NON-NLS-1$
		assertTrue("Invalid agent location: " + path.toString(), path.toFile().isFile()); //$NON-NLS-1$
	    File agentFile = path.toFile();
	    String agentPath = getTestRoot() + getPathSep() + agentFile.getName();
	    agentNode = getFSNode(agentPath);
		if (agentNode == null) {
			URL rootURL = testRoot.getLocationURL();
			URL agentURL = new URL(rootURL, agentFile.getName());
			CacheManager.getInstance().uploadFiles(getProgressMonitor(), new File[] { agentFile }, new URL[] { agentURL }, null);
			agentNode = getFSNode(agentPath);
			assertNotNull(agentNode);
		}
    }
	
	public void testBinaryFile() {
		assertTrue(ContentTypeHelper.getInstance().isBinaryFile(agentNode));
	}
	
	public void testContentType() {
		IContentType contentType = ContentTypeHelper.getInstance().getContentType(testFile);
		assertEquals("Text", contentType.getName());
	}
}
