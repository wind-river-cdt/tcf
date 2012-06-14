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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.tcf.te.ui.activator.UIPlugin;
import org.eclipse.tcf.te.ui.interfaces.ISearchCallback;
import org.eclipse.tcf.te.ui.interfaces.ISearchable;
import org.eclipse.tcf.te.ui.nls.Messages;

/**
 * The search engine used to search a tree viewer.
 */
public class SearchEngine {
	// The search engine used search the tree
	AbstractSearcher fSearcher;
	// The searching job.
	Job fSearchJob;
	// The viewer being searched.
	TreeViewer fViewer;
	// If the search algorithm is depth preferable
	boolean fDepthFirst;
	// The search matcher used to match tree nodes during traversing.
	ISearchable fSearchable;
	// The current starting path of the searcher engine.
	TreePath fStartPath;
	// Whether it is wrap search.
	boolean fWrap;
	// The last result being searched.
	TreePath fLastResult;

	/**
	 * Create an instance for the tree viewer.
	 * 
	 * @param viewer The tree viewer.
	 * @param depthFirst 
	 */
	public SearchEngine(TreeViewer viewer, boolean depthFirst) {
		fViewer = viewer;
		fDepthFirst = depthFirst;
	}
	
	/**
	 * The set the searchable
	 * 
	 * @param searchable the searchable element.
	 */
	public void setSearchable(ISearchable searchable) {
		fSearchable = searchable;
		fSearcher = fDepthFirst ? new DepthFirstSearcher(fViewer, fSearchable) : new BreadthFirstSearcher(fViewer, fSearchable);
	}

	/**
	 * If the current algorithm is DFS.
	 * 
	 * @return true if it is DFS.
	 */
	public boolean isDepthFirst() {
		return fDepthFirst;
	}

	/**
	 * Set the algorithm to depth-first according the boolean.
	 * 
	 * @param depthFirst
	 */
	public void setDepthFirst(boolean depthFirst) {
		if (fDepthFirst != depthFirst) {
			fDepthFirst = depthFirst;
			fSearcher = fDepthFirst ? new DepthFirstSearcher(fViewer, fSearchable) : new BreadthFirstSearcher(fViewer, fSearchable);
		}
	}

	/**
	 * Set the initial searching path.
	 * 
	 * @param path The initial searching path.
	 */
	public void setStartPath(TreePath path) {
		fStartPath = path;
		if (fSearcher != null) {
			fSearcher.setStartPath(path);
		}
	}
	
	/**
	 * Get the start path.
	 * 
	 * @return the start path.
	 */
	public TreePath getStartPath() {
		return fStartPath;
	}
	
	/**
	 * If the current searching scope is all.
	 */
	public boolean isScopeAll() {
		if(fStartPath == null) return true;
		Object element = fStartPath.getLastSegment();
		return element == fViewer.getInput();
	}

	/**
	 * Reset the searching path.
	 */
	public void resetPath() {
		if (fSearcher != null) {
			fSearcher.setStartPath(fStartPath);
		}
	}

	/**
	 * Clean up the searching job.
	 */
	public void endSearch() {
		if (fSearchJob != null) {
			fSearchJob.cancel();
			fSearchJob = null;
		}
	}

	/**
	 * Start searching using the progress monitor part and invoke the callback
	 * after a node is found.
	 * 
	 * @param callback The callback to invoked.
	 * @param pmpart The progress monitor part.
	 */
	public void startSearch(final ISearchCallback callback, final ProgressMonitorPart pmpart) {
		final TreePath[] result = new TreePath[1];
		fSearchJob = new Job(Messages.TreeViewerSearchDialog_JobName) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor = new DelegateProgressMonitor(monitor, pmpart);
				monitor.beginTask(Messages.TreeViewerSearchDialog_MainTaskName, IProgressMonitor.UNKNOWN);
				try {
					result[0] = fSearcher.searchNext(monitor);
				}
				catch (InvocationTargetException e) {
					Status status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), e.getMessage(), e);
					return status;
				}
				catch (InterruptedException e) {
					return Status.CANCEL_STATUS;
				}
				finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		fSearchJob.setSystem(true);
		fSearchJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(final IJobChangeEvent event) {
				fViewer.getTree().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						IStatus status = event.getResult();
						TreePath treePath = result[0];
						if (callback != null) callback.searchDone(status, treePath);
						searchDone(status, treePath);
						fSearchJob = null;
						if (status.isOK() && treePath == null && fWrap && fLastResult != null) {
							fLastResult = null;
							startSearch(callback, pmpart);
						}
					}
				});
			}
		});
		fSearchJob.schedule();
	}

	/**
	 * Set the searching direction, only valid for DFS.
	 * 
	 * @param foreward The searching direction.
	 */
	public void setForeward(boolean foreward) {
		if (fDepthFirst) {
			((DepthFirstSearcher) fSearcher).setForeward(foreward);
		}
	}

	/**
	 * The method called after the searching is done.
	 * 
	 * @param status The searching result status.
	 * @param path The searching result, or null if no path is found.
	 */
	void searchDone(IStatus status, TreePath path) {
		if (status.isOK()) {
			if (path != null) {
				fLastResult = path;
				fViewer.expandToLevel(path, 0);
				fViewer.setSelection(new StructuredSelection(new Object[] { path }), true);
			}
			else if (fWrap && fLastResult != null) {
				setStartPath(fLastResult);
			}
		}
	}

	/**
	 * If the current search is wrap search.
	 * 
	 * @return true if it is wrap search.
	 */
	public boolean isWrap() {
		return fWrap;
	}

	/**
	 * Set the current searching to wrap search.
	 * 
	 * @param w 
	 */
	public void setWrap(boolean w) {
		fWrap = w;
	}

	/**
	 * Set the last searching result.
	 * 
	 * @param path The last searched tree path.
	 */
	public void setLastResult(TreePath path) {
		fLastResult = path;
	}

	/**
	 * Get the last searched path.
	 * 
	 * @return The last searched path.
	 */
	public TreePath getLastResult() {
		return fLastResult;
	}
}
