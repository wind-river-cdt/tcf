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
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.te.ui.interfaces.ILazyLoader;
import org.eclipse.tcf.te.ui.interfaces.ISearchMatcher;
import org.eclipse.tcf.te.ui.trees.Pending;

public abstract class AbstractSearcher implements ITreeSearcher {
	protected TreeViewer fViewer;
	private ILabelProvider fLabelProvider;
	// The searching job.
	protected Job fSearchJob;
	public AbstractSearcher(TreeViewer viewer) {
		fViewer = viewer;
		fLabelProvider = (ILabelProvider) fViewer.getLabelProvider();
	}

	abstract protected TreePath searchNode(boolean forward, ISearchMatcher matcher, IProgressMonitor monitor);

	protected void reportProgress(final Object element, final IProgressMonitor monitor) {
		if (Display.getCurrent() != null) {
			String elementText;
			if (fLabelProvider != null) {
				elementText = fLabelProvider.getText(element);
			}
			else {
				elementText = element == null ? "" : element.toString();
			}
			monitor.subTask("Try to matching \"" + elementText + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else {
			fViewer.getTree().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					reportProgress(element, monitor);
				}
			});
		}
	}
	
	protected Object[] getUpdatedChildren(final Object parent, final IProgressMonitor monitor) {
		if (Display.getCurrent() != null) {
			if (parent instanceof Pending) return new Object[0];
			ILazyLoader lazyLoader = getLazyLoader(parent);
			if (lazyLoader != null) {
				if (!lazyLoader.isDataLoaded()) {
					try {
						lazyLoader.loadData(monitor);
					}
					catch (InvocationTargetException e) {
						return new Object[0];
					}
					catch (InterruptedException e) {
						monitor.setCanceled(true);
						return new Object[0];
					}
				}
			}
			Object[] children = getSortedChildren(parent);
			return children;
		}
		final AtomicReference<Object[]> ref = new AtomicReference<Object[]>();
		fViewer.getTree().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				ref.set(getUpdatedChildren(parent, monitor));
			}
		});
		return ref.get();
	}

	private Object[] getSortedChildren(Object parentElementOrTreePath) {
		Object[] result = getFilteredChildren(parentElementOrTreePath);
		ViewerComparator comparator = fViewer.getComparator();
		if (comparator != null) {
			// be sure we're not modifying the original array from the model
			result = (Object[]) result.clone();
			comparator.sort(fViewer, result);
		}
		return result;
    }

	private Object[] getFilteredChildren(Object parent) {
		Object[] result = getRawChildren(parent);
		ViewerFilter[] filters = fViewer.getFilters();
		if (filters != null) {
			for (ViewerFilter f : filters) {
				result = f.filter(fViewer, parent, result);
			}
		}
		return result;
    }

	private Object[] getRawChildren(Object parent) {
		Object[] result = null;
		if (parent != null) {
			ITreeContentProvider tcp = (ITreeContentProvider) fViewer.getContentProvider();
			result = tcp.getChildren(parent);
		}
		return (result != null) ? result : new Object[0];
    }

	private ILazyLoader getLazyLoader(Object parent) {
		ILazyLoader loader = null;
		if(parent instanceof ILazyLoader) {
			loader = (ILazyLoader) parent;
		}
		if(loader == null && parent instanceof IAdaptable) {
			loader = (ILazyLoader)((IAdaptable)parent).getAdapter(ILazyLoader.class);
		}
		if(loader == null) {
			loader = (ILazyLoader) Platform.getAdapterManager().getAdapter(parent, ILazyLoader.class);
		}
	    return loader;
    }

	/**
	 * Search the viewer for the next target which matches the condition defined
	 * by the matcher. The searching process is asynchronous. The call will
	 * return immediately. Once the target is found, it will invoke the passed
	 * callback to notify the caller the result.
	 * 
	 * @param matcher
	 *            The matcher defining the searching condition. It must not be
	 *            null.
	 * @param callback
	 *            The callback invoked when the next target is done. It must not
	 *            be null.
	 */
	public void searchNext(final boolean foreward, final ISearchMatcher matcher, final ISearchCallback callback) {
		final TreePath[]result = new TreePath[1];
		fSearchJob = new Job("Searching ...") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Searching ...", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
				result[0] = searchNode(foreward, matcher, monitor);
				monitor.done();
				return monitor.isCanceled()?Status.CANCEL_STATUS:Status.OK_STATUS;
			}
		};
		fSearchJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				callback.callback(event.getResult(), result[0]);
			}
		});
		fSearchJob.schedule();
	}

	/**
	 * End the search process. This method is called to clear the searching
	 * context when the search process is ended. Callers must call this method
	 * to clean up the state data left by the search process.
	 */
	public void endSearch() {
		if (fSearchJob != null) {
			fSearchJob.cancel();
			fSearchJob = null;
		}
	}
	
}
