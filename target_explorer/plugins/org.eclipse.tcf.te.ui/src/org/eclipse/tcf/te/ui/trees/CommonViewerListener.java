/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.trees;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.tcf.te.core.utils.Ancestor;

/**
 * CommonViewerListener listens to the property change event from the
 *  tree and update the viewer accordingly.
 */
class CommonViewerListener extends Ancestor<Object> implements PropertyChangeListener, IPropertyChangeListener {
	// The timer that process the property events periodically.
	private static Timer viewerTimer;
	static {
		viewerTimer = new Timer("Viewer_Refresher", true); //$NON-NLS-1$
	}
	// The interval of the refreshing timer.
	private static final long INTERVAL = 333;
	// Maximum delay before immediate refreshing.
	private static final long MAX_DELAY = 1000;
	// The NULL object stands for refreshing the whole tree.
	private static final Object NULL = new Object();
	// The tree viewer
	private TreeViewer viewer;
	// The current queued property event sources.
	private List<Object> queue;
	// The timer task to process the property events periodically.
	private TimerTask task;
	// The content provider
	ITreeContentProvider contentProvider;
	// The time of last run.
	long lastRun;

	/***
	 * Create an instance for the specified tree content provider.
	 *
	 * @param viewer The tree content provider.
	 */
	public CommonViewerListener(TreeViewer viewer, ITreeContentProvider contentProvider) {
		Assert.isNotNull(viewer);
		this.viewer = viewer;
		this.contentProvider = contentProvider;
		this.task = new TimerTask(){
			@Override
            public void run() {
				handleEvent(true);
            }};
		viewerTimer.schedule(this.task, INTERVAL, INTERVAL);
		this.queue = Collections.synchronizedList(new ArrayList<Object>());
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.core.utils.Ancestor#getParent(java.lang.Object)
	 */
	@Override
    protected Object getParent(Object element) {
		return contentProvider.getParent(element);
    }
	
	/*
	 * (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
    public void propertyChange(PropertyChangeEvent event) {
		processEvent(event);
    }
	
	/**
	 * Adding the event object into the queue and trigger the scheduling.
	 * 
	 * @param event The event object.
	 */
	private void processEvent(EventObject event) {
		Object object = event.getSource();
		Assert.isTrue(object != null);
		queue.add(object);
		viewerTimer.schedule(new TimerTask(){
			@Override
            public void run() {
				handleEvent(false);
            }}, 0);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
    public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
		processEvent(event);
    }

	/**
	 * Get and empty the queued objects.
	 * 
	 * @return The objects in current queue.
	 */
	Object[] emptyQueue() {
		synchronized (queue) {
			Object[] objects = queue.toArray();
			queue.clear();
			return objects;
		}
	}
	
	/**
	 * Check if it is ready for next run. If the time
	 * has expired, then mark last run time and return true.
	 * 
	 * @param scheduled if this processing is scheduled
	 * @return true if it is time.
	 */
	synchronized boolean checkReady(boolean scheduled) {
		if (scheduled || System.currentTimeMillis() - lastRun > MAX_DELAY) {
			lastRun = System.currentTimeMillis();
			return true;
		}
		return false;
	}

	/**
	 * Handle the current events in the event queue.
	 * 
	 * @param scheduled if this handling is scheduled.
	 */
	void handleEvent(boolean scheduled) {
		if (checkReady(scheduled)) {
			Object[] objects = emptyQueue();
			if (objects.length > 0) {
				List<Object> list = mergeObjects(objects);
				Object object = getRefreshRoot(list);
				processObject(object);
			}
		}
	}

	/**
	 * Get the refreshing root for the object list.
	 * 
	 * @param objects The objects to be refreshed.
	 * @return The root of these objects.
	 */
	private Object getRefreshRoot(List<Object> objects) {
		if (objects.isEmpty()) {
	    	return NULL;
	    }
	    else if (objects.size() == 1) {
	    	Object object = objects.get(0);
	    	if (contentProvider.getParent(object) == null) {
	    		return NULL;
	    	}
	    	return object;
	    }
	    else {
	    	// If there are multiple root nodes, then select NULL as the final root.
	    	Object object = getAncestor(objects);
			if (object == null) {
				return NULL;
			}
			return object;
	    }
    }

	/**
	 * Merge the current objects into an ancestor object.
	 *
	 * @param objects The objects to be merged.
	 * @return NULL or a list presenting the top objects.
	 */
	private List<Object> mergeObjects(Object[] objects) {
		for (Object object : objects) {
			if (object == NULL) {
				// If one object is NULL, then return NULL
				List<Object> result = new ArrayList<Object>();
				result.add(NULL);
				return result;
			}
		}
		// Remove duplicates.
		List<Object> list = Arrays.asList(objects);
		Set<Object> set = new HashSet<Object>(list);
		objects = set.toArray();
		list = Arrays.asList(objects);
		return getAncestors(list);
	}

	/**
	 * Process the object node.
	 *
	 * @param object The object to be processed.
	 */
	void processObject(final Object object) {
		Assert.isNotNull(object);
	    Tree tree = viewer.getTree();
	    if (!tree.isDisposed()) {
	    	Display display = tree.getDisplay();
	    	if (display.getThread() == Thread.currentThread()) {
	    		if (object != NULL) {
	    			viewer.refresh(object);
	    		}
	    		else {
	    			viewer.refresh();
	    		}
	    	}
	    	else {
	    		display.asyncExec(new Runnable() {
	    			@Override
	    			public void run() {
	    				processObject(object);
	    			}
	    		});
	    	}
	    }
    }

	/**
	 * Cancel the current task and the current timer.
	 */
	public void cancel() {
		task.cancel();
    }
}
