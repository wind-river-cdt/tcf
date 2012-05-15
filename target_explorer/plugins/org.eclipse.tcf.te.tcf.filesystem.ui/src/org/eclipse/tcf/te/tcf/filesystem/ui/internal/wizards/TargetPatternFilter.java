/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.wizards;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.ui.navigator.LabelProviderDelegate;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * A class that handles filtering wizard node items based on a supplied matching
 * string and keywords
 * <p>
 * This class is copied and adapted from <code>org.eclipse.ui.internal.dialogs.WizardPatternFilter</code>.
 *
 * @since 3.8
 */
public class TargetPatternFilter extends PatternFilter {
    private LabelProviderDelegate targetLabelProvider = new LabelProviderDelegate();
	/**
	 * Create a new instance of a WizardPatternFilter
	 * @param isMatchItem
	 */
	public TargetPatternFilter() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.internal.dialogs.PatternFilter#isElementSelectable(java.lang.Object)
	 */
	@Override
    public boolean isElementSelectable(Object element) {
		return element instanceof IPeerModel;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.internal.dialogs.PatternFilter#isElementMatch(org.eclipse.jface.viewers.Viewer, java.lang.Object)
	 */
	@Override
    protected boolean isLeafMatch(Viewer viewer, Object element) {
		if ( element instanceof IPeerModel) {
			String text = targetLabelProvider.getText(element);
			if (wordMatches(text)) {
				return true;
			}
		}
		return false;
	}
}
