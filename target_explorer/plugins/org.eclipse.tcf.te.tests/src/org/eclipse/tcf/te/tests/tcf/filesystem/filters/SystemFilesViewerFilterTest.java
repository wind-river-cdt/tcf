/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tests.tcf.filesystem.filters;

import org.eclipse.tcf.te.runtime.utils.Host;
import org.eclipse.tcf.te.tcf.filesystem.filters.SystemFilesViewerFilter;
import org.eclipse.tcf.te.tests.tcf.filesystem.FSPeerTestCase;

public class SystemFilesViewerFilterTest extends FSPeerTestCase {
	public void testSelect() {
		if(Host.isWindowsHost()) {
			SystemFilesViewerFilter filter = new SystemFilesViewerFilter();
			assertTrue(filter.select(null, testFolder, testFile));
		}
	}
}
