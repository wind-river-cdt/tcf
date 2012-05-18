/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.internal;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.tcf.te.ui.interfaces.ISearchMatcher;

public class WidthTreeSearcher extends AbstractSearcher{
	private Queue<TreePath> queue;
	// The searching job.
	private Job fSearchJob;
	public WidthTreeSearcher(TreeViewer viewer){
		super(viewer);
		this.queue = new ConcurrentLinkedQueue<TreePath>();
	}
	@Override
	public void startSearch(TreePath start) {
		queue.offer(start);
	}
	
	@Override
    protected TreePath searchNode(boolean forward, ISearchMatcher matcher, IProgressMonitor monitor) {
		TreePath result = null;
		while(!queue.isEmpty() && result == null && !monitor.isCanceled()) {
			TreePath path = queue.poll();
			Object element = path.getLastSegment();
			Object[] children = getUpdatedChildren(element, monitor);
			if(children != null && children.length > 0) {
				for(Object child : children) {
					TreePath childPath = path.createChildPath(child);
					queue.offer(childPath);
				}
			}
			reportProgress(element, monitor);
			if(matcher.match(element)) {
				result = path;
			}
		}
	    return result;
    }
}
