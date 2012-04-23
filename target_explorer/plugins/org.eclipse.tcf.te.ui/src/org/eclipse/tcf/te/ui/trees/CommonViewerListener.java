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

/**
 * CommonViewerListener listens to the property change event from the
 *  tree and update the viewer accordingly.
 */
class CommonViewerListener implements PropertyChangeListener, IPropertyChangeListener {
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
	// The content provider
	private ITreeContentProvider contentProvider;
	// The current queued property event sources.
	private List<Object> queue;
	// The timer task to process the property events periodically.
	private TimerTask task;
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
	    	if (getParent(object) == null) {
	    		return NULL;
	    	}
	    	return object;
	    }
	    else {
	    	// If there are multiple root nodes, then select NULL as the final root.
			Object object = getCommonAncestor(objects);
			if (object == null) {
				return NULL;
			}
			return object;
	    }
    }

	/**
	 * Get a object which is the common ancestor of the specified objects.
	 *
	 * @param objects The object list.
	 * @return The common ancestor.
	 */
	private Object getCommonAncestor(List<Object> objects) {
		Assert.isTrue(objects.size() > 1);
		Object object1 = objects.get(0);
		for (int i = 1; i < objects.size(); i++) {
			Object object2 = objects.get(i);
			object1 = getCommonAncestor(object1, object2);
			if (object1 == null) return null;
		}
		return object1;
	}

	/**
	 * Get the common ancestor of the specified two objects.
	 *
	 * @param object1 The first object.
	 * @param object2 The second object.
	 * @return The common ancestor.
	 */
	private Object getCommonAncestor(Object object1, Object object2) {
		Assert.isNotNull(object1);
		Assert.isNotNull(object2);
		if (isAncestorOf(object1, object2)) {
			return object1;
		}
		if (isAncestorOf(object2, object1)) {
			return object2;
		}
		Object ancestor = null;
		Object parent1 = getParent(object1);
		if(parent1 != null) {
			ancestor = getCommonAncestor(parent1, object2);
		}
		if(ancestor != null) return ancestor;
		Object parent2 = getParent(object2);
		if(parent2 != null) {
			ancestor = getCommonAncestor(object1, parent2);
		}
		if(ancestor != null) return ancestor;
		if(parent1 != null && parent2 != null) {
			ancestor = getCommonAncestor(parent1, parent2);
		}
		return ancestor;
	}

	/**
	 * Merge the current objects into an ancestor object.
	 *
	 * @param objects The objects to be merged.
	 * @return NULL or a list presenting the top objects.
	 */
	private List<Object> mergeObjects(Object[] objects) {
		// If one object is NULL, then return NULL
		List<Object> result = new ArrayList<Object>();
		for (Object object : objects) {
			if (object == NULL) {
				result.add(NULL);
				return result;
			}
		}
		// Remove duplicates.
		List<Object> list = Arrays.asList(objects);
		Set<Object> set = new HashSet<Object>(list);
		objects = set.toArray();

		list = Arrays.asList(objects);
		for (Object object : list) {
			if (!hasAncestor(object, list)) {
				result.add(object);
			}
		}
		return result;
	}

	/**
	 * If the target node has ancestor in the specified node list.
	 *
	 * @param target The node to be tested.
	 * @param nodes The node list to search in.
	 * @return true if the target node has an ancestor in the node list.
	 */
	private boolean hasAncestor(Object target, List<Object> nodes) {
		for (Object node : nodes) {
			if (isAncestorOf(node, target)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Judges if the object1 is an ancestor of the object2.
	 *
	 * @param object1 The first object to be tested.
	 * @param object2 The second object to be tested.
	 * @return true if the first object is the ancestor of the second object2.
	 */
	private boolean isAncestorOf(Object object1, Object object2) {
		if (object2 == null) return false;
		Object parent = getParent(object2);
		if (parent == object1) return true;
		return isAncestorOf(object1, parent);
    }
	
	/**
	 * Get the parent of the specified object in the display thread.
	 * 
	 * @param object The object
	 * @return its parent.
	 */
	Object getParent(final Object object) {
		return contentProvider.getParent(object);
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
