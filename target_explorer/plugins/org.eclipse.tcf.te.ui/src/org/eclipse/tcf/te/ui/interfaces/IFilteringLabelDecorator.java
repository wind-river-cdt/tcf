/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.interfaces;

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * The interface to define a label decorator which has a method
 * to check if the label decorator is enabled for specified viewer and 
 * element.
 * <p>
 * An ordinary label decorator's enablement state is only checked upon the
 * cell element, which is an issue when the decoration depends on the 
 * tree viewer's state besides the element. This interface is designed
 * to check the enablement state with both the tree viewer and the element
 * itself.
 * <p>
 * This interface is used in TreeControl and Target Explorer view to decorate
 * the tree node that are being filtered.
 * 
 * @see TreeViewerDecoratingLabelProvider
 * @see ViewViewerDecoratingLabelProvider
 */
public interface IFilteringLabelDecorator extends ILabelDecorator {
	
	/**
	 * Check if this decorator is enabled to the specified viewer and
	 * element.
	 * 
	 * @param viewer The tree viewer
	 * @param element The element
	 * @return true if this decorator is enabled over this viewer and this element.
	 */
	public boolean isEnabled(TreeViewer viewer, Object element);
}
