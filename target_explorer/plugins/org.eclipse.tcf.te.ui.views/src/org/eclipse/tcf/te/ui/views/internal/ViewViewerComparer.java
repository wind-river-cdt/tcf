/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.internal;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.IElementComparer;

/**
 * The comparer implementation for ViewViewer in order customize
 * the comparison of tree nodes.
 */
public class ViewViewerComparer implements IElementComparer {
	// If the current comparison is by default.
	private boolean byDefault;
	
	/**
	 * Constructor
	 */
	public ViewViewerComparer() {
		byDefault = true;
	}
	
	/**
	 * Set if the current comparison should follow default one.
	 * 
	 * @param value The new value.
	 */
	public void setByDefault(boolean value) {
		byDefault = value;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IElementComparer#equals(java.lang.Object, java.lang.Object)
	 */
	@Override
    public boolean equals(Object a, Object b) {
		if(a == b) return true;
		if(byDefault) {
			return a.equals(b);
		}
		IElementComparer comparer = (IElementComparer) Platform.getAdapterManager().getAdapter(a, IElementComparer.class);
		if(comparer == null) {
			comparer = (IElementComparer) Platform.getAdapterManager().getAdapter(b, IElementComparer.class);
		}
		if(comparer != null) {
			return comparer.equals(a, b);
		}
		return a.equals(b);
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IElementComparer#hashCode(java.lang.Object)
	 */
	@Override
    public int hashCode(Object element) {
		if (element != null) {
			if(byDefault)
				return element.hashCode();
			IElementComparer comparer = (IElementComparer) Platform.getAdapterManager().getAdapter(element, IElementComparer.class);
			if(comparer != null) {
				return comparer.hashCode(element);
			}
			return element.hashCode();
		}
	    return 0;
    }
}
