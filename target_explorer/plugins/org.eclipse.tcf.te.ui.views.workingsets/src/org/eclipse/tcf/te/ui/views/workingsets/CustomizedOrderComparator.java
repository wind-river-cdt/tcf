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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.navigator.IMementoAware;

/**
 * <p>
 * The comparator to sort working sets in the order specified by end users.
 * It reads a series of working set names and sort the given list in the order
 * of the working set names.
 * </p>
 */
public class CustomizedOrderComparator implements Comparator<IWorkingSet>, IMementoAware, IPersistableElement, IAdaptable {
	// The separator delimiter to separate working set names store in the memento file.
	private static final String WORKINGSET_SEPARATOR = "|"; //$NON-NLS-1$
	// The sequence of the sorted working set.
	private static final String ATTR_SEQUENCE = "sequence"; //$NON-NLS-1$

	// Working set names which are used to sort working sets.
	private List<String> workingSetNames;

	/**
	 * Create a comparator instance.
	 */
	public CustomizedOrderComparator() {
	}

	/**
	 * Create a comparator instance using the same order of the
	 * specified working set array.
	 *
	 * @param workingSets The working set array
	 */
	public CustomizedOrderComparator(IWorkingSet[] workingSets) {
		workingSetNames = new ArrayList<String>();
		for (IWorkingSet workingSet : workingSets) {
			workingSetNames.add(workingSet.getName());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IMementoAware#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
    public void saveState(IMemento memento) {
		StringBuilder buf = new StringBuilder();
		for (String name : workingSetNames) {
			buf.append(name + WORKINGSET_SEPARATOR);
		}
		if (buf.length() > 0) buf.deleteCharAt(buf.length() - 1);
		memento.putString(ATTR_SEQUENCE, buf.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IMementoAware#restoreState(org.eclipse.ui.IMemento)
	 */
	@Override
    public void restoreState(IMemento aMemento) {
		String wsNames = aMemento.getString(ATTR_SEQUENCE);
		StringTokenizer st = new StringTokenizer(wsNames, WORKINGSET_SEPARATOR);
		workingSetNames = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			workingSetNames.add(st.nextToken());
		}
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IPersistableElement#getFactoryId()
	 */
	@Override
    public String getFactoryId() {
		return CustomizedOrderComparatorFactory.FACTORY_ID;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
    public Object getAdapter(Class adapter) {
		if (adapter == IPersistableElement.class) {
			return this;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
    public int compare(IWorkingSet o1, IWorkingSet o2) {
		String id1 = o1.getName();
		String id2 = o2.getName();
		int index1 = workingSetNames.indexOf(id1);
		int index2 = workingSetNames.indexOf(id2);
		if (index1 != -1 && index2 != -1)  return index1 > index2 ? 1 : (index1 < index2 ? -1 : 0);
		return 0;
	}
}
