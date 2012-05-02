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
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.tcf.te.tests.tcf.filesystem.FSPeerTestCase;
import org.eclipse.tcf.te.ui.views.interfaces.handler.IRefreshHandlerDelegate;
import org.eclipse.ui.IActionFilter;

public class FSTreeNodeAdapterFactoryTest extends FSPeerTestCase {
	public void testActionFilterAdapter() {
		IActionFilter filter = (IActionFilter) Platform.getAdapterManager().getAdapter(testFile, IActionFilter.class);
		assertNotNull(filter);
	}

	public void testLabelProviderAdapter() {
		ILabelProvider labelProvider = (ILabelProvider) Platform.getAdapterManager().getAdapter(testFile, ILabelProvider.class);
		assertNotNull(labelProvider);
	}

	public void testRefreshHandlerDelegateAdapter() {
		IRefreshHandlerDelegate delegate = (IRefreshHandlerDelegate) Platform.getAdapterManager().getAdapter(testFolder, IRefreshHandlerDelegate.class);
		assertNotNull(delegate);
	}

}
