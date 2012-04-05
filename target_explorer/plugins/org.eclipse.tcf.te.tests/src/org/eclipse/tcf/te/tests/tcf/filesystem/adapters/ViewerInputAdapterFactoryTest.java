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
import org.eclipse.tcf.te.core.interfaces.IViewerInput;
import org.eclipse.tcf.te.tests.tcf.filesystem.FSPeerTestCase;

public class ViewerInputAdapterFactoryTest extends FSPeerTestCase {
	public void testViewerInputAdapter() {
		IViewerInput input = (IViewerInput) Platform.getAdapterManager().getAdapter(peerModel, IViewerInput.class);
		assertNotNull(input);
	}
}
