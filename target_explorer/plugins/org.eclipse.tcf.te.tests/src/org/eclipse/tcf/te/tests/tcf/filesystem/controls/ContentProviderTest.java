package org.eclipse.tcf.te.tests.tcf.filesystem.controls;

import org.eclipse.tcf.te.tcf.filesystem.controls.FSNavigatorContentProvider;
import org.eclipse.tcf.te.tests.tcf.filesystem.FSPeerTestCase;

public class ContentProviderTest extends FSPeerTestCase {
	public void testGetChildren() {
		FSNavigatorContentProvider contentProvider = new FSNavigatorContentProvider();
		Object[] children = contentProvider.getChildren(testFolder);
		assertNotNull(children);
		assertTrue(children.length == 3);
	}
	public void testGetParent() {
		FSNavigatorContentProvider contentProvider = new FSNavigatorContentProvider();
		Object parent = contentProvider.getParent(testFile);
		assertEquals(parent, testFolder);
	}
	public void testHasChildren() {
		FSNavigatorContentProvider contentProvider = new FSNavigatorContentProvider();
		assertTrue(contentProvider.hasChildren(testFolder));
	}
}
