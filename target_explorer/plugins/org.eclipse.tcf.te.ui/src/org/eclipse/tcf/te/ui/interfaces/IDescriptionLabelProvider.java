/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.interfaces;

import org.eclipse.jface.viewers.ILabelProvider;

/**
 * An extended label provider that provides descriptive texts for given elements
 * besides labels and images.
 */
public interface IDescriptionLabelProvider extends ILabelProvider {
	
    /**
     * Returns the description for the given element.       
     * 
     * @param element the element for which to provide the description text
     * @return the text string used to describe the element, or <code>null</code>
     *   if there is no description text for the given object
     */
	public String getDescription(Object element);
}
