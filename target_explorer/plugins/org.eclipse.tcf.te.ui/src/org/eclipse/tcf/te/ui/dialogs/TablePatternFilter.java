/*******************************************************************************
 * Copyright (c) 2006, 2009, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.dialogs;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * The viewer filter to select those elements which matches the given filter pattern.
 * @since 1.0.0 Copied from org.eclipse.ui.internal.navigator.filters.CommonFiltersTab$TablePatternFilter
 */
class TablePatternFilter extends ViewerFilter {
	private static final String ALL = "*"; //$NON-NLS-1$
	
	private StringMatcher matcher = null;
	private ILabelProvider labelProvider;

	public TablePatternFilter(ILabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer,
	 * java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return match(labelProvider.getText(element));
	}

	protected void setPattern(String newPattern) {
		if (newPattern == null || newPattern.trim().length() == 0) {
			matcher = new StringMatcher(ALL, true, false);
		}
		else {
			String patternString = ALL + newPattern + ALL;
			matcher = new StringMatcher(patternString, true, false);
		}
	}

	/**
	 * Answers whether the given String matches the pattern.
	 * 
	 * @param input the String to test
	 * 
	 * @return whether the string matches the pattern
	 */
	protected boolean match(String input) {
		if (input == null) {
			return false;
		}
		return matcher == null || matcher.match(input);
	}
}