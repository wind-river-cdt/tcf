/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.internal.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.tcf.te.ui.interfaces.ISearchMatcher;

/**
 * The search engine which uses BFS(breadth-first search) algorithm
 * to search elements that matches a specified matcher.
 */
public class BreadthFirstSearcher extends AbstractSearcher{
	// The queue to pre-populate the nodes to be matched
	private Queue<TreePath> queue;

	/**
	 * Create a breadth-first searcher with the specified viewer and a
	 * matcher.
	 *
	 * @param viewer The tree viewer.
	 * @param matcher The search matcher used match a single tree node.
	 */
	public BreadthFirstSearcher(TreeViewer viewer, ISearchMatcher matcher){
		super(viewer, matcher);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ITreeSearcher#setStartPath(org.eclipse.jface.viewers.TreePath)
	 */
	@Override
    public void setStartPath(TreePath path) {
		this.queue = new ConcurrentLinkedQueue<TreePath>();
		Assert.isTrue(this.queue.offer(path));
	}

	/**
	 * Search the tree using a matcher using BFS algorithm.
	 *
	 * @param monitor The monitor reporting the progress.
	 * @return The tree path whose leaf node satisfies the searching rule.
	 */
	@Override
    public TreePath searchNext(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException{
		TreePath result = null;
		Assert.isNotNull(queue);
		while(!queue.isEmpty() && result == null && !monitor.isCanceled()) {
			TreePath path = queue.poll();
			Object element = path.getLastSegment();
			Object[] children = getUpdatedChildren(element, monitor);
			if(children != null && children.length > 0) {
				for(Object child : children) {
					TreePath childPath = path.createChildPath(child);
					Assert.isTrue(queue.offer(childPath));
				}
			}
			String elementText = getElementText(element);
			monitor.subTask(elementText);
			if(fMatcher.match(element)) {
				result = path;
			}
		}
		if(monitor.isCanceled()) {
			throw new InterruptedException();
		}
	    return result;
    }
}
