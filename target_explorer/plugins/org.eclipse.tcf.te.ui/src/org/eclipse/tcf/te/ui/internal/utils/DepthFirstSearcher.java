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
import java.util.LinkedList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.tcf.te.ui.interfaces.ISearchMatcher;
import org.eclipse.tcf.te.ui.interfaces.ISearchable;

/**
 * The search engine which uses DFS(depth-first search) algorithm
 * to search elements that matches a specified matcher.
 */
public class DepthFirstSearcher extends AbstractSearcher {
	private static final int START_INDEX = -1;
	private static final int END_INDEX = -2;

	// A stack element used to store the current context and the children index
	private class StackElement {
		Object node;
		int index;

		public StackElement(Object node, int index) {
			this.node = node;
			this.index = index;
		}
	}
	// The searching stack in which searching contexts are stored.
	private LinkedList<StackElement> fSearchStack;
	// The searching direction.
	private boolean fForeward;

	/**
	 * Create a depth-first searcher with the specified viewer and a
	 * matcher.
	 * 
	 * @param viewer The tree viewer.
	 * @param matcher The search matcher used match a single tree node.
	 */
	public DepthFirstSearcher(TreeViewer viewer, ISearchable searchable) {
		super(viewer, searchable);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ITreeSearcher#setStartPath(org.eclipse.jface.viewers.TreePath)
	 */
	@Override
    public void setStartPath(TreePath path) {
		fSearchStack = new LinkedList<StackElement>();
		if (path == null) {
			Object obj = fViewer.getInput();
			path = new TreePath(new Object[] { obj });
		}
		initSearchContext(path);
	}
	
	/**
	 * Set the searching direction.
	 * 
	 * @param foreward searching direction.
	 */
	public void setForeward(boolean foreward) {
		fForeward = foreward;
	}

	/**
	 * Populate the stacks with initial path.
	 * 
	 * @param start The initial path.
	 */
	private void initSearchContext(TreePath start) {
		int count = start.getSegmentCount();
		for (int i = 0; i < count; i++) {
			Object element = start.getSegment(i);
			//Push a stack element with initial index as START_INDEX.
			fSearchStack.addLast(new StackElement(element, START_INDEX));
			if (i > 0) {
				IProgressMonitor monitor = new NullProgressMonitor();
				Object parent = start.getSegment(i-1);
                Object[] children = getUpdatedChildren(parent, monitor);
				for (int j = 0; j < children.length; j++) {
					if (children[j] == element) {
						StackElement parentStack = fSearchStack.get(i - 1);
						//Assign the stack element's index with element index in the children list.
						parentStack.index = j;
						break;
					}
				}
			}
		}
	}

	/**
	 * Search the tree using a matcher using DFS algorithm.
	 * 
	 * @param monitor The monitor reporting the progress.
	 * @return The tree path whose leaf node satisfies the searching rule.
	 */
	@Override
    public TreePath searchNext(IProgressMonitor monitor)  throws InvocationTargetException, InterruptedException {
		TreePath result = null;
		ISearchMatcher matcher = fSearchable.getMatcher();
		while (!fSearchStack.isEmpty() && result == null && !monitor.isCanceled()) { //Search util the stack is empty or the result is found.
			StackElement top = fSearchStack.getLast(); //Get the top stack element.
			if(!fForeward && top.index == END_INDEX || fForeward && top.index == START_INDEX){
				String elementText = fSearchable.getElementText(top.node);
				monitor.subTask(elementText);
				result = matcher.match(top.node) ? this.createContextPath() : null;
			}
			if (top.index == END_INDEX) {//If the top index is END_INDEX, it means the node has been finished.
				fSearchStack.removeLast(); //Then discard it.
			} else {
				Object[] children = getUpdatedChildren(top.node, monitor);//Get current node's children.
				if (children != null && children.length > 0) {//If there are some children.
					if(fForeward && top.index == children.length-1 || !fForeward && top.index == 0){
						//If this is the last index
						top.index = END_INDEX;
					}else{
						//Increase or decrease the index according to the direction.
						if(top.index == START_INDEX)
							top.index = fForeward ? 0 : children.length - 1;
						else
							top.index = fForeward ? top.index + 1 : top.index - 1;
						//Push the child at the index with START_INDEX into the stack.
						fSearchStack.addLast(new StackElement(children[top.index], START_INDEX));
					}
				} else {//If there's no children.
					top.index = END_INDEX;//Assign the index with END_INDEX.
				}
			}
		}
		if(monitor.isCanceled()) {
			throw new InterruptedException();
		}
		return result;
	}
	
	/**
	 * Create a path using the current elements of the stack.
	 * 
	 * @return The tree path representing the path of the stack.
	 */
	private TreePath createContextPath() {
		StackElement[] contexts = new StackElement[fSearchStack.size()];
		fSearchStack.toArray(contexts);
		Object[] elements = new Object[contexts.length];
		for (int i = 0; i < contexts.length; i++) {
			elements[i] = contexts[i].node;
		}
		return new TreePath(elements);
	}
}
