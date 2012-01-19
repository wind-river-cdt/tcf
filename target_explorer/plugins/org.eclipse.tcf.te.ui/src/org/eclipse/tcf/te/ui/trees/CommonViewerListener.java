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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;

/**
 * CommonViewerListener listens to the property change event from Target Explorer's
 *  tree and update the viewer accordingly.
 */
public abstract class CommonViewerListener extends TimerTask implements IPropertyChangeListener {
	private static final long INTERVAL = 500;
	private static final long MAX_IMMEDIATE_INTERVAL = 1000;
	private static final Object NULL = new Object();
	// The common viewer of Target Explorer view.
	private TreeViewer viewer;
	// Last time that the property event was processed.
	private long lastTime = 0;
	// The timer that process the property events periodically.
	private Timer timer;
	// The current queued property event sources.	
	private Queue<Object> queue;

	/***
	 * Create an instance for the specified common viewer.
	 *
	 * @param viewer The common viewer from Target Explorer view.
	 */
	public CommonViewerListener(TreeViewer viewer) {
		this.viewer = viewer;
		this.timer = new Timer();
		this.timer.schedule(this, INTERVAL, INTERVAL);
		this.queue = new ConcurrentLinkedQueue<Object>();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
    public void propertyChange(final PropertyChangeEvent event) {
		long now = System.currentTimeMillis();
		Object object = event.getSource();
		if(object == null)
			object = NULL;
		queue.offer(object);
		if(now - lastTime > MAX_IMMEDIATE_INTERVAL) {
			run();
		}
    }

	/*
	 * (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		if (!queue.isEmpty()) {
			Object[] objects = queue.toArray();
			Object object = mergeObjects(objects);
			if (object instanceof List<?>) {
				List<?> list = (List<?>) object;
				if (list.size() == 1) {
					object = list.get(0);
					if(isRootObject(object)) {
						object = NULL;
					}
				}
				else {
					// If there are multiple root nodes, then select NULL as the final root.
					object = NULL;
				}
			}
			processObject(object);
			queue.clear();
			lastTime = System.currentTimeMillis();
		}
	}

	/**
	 * If the specified object is a root object;
	 * 
	 * @param object The object to be tested.
	 * @return true if it is root object.
	 */
	protected abstract boolean isRootObject(Object object);

	/**
	 * Merge the current objects into an ancestor object.
	 * 
	 * @param objects The objects to be merged.
	 * @return NULL or a list presenting the top objects.
	 */
	private Object mergeObjects(Object[] objects) {
		for (Object object : objects) {
			if (object == NULL) return NULL;
		}
		List<Object> list = Arrays.asList(objects);
		List<Object> result = new ArrayList<Object>();
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
	 * Judges if the node is an ancestor of the target node.
	 * 
	 * @param node The node to be tested.
	 * @param target The target node.
	 * @return true if the node is an ancestor of the target node.
	 */
	private boolean isAncestorOf(Object node, Object target) {
		if (target == null) return false;
		Object parent = getParent(target);
		if (parent == node) return true;
		return isAncestorOf(node, parent);
   }
	
	/**
	 * Get the element's parent object.
	 * 
	 * @param element The element
	 * @return The parent of the element.
	 */
	protected abstract Object getParent(Object element);

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
}
