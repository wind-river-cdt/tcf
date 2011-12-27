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

import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.processes.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.processes.ui.internal.callbacks.QueryDoneOpenChannel;
import org.eclipse.tcf.te.tcf.processes.ui.internal.callbacks.RefreshDoneOpenChannel;
import org.eclipse.tcf.te.tcf.processes.ui.internal.preferences.IPreferenceConsts;
import org.eclipse.tcf.te.ui.interfaces.IViewerInput;

/**
 * The process tree model implementation.
 */
public class ProcessModel implements IPreferenceConsts{
	/* default */static final String PROCESS_ROOT_KEY = UIPlugin.getUniqueIdentifier() + ".process.root"; //$NON-NLS-1$

	/**
	 * Get the process model stored in the peer model.
	 * If there's no process model yet, create a new process model. 
	 * 
	 * @param peerModel The target's peer model.
	 * @return The process model representing the process.
	 */
	public static ProcessModel getProcessModel(final IPeerModel peerModel) {
		if (peerModel != null) {
			if (Protocol.isDispatchThread()) {
				ProcessModel model = (ProcessModel) peerModel.getProperty(PROCESS_ROOT_KEY);
				if (model == null) {
					model = new ProcessModel(peerModel);
					peerModel.setProperty(PROCESS_ROOT_KEY, model);
				}
				return model;
			}
			final AtomicReference<ProcessModel> reference = new AtomicReference<ProcessModel>();
			Protocol.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					reference.set(getProcessModel(peerModel));
				}
			});
			return reference.get();
		}
		return null;
	}
	
	// The root node of the peer model
	private ProcessTreeNode root;
	// The polling interval in seconds. If it is zero, then stop polling periodically.
	/* default */int interval;
	// The timer to schedule polling task.
	/* default */Timer pollingTimer;
	// The flag to indicate if the polling has been stopped.
	/* default */boolean stopped;
	private IPeerModel peerModel;

	/**
	 * Create a File System Model.
	 */
	ProcessModel(IPeerModel peerModel) {
		this.peerModel = peerModel;
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

	/**
	 * Start the periodical polling.
	 */
	public void startPolling() {
	    setStopped(false);
	    pollingTimer = new Timer();
		schedulePolling();
    }

	/**
	 * Set the status of the polling and 
	 * fire a property change event.
	 * 
	 * @param stopped if the polling should be stopped.
	 */
	void setStopped(boolean stopped) {
		if(this.stopped != stopped) {
			boolean old = this.stopped;
			this.stopped = stopped;
			Boolean oldValue = Boolean.valueOf(old);
			Boolean newValue = Boolean.valueOf(stopped);
			PropertyChangeEvent event = new PropertyChangeEvent(peerModel, "stopped", oldValue, newValue); //$NON-NLS-1$
			IViewerInput viewerInput = (IViewerInput) peerModel.getAdapter(IViewerInput.class);
			viewerInput.firePropertyChange(event);
		}
    }

	/**
	 * Stop the periodical polling.
	 */
	public void stopPolling() {
		setStopped(true);
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
	    if(interval == 0) {
	    	// Interval has not yet been initialized.
	    	IPreferenceStore prefStore = UIPlugin.getDefault().getPreferenceStore();
	    	interval = prefStore.getInt(PREF_LAST_INTERVAL);
	    }
        pollingTimer.schedule(pollingTask, interval * 1000);
    }
	
	/**
	 * Set new interval. 
	 * 
	 * @param interval The new interval.
	 */
	public void setInterval(int interval) {
		Assert.isTrue(interval > 0);
		if (this.interval != interval) {
			this.interval = interval;
			IPreferenceStore prefStore = UIPlugin.getDefault().getPreferenceStore();
			prefStore.setValue(PREF_LAST_INTERVAL, interval);
		}
	}

	/**
	 * Get the current interval.
	 * 
	 * @return the current interval.
	 */
	public int getInterval() {
		return interval;
	}

	/**
	 * Query the children of the given process context.
	 * 
	 * @param parentNode The process context node. Must be not <code>null</code>.
	 */
	public void queryChildren(ProcessTreeNode parentNode) {
		Assert.isNotNull(parentNode);
		parentNode.childrenQueryRunning = true;
		Tcf.getChannelManager().openChannel(parentNode.peerNode.getPeer(), false, new QueryDoneOpenChannel(parentNode));
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
		Tcf.getChannelManager().openChannel(parentNode.peerNode.getPeer(), false, new RefreshDoneOpenChannel(callback, parentNode));
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
	
	/**
	 * Update the most recently  used interval adding 
	 * a new interval.
	 * 
	 * @param interval The new interval.
	 */
	public void addMRUInterval(int interval){
        IPreferenceStore prefStore = UIPlugin.getDefault().getPreferenceStore();
        String mruList = prefStore.getString(PREF_INTERVAL_MRU_LIST);
        if (mruList == null || mruList.trim().length() == 0) {
        	mruList = "" + interval; //$NON-NLS-1$
        }else{
        	StringTokenizer st = new StringTokenizer(mruList, ":"); //$NON-NLS-1$
        	int maxCount = prefStore.getInt(PREF_INTERVAL_MRU_COUNT);
        	boolean found = false;
        	while (st.hasMoreTokens()) {
        		String token = st.nextToken();
        		try {
        			int s = Integer.parseInt(token);
        			if(s == interval ) {
        				found = true;
        				break;
        			}
        		}
        		catch (NumberFormatException nfe) {
        		}
        	}
        	if(!found) {
        		mruList = mruList + ":" + interval; //$NON-NLS-1$
        		st = new StringTokenizer(mruList, ":"); //$NON-NLS-1$
        		if(st.countTokens() > maxCount) {
        			int comma = mruList.indexOf(":"); //$NON-NLS-1$
        			if(comma != -1) {
        				mruList = mruList.substring(comma+1);
        			}
        		}
        	}
        }
        prefStore.setValue(PREF_INTERVAL_MRU_LIST, mruList);
    }

	/**
	 * If the polling has been stopped.
	 * 
	 * @return true if it is stopped.
	 */
	public boolean isRefreshStopped() {
	    return stopped;
    }
	
	/**
	 * Get the peer model associated with this model.
	 * 
	 * @return The peer model.
	 */
	IPeerModel getPeerModel() {
		return peerModel;
	}
}	

