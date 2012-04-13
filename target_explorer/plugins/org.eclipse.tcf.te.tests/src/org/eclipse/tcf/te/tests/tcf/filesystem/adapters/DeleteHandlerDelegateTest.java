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
import org.eclipse.tcf.te.tcf.filesystem.core.interfaces.IConfirmCallback;
import org.eclipse.tcf.te.tcf.filesystem.ui.internal.adapters.DeleteHandlerDelegate;
import org.eclipse.tcf.te.tests.tcf.filesystem.FSPeerTestCase;
import org.eclipse.tcf.te.ui.views.interfaces.handler.IDeleteHandlerDelegate;

@SuppressWarnings("restriction")
public class DeleteHandlerDelegateTest extends FSPeerTestCase {
	private IDeleteHandlerDelegate delegate;

    @Override
	protected void setUp() throws Exception {
		super.setUp();
		delegate = (IDeleteHandlerDelegate) Platform.getAdapterManager().getAdapter(test11File, IDeleteHandlerDelegate.class);
		assertNotNull(delegate);
		IConfirmCallback confirmCallback = new IConfirmCallback() {
			@Override
			public boolean requires(Object object) {
				// Do not require confirmation, just delete it.
				return false;
			}

			@Override
			public int confirms(Object object) {
				return 0;
			}
		};
		((DeleteHandlerDelegate) delegate).setConfirmCallback(confirmCallback);
	}

	public void testCanDelete() {
		assertTrue(delegate.canDelete(test11File));
	}

	/*
	public void testDelete() throws Exception {
		final AtomicReference<IStatus> ref = new AtomicReference<IStatus>();
		PropertiesContainer props = new PropertiesContainer();
		props.setProperty("selection", new StructuredSelection(test11File)); //$NON-NLS-1$
		delegate.delete(test11File, props, new Callback() {
			@Override
			protected void internalDone(Object caller, IStatus status) {
				ref.set(status);
			}
		});
		IStatus status = ref.get();
		assertTrue(status.isOK());
		assertNull(status.getException());
		String location = test11File.getLocation();
		test11File = getFSNode(location);
		assertNull(test11File);
	}
	*/
}
