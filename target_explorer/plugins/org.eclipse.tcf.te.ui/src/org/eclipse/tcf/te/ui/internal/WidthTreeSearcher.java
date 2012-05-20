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

import java.lang.reflect.InvocationTargetException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.tcf.te.ui.interfaces.ISearchMatcher;

public class WidthTreeSearcher extends AbstractSearcher{
	private Queue<TreePath> queue;
	public WidthTreeSearcher(TreeViewer viewer, ISearchMatcher matcher){
		super(viewer, matcher);
	}

	@Override
    public void setStartPath(TreePath path) {
		this.queue = new ConcurrentLinkedQueue<TreePath>();
		this.queue.offer(path);
	}

	@Override
    public TreePath searchNext(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException{
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
			String elementText = getElementText(element);
			advance(elementText, monitor);
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
