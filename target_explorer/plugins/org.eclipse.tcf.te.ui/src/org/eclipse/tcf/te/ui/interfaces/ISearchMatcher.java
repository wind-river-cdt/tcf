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



/**
 * The search matcher interface. A class implementing this interface defines 
 * the searching rule. 
 */
public interface ISearchMatcher {
	
	/**
	 * If the element matches the matching rule.
	 * 
	 * @param element
	 * 				The element to be examined.
	 * @return true if it matches or else false.
	 */
	public boolean match(Object element);
}
