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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.tcf.te.ui.interfaces.ISearchMatcher;

/**
 * The interface to define a search engine for the tree viewer. 
 * It has three methods which defines the process of searching 
 * elements in the viewer:
 * <ol>
 * <li><code>startSearch</code> with an initial path to prepare the searching
 * context.</li>
 * <li><code>searchNext</code> to search an element with a rule defined by
 * ISearchMatcher.</li>
 * <li><code>endSearch</code> to clear up the searching context.</li>
 * </ol>
 * 
 * @see ISearchMatcher
 * @see ISearchCallback
 */
public interface ITreeSearcher {

	/**
	 * Search the viewer for the next target which matches the condition defined by the matcher. The
	 * searching process is asynchronous. The call will return immediately. Once the target is
	 * found, it will invoke the passed callback to notify the caller the result.
	 * 
	 * @param matcher The matcher defining the searching condition. It must not be null.
	 * @param callback The callback invoked when the next target is done. It must not be null.
	 */
	public TreePath searchNext(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException;
}
