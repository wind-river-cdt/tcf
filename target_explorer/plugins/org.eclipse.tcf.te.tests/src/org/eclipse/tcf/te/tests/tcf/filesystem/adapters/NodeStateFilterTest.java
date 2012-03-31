/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tests.tcf.filesystem.adapters;

import org.eclipse.core.runtime.Platform;
import org.eclipse.tcf.te.tests.tcf.filesystem.FSPeerTestCase;
import org.eclipse.ui.IActionFilter;

public class NodeStateFilterTest extends FSPeerTestCase {
	public void testCacheState() {
		IActionFilter filter = (IActionFilter) Platform.getAdapterManager().getAdapter(testFile, IActionFilter.class);
		assertNotNull(filter);
		assertFalse(filter.testAttribute(testFile, "cache.state", "consistent"));  //$NON-NLS-1$//$NON-NLS-2$
	}
	
	public void testEditorCut() {
		IActionFilter filter = (IActionFilter) Platform.getAdapterManager().getAdapter(testFile, IActionFilter.class);
		assertNotNull(filter);
		assertFalse(filter.testAttribute(testFile, "edit.cut", null));  //$NON-NLS-1$
	}
	
	public void testHidden() {
		IActionFilter filter = (IActionFilter) Platform.getAdapterManager().getAdapter(testFile, IActionFilter.class);
		assertNotNull(filter);
		assertFalse(filter.testAttribute(testFile, "hidden", "true"));  //$NON-NLS-1$ //$NON-NLS-2$
	}
}
