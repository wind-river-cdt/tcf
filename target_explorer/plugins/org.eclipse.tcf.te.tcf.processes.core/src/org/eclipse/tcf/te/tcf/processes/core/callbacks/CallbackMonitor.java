/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.core.callbacks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.tcf.processes.core.activator.CoreBundleActivator;
import org.eclipse.tcf.te.tcf.processes.core.nls.Messages;

/**
 * A helper class used to synchronize multiple threads. It is used
 * to join multiple threads which collaborate to create the pre-condition
 * of the callback code. 
 * <p>
 * A callback monitor maintains a map containing a set of locks. 
 * The collaborating threads should unlock one of its own lock in
 * it and wake up the callback if all the locks in the map is opened.
 * <p>
 * The following is an example:
 * <pre>
 * class Thread1 extends Thread {
 * 		CallbackMonitor monitor;
 * 		public Thread1(CallbackMonitor monitor){
 * 			this.monitor = monitor;
 * 		}
 * 		public void run() {
 * 			// Do the work
 * 			...
 * 			// Unlock this thread.
 * 			monitor.unlock(this)
 * 		}
 * }
 * class Thread2 extends Thread {
 * 		CallbackMonitor monitor;
 * 		public Thread2(CallbackMonitor monitor){
 * 			this.monitor = monitor;
 * 		}
 * 		public void run() {
 * 			// Do the work
 * 			...
 * 			// Unlock this thread.
 * 			monitor.unlock(this)
 * 		}
 * }
 * ...
 * public void collaborate() {
 * 		Runnable callback = new Runnable() {
 * 			public void run() {
 * 				// Do something which must be done after all the threads end.
 * 				...
 * 			}
 * 		};
 * 		CallbackMonitor monitor = new CallbackMonitor(callback);
 * 		Thread1 thread1 = new Thread1(monitor);
 * 		Thread2 thread2 = new Thread2(monitor);
 * 		...
 * 		monitor.lock(thread1, thread2, ...);
 * 		thread1.start();
 * 		thread2.start();
 * 		...
 * }
 * </pre>
 * <p>
 * The above creates multiple threads which lock on the monitor and
 * invoke unlock when they end. The keys they used can be anything which
 * are unique among the threads. Once all threads end, the callback defined
 * in the method will be invoked and do the thing which requires to be done
 * after the end of these threads.
 * <p>
 * <b>Note:</b><em>The threads which require collaboration on the callback 
 * monitor should be started only after all the locks corresponding to them 
 * are added. </em>
 * <p>
 * For example, the above threads are started after the monitor locks all the threads:
 * <pre>
 * 		monitor.lock(thread1, thread2, ...);
 * 		thread1.start();
 * 		thread2.start();
 * 		...
 * </pre>
 */
public class CallbackMonitor {
	// The callback which is invoked after all the locks are unlocked.
	private ICallback callback;
	// The lock map containing the keys and the corresponding running results.
	private Map<Object, IStatus> locks;
	
	/**
	 * Create a callback monitor with the specified callback.
	 * 
	 * @param callback The callback to be invoked after all the locks being unlocked.
	 */
	public CallbackMonitor(ICallback callback) {
		Assert.isNotNull(callback);
		this.callback = callback;
		this.locks = Collections.synchronizedMap(new HashMap<Object, IStatus>());
	}
	
	/**
	 * Create a callback monitor with the specified callback and the keys.
	 * 
	 * @param callback The callback to be invoked after all the locks being unlocked.
	 * @param keys The keys to lock and unlock the locks.
	 */
	public CallbackMonitor(ICallback callback, Object... keys) {
		Assert.isNotNull(callback);
		this.callback = callback;
		this.locks = Collections.synchronizedMap(new HashMap<Object, IStatus>());
		for (Object key : keys) {
			this.locks.put(key, null);
		}
	}
	
	/**
	 * Add multiple locks with the specified keys.
	 * 
	 * @param keys The keys whose locks are added.
	 */
	public synchronized void lock(Object... keys) {
		for(Object key : keys) {
			this.locks.put(key, null);
		}
	}
	
	/**
	 * Add a lock with the specified key.
	 * 
	 * @param key The key whose lock is added.
	 */
	public synchronized void lock(Object key) {
		Assert.isNotNull(key);
		this.locks.put(key, null);
	}

	/**
	 * Unlock the lock with the specified key and status 
	 * check if all the locks have been unlocked. If all the 
	 * locks have been unlocked, then invoke the callback.
	 * 
	 * @param key The key to unlock its lock.
	 */
	public synchronized void unlock(Object key, IStatus status) {
		Assert.isNotNull(key);
		Assert.isNotNull(status);
		locks.put(key, status);
		IStatus current = getCurrentStatus();
		if (current != null) {
			callback.done(this, current);
		}
	}

	/**
	 * Check if all the locks are unlocked and return a running status.
	 * 
	 * @return a MultiStatus object describing running result or null if not completed yet.
	 */
	private synchronized IStatus getCurrentStatus() {
		List<IStatus> list = new ArrayList<IStatus>();
		for (Entry<Object, IStatus>entry : locks.entrySet()) {
			IStatus status = entry.getValue();
			if (status == null) return null;
			list.add(status);
		}
		IStatus[] children = list.toArray(new IStatus[list.size()]); 
		return new MultiStatus(CoreBundleActivator.getUniqueIdentifier(), 0, children, Messages.CallbackMonitor_AllTasksFinished, null);
	}
}
