/*******************************************************************************
 * Copyright (c) 2006, 2012, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.internal.utils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * The viewer filter to select those elements which matches the given filter pattern.
 * <p>
 * Copied from org.eclipse.ui.internal.navigator.filters.CommonFiltersTab$TablePatternFilter
 */
public class TablePatternFilter extends ViewerFilter {
	private static final String ALL = "*"; //$NON-NLS-1$

	protected StringMatcher matcher = null;
	protected ILabelProvider labelProvider;
	protected List<PropertyChangeListener> listeners;
	protected String pattern;

	public TablePatternFilter(ILabelProvider labelProvider) {
		this.labelProvider = labelProvider;
		listeners = Collections.synchronizedList(new ArrayList<PropertyChangeListener>());
	}

	/**
	 * Add a property change listener to this pattern filter.
	 *
	 * @param l The listener.
	 */
	public void addPropertyChangeListener(PropertyChangeListener l) {
		listeners.add(l);
	}

	/**
	 * Remove a property change listener to this pattern filter.
	 *
	 * @param l The listener.
	 */
	public void removePropertyChangeListener(PropertyChangeListener l) {
		listeners.remove(l);
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

	/**
	 * Set a new pattern to filter elements.
	 *
	 * @param newPattern The new pattern
	 */
	public void setPattern(String newPattern) {
		pattern = newPattern;
		StringMatcher old = matcher;
		if (newPattern == null || newPattern.trim().length() == 0) {
			matcher = null;
		}
		else {
			String patternString = ALL + newPattern + ALL;
			matcher = new StringMatcher(patternString, true, false);
		}
		firePatternChangedEvent(old, matcher);
	}

	/**
	 * Fire a pattern changed event to all listening listeners.
	 *
	 * @param oldMatcher The old matcher.
	 * @param newMatcher The new matcher.
	 */
	protected void firePatternChangedEvent(StringMatcher oldMatcher, StringMatcher newMatcher) {
		PropertyChangeEvent event = new PropertyChangeEvent(this, "pattern", oldMatcher, newMatcher); //$NON-NLS-1$
		synchronized(listeners) {
			for(PropertyChangeListener listener : listeners) {
				listener.propertyChange(event);
			}
		}
    }

	public String getFilterText() {
	    return pattern;
    }

	/**
	 * Answers whether the given String matches the pattern.
	 *
	 * @param input the String to test
	 *
	 * @return whether the string matches the pattern
	 */
	public boolean match(String input) {
		if (input == null) {
			return false;
		}
		return matcher == null || matcher.match(input);
	}
}