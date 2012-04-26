/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.workingsets;

import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.ui.IWorkingSet;

/**
 * The element comparer implementation of a working set which only
 * compares name and id instead of the default behavior of its equals
 * method, which compares sub working sets as well.
 */
public class WorkingSetComparer implements IElementComparer {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IElementComparer#equals(java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean equals(Object a, Object b) {
		if(a instanceof IWorkingSet && b instanceof IWorkingSet) {
			IWorkingSet aws = (IWorkingSet) a;
			IWorkingSet bws = (IWorkingSet) b;
			String aName = aws.getName();
			String aId = aws.getId();
			String bName = bws.getName();
			String bId = bws.getId();
			return _equals(aName, bName) && _equals(aId, bId);
		}
		return _equals(a, b);
	}
	
	private boolean _equals(Object a, Object b) {
		return a == null ? b == null : a.equals(b);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IElementComparer#hashCode(java.lang.Object)
	 */
	@Override
	public int hashCode(Object element) {
		return element == null ? 0 : element.hashCode();
	}
}
