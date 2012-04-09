/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tests.tcf.filesystem.testers;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.tcf.te.tcf.filesystem.ui.internal.testers.ClipboardPropertyTester;
import org.eclipse.tcf.te.tests.tcf.filesystem.FSPeerTestCase;

@SuppressWarnings("restriction")
public class ClipboardPropertyTesterTest extends FSPeerTestCase {
	public void testCanPaste() {
        ClipboardPropertyTester tester = new ClipboardPropertyTester();
		IStructuredSelection selection = new StructuredSelection(testFolder);
		assertFalse(tester.test(selection, "canPaste", null, null)); //$NON-NLS-1$
	}
}
