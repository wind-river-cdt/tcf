/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.processes.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.processes.ui.interfaces.INodeStateListener;
import org.eclipse.tcf.te.tcf.processes.ui.internal.callbacks.QueryDoneOpenChannel;
import org.eclipse.tcf.te.tcf.processes.ui.internal.callbacks.RefreshDoneOpenChannel;
import org.eclipse.tcf.te.tcf.processes.ui.internal.preferences.IPreferenceConsts;

/**
 * The process tree model implementation.
 */
public class ProcessModel {
	// Node state listeners.
	private List<INodeStateListener> listeners;
	// Property Change Listeners
	private List<IPropertyChangeListener> propertyChangeListeners;
	// The root node of the peer model
	private ProcessTreeNode root;
	// The polling interval in seconds. If it is zero, then stop polling periodically.
	/* default */long interval = 5;
	// The timer to schedule polling task.
	/* default */Timer pollingTimer;
	// The flag to indicate if the polling has been stopped.
	/* default */boolean stopped;

	/**
	 * Create a File System Model.
	 */
	ProcessModel(IPeerModel peerModel) {
		this.listeners = Collections.synchronizedList(new ArrayList<INodeStateListener>());
		this.propertyChangeListeners = Collections.synchronizedList(new ArrayList<IPropertyChangeListener>());
	}

	/**
	 * Get the root node of the peer model.
	 * 
	 * @return The root node.
	 */
	public ProcessTreeNode getRoot() {
		return root;
	}

	/**
	 * Set the root node of the peer model.
	 * 
	 * @param root The root node
	 */
	public void createRoot(IPeerModel peerModel ) {
		ProcessTreeNode root = new ProcessTreeNode();
		root.type = "ProcRootNode"; //$NON-NLS-1$
		root.peerNode = peerModel;
		root.childrenQueried = false;
		root.childrenQueryRunning = false;
		this.root = root;
		startPolling();
	}

	public void startPolling() {
	    stopped = false;
	    pollingTimer = new Timer();
		schedulePolling();
    }
	
	public void stopPolling() {
		stopped = true;
	}

	/**
	 * Schedule the periodical polling.
	 */
	void schedulePolling() {
	    TimerTask pollingTask = new TimerTask(){
			@Override
	        public void run() {
				refresh(new Runnable() {
					@Override
					public void run() {
						if (!stopped) {
							schedulePolling();
						}
						else {
							pollingTimer.cancel();
							pollingTimer = null;
						}
					}
				});
	        }};
        pollingTimer.schedule(pollingTask, interval * 1000);
    }
	
	/**
	 * Set new interval. If the new interval is zero,
	 * then stop scheduling periodically.
	 * 
	 * @param interval The new interval.
	 */
	public void setInterval(long interval) {
		this.interval = interval;
	}

	public long getInterval() {
		return interval;
	}
	
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		if(!propertyChangeListeners.contains(listener)) {
			propertyChangeListeners.add(listener);
		}
	}
	
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		if(propertyChangeListeners.contains(listener)) {
			propertyChangeListeners.remove(listener);
		}
	}
	/**
	 * Add an INodeStateListener to the File System model if it is not in the listener list yet.
	 * 
	 * @param listener The INodeStateListener to be added.
	 */
	public void addNodeStateListener(INodeStateListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	/**
	 * Remove the INodeStateListener from the File System model if it exists in the listener list.
	 * 
	 * @param listener The INodeStateListener to be removed.
	 */
	public void removeNodeStateListener(INodeStateListener listener) {
		if (listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}

	/**
	 * Fire a node state changed event with the specified node.
	 * 
	 * @param node The node whose state has changed.
	 */
	public void fireNodeStateChanged(ProcessTreeNode node) {
		synchronized (listeners) {
			for (INodeStateListener listener : listeners) {
				listener.stateChanged(node);
			}
		}
	}
	
	/**
	 * Query the children of the given process context.
	 * 
	 * @param parentNode The process context node. Must be not <code>null</code>.
	 */
	public void queryChildren(ProcessTreeNode parentNode) {
		Assert.isNotNull(parentNode);
		parentNode.childrenQueryRunning = true;
		Tcf.getChannelManager().openChannel(parentNode.peerNode.getPeer(), false, new QueryDoneOpenChannel(this, parentNode));
	}

	/**
	 * Recursively refresh the children of the given process context with a callback, which is
	 * called when whole process is finished.
	 * 
	 * @param parentNode The process context node. Must be not <code>null</code>.
	 * @param callback The callback object, or <code>null</code> when callback is not needed.
	 */
	public void refresh(ProcessTreeNode parentNode, Runnable callback) {
		Assert.isNotNull(parentNode);
		parentNode.childrenQueryRunning = true;
		Tcf.getChannelManager().openChannel(parentNode.peerNode.getPeer(), false, new RefreshDoneOpenChannel(this, callback, parentNode));
	}

	/**
	 * Recursively refresh the children of the given process context.
	 * 
	 * @param parentNode The process context node. Must be not <code>null</code>.
	 */
	public void refresh(final ProcessTreeNode parentNode) {
		refresh(parentNode, null);
	}

	/**
	 * Recursively refresh the tree from the root node with a callback, which
	 * is called when the whole process is finished.
	 * 
	 * @param runnable The callback object or <code>null</code> when callback is not needed.
	 */
	public void refresh(Runnable runnable) {
		if (this.root.childrenQueried && !this.root.childrenQueryRunning) {
			refresh(this.root, runnable);
		}
		else {
			if (runnable != null) {
				runnable.run();
			}
		}
	}

	/**
	 * Recursively refresh the tree from the root node.
	 */
	public void refresh() {
		if(this.root.childrenQueried && !this.root.childrenQueryRunning) {
			refresh(this.root, null);
		}
	}
	
	public void updateMRU(int seconds) {
        IPreferenceStore prefStore = UIPlugin.getDefault().getPreferenceStore();
        String mruList = prefStore.getString(IPreferenceConsts.PREF_INTERVAL_MRU_LIST);
        if (mruList == null || mruList.trim().length() == 0) {
        	mruList = "" + seconds; //$NON-NLS-1$
        }else{
        	StringTokenizer st = new StringTokenizer(mruList, ":"); //$NON-NLS-1$
        	int maxCount = prefStore.getInt(IPreferenceConsts.PREF_INTERVAL_MRU_COUNT);
        	boolean found = false;
        	while (st.hasMoreTokens()) {
        		String token = st.nextToken();
        		try {
        			int s = Integer.parseInt(token);
        			if(s == seconds ) {
        				found = true;
        				break;
        			}
        		}
        		catch (NumberFormatException nfe) {
        		}
        	}
        	if(!found) {
        		mruList = mruList + ":" + seconds; //$NON-NLS-1$
        		st = new StringTokenizer(mruList, ":"); //$NON-NLS-1$
        		if(st.countTokens() > maxCount) {
        			int comma = mruList.indexOf(":"); //$NON-NLS-1$
        			if(comma != -1) {
        				mruList = mruList.substring(comma+1);
        			}
        		}
        	}
        }
        prefStore.setValue(IPreferenceConsts.PREF_INTERVAL_MRU_LIST, mruList);
    }

	public boolean isRefreshStopped() {
	    return stopped;
    }
}	

