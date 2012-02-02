/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.workingsets;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
/**
 * The element factory to create a CustomizedOrderComparator from a
 * memento.
 */
public class CustomizedOrderComparatorFactory implements IElementFactory {
	public static final String FACTORY_ID = "org.eclipse.tcf.te.ui.views.workingsets.CustomizedOrderComparatorFactory"; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IElementFactory#createElement(org.eclipse.ui.IMemento)
	 */
	@Override
    public IAdaptable createElement(IMemento memento) {
		CustomizedOrderComparator comparator = new CustomizedOrderComparator();
		comparator.restoreState(memento);
		return comparator;
	}
}
