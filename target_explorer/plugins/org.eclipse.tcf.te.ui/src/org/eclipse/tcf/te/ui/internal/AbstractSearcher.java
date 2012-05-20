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

import java.lang.reflect.Method;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.te.ui.interfaces.ILazyLoader;
import org.eclipse.tcf.te.ui.interfaces.ISearchMatcher;
import org.eclipse.tcf.te.ui.trees.Pending;
import org.eclipse.ui.PlatformUI;

public abstract class AbstractSearcher implements ITreeSearcher {
	private static Method methodGetSortedChildren;
	static {
		try {
	        methodGetSortedChildren = AbstractTreeViewer.class.getDeclaredMethod("getSortedChildren", new Class[]{Object.class}); //$NON-NLS-1$
	        methodGetSortedChildren.setAccessible(true);
        }
        catch (Exception e) {
        }
	}
	protected TreeViewer fViewer;
	// The label provider of the tree viewer.
	private ILabelProvider fLabelProvider;
	protected ISearchMatcher fMatcher;
	public AbstractSearcher(TreeViewer viewer, ISearchMatcher matcher) {
		fViewer = viewer;
		fLabelProvider = (ILabelProvider) fViewer.getLabelProvider();
		fMatcher = matcher;
	}
	
	abstract public void setStartPath(TreePath path);
	
	public String getElementText(final Object element) {
		if (Display.getCurrent() != null) {
			if(element == fViewer.getInput()) return "the root"; //$NON-NLS-1$
			String elementText;
			if (fLabelProvider != null) {
				elementText = fLabelProvider.getText(element);
			}
			else {
				elementText = element == null ? "" : element.toString(); //$NON-NLS-1$
			}
			return elementText;
		}
		final String[] result = new String[1];
		fViewer.getTree().getDisplay().syncExec(new Runnable() {
        	@Override
        	public void run() {
        		result[0] = getElementText(element);
        	}
        });
		return result[0];
	}
	
	protected void advance(String msg, IProgressMonitor monitor) {
		monitor.subTask(msg);
	}

	protected Object[] getUpdatedChildren(final Object parent, final IProgressMonitor monitor) {
		if (parent instanceof Pending) return new Object[0];
		final ILazyLoader lazyLoader = getLazyLoader(parent);
		if (lazyLoader != null) {
			if (!lazyLoader.isDataLoaded()) {
				try{
					lazyLoader.loadData(monitor);
				}catch(Exception e) {
					return new Object[0];
				}
			}
		}
		Object[] children = getSortedChildren(parent);
		return children;
	}

	Object[] getSortedChildren(final Object parentElementOrTreePath) {
		if(Display.getCurrent() != null) {
		try {
			if (methodGetSortedChildren != null) {
				return (Object[]) methodGetSortedChildren.invoke(fViewer, parentElementOrTreePath);
			}
		}
		catch (Exception e) {
		}
		return new Object[0];
		}
		final Object[][]result = new Object[1][];
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable(){
			@Override
            public void run() {
				result[0] = getSortedChildren(parentElementOrTreePath);
            }});
		return result[0];
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
}
