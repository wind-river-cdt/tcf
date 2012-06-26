/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tests.tcf.processes.callbacks;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.tcf.te.runtime.callback.Callback;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.processes.core.callbacks.QueryDoneOpenChannel;
import org.eclipse.tcf.te.tcf.processes.core.callbacks.RefreshChildrenDoneOpenChannel;
import org.eclipse.tcf.te.tcf.processes.core.callbacks.RefreshDoneOpenChannel;
import org.eclipse.tcf.te.tests.tcf.processes.ProcessesTestCase;

public class QueryChildrenCallbackTest extends ProcessesTestCase {
	public void testQueryChildren() throws Exception {
		Assert.isNotNull(processRoot);
		processRoot.childrenQueryRunning = true;
		final AtomicReference<IStatus> statusRef = new AtomicReference<IStatus>();
		final Callback callback = new Callback(){
			@Override
            protected void internalDone(Object caller, IStatus status) {
				statusRef.set(status);
            }
		};
		Tcf.getChannelManager().openChannel(peer, null, new QueryDoneOpenChannel(processRoot,callback));
		waitAndDispatch(0, callback.getDoneConditionTester(new NullProgressMonitor()));
		assertTrue(statusRef.get().isOK());
	}
	
	public void testRefreshChildren() throws Exception {
		Assert.isNotNull(processRoot);
		processRoot.childrenQueryRunning = true;
		final AtomicReference<IStatus> statusRef = new AtomicReference<IStatus>();
		final Callback callback = new Callback(){
			@Override
            protected void internalDone(Object caller, IStatus status) {
				statusRef.set(status);
            }
		};
		Tcf.getChannelManager().openChannel(peer, null, new RefreshChildrenDoneOpenChannel(processRoot,callback));
		waitAndDispatch(0, callback.getDoneConditionTester(new NullProgressMonitor()));
		assertTrue(statusRef.get().isOK());
	}

	public void testRefresh() throws Exception {
		Assert.isNotNull(processRoot);
		processRoot.childrenQueryRunning = true;
		final AtomicReference<IStatus> statusRef = new AtomicReference<IStatus>();
		final Callback callback = new Callback(){
			@Override
            protected void internalDone(Object caller, IStatus status) {
				statusRef.set(status);
            }
		};
		Tcf.getChannelManager().openChannel(peer, null, new RefreshDoneOpenChannel(processRoot, callback));
		waitAndDispatch(0, callback.getDoneConditionTester(new NullProgressMonitor()));
		assertTrue(statusRef.get().isOK());
	}
}
