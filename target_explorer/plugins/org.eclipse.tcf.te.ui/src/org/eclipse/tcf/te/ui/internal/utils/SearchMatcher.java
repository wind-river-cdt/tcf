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

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.te.ui.interfaces.ISearchMatcher;
import org.eclipse.ui.PlatformUI;

/**
 * A search matcher implementation that support
 * common searching options.
 */
public class SearchMatcher implements ISearchMatcher {
	// The text to be searched.
	private String fSearchTarget;
	// Whether it is case sensitive
	private boolean fCaseSensitive;
	// Whether it is precise matching.
	private boolean fMatchPrecise;
	// The viewer being searched.
	private TreeViewer fViewer;

	/**
	 * Create a search matcher with the tree viewer.
	 *
	 * @param viewer The tree viewer to create a matcher for.
	 */
	public SearchMatcher(TreeViewer viewer) {
		fViewer = viewer;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.interfaces.ISearchMatcher#match(java.lang.Object)
	 */
	@Override
	public boolean match(Object context) {
		if (context == null) return false;
		String text = getElementText(context);
		if (text == null) return false;
		String target = fSearchTarget;
		if (!fCaseSensitive) {
			text = text.toLowerCase();
			target = fSearchTarget != null ? fSearchTarget.toLowerCase() : null;
		}
		if (fMatchPrecise) return text.equals(target);
		return text.indexOf(target) != -1;
	}

	/**
	 * Get the text representation of a element using the label provider of the tree viewer. Note:
	 * this method could be called at any thread.
	 *
	 * @param element The element.
	 * @return The text representation.
	 */
	public String getElementText(final Object element) {
		if (Display.getCurrent() != null) {
			if (element == fViewer.getInput()) return null;
			ILabelProvider labelProvider = (ILabelProvider) fViewer.getLabelProvider();
			if (labelProvider != null) {
				return labelProvider.getText(element);
			}
			return element == null ? "" : element.toString(); //$NON-NLS-1$
		}
		final String[] result = new String[1];
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				result[0] = getElementText(element);
			}
		});
		return result[0];
	}

	/**
	 * Set the searching target.
	 *
	 * @param target The target node's matching string.
	 */
	public void setMatchTarget(String target) {
		fSearchTarget = target;
	}

	/**
	 * Set if the searching is case-sensitive.
	 *
	 * @param caseSensitive
	 */
	public void setCaseSensitive(boolean caseSensitive) {
		fCaseSensitive = caseSensitive;
	}

	/**
	 * Set if the searching is to match precisely.
	 *
	 * @param matchPrecise
	 */
	public void setMatchPrecise(boolean matchPrecise) {
		fMatchPrecise = matchPrecise;
	}
}
