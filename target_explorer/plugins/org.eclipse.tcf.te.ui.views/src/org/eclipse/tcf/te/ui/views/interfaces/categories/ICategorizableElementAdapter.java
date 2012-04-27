/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.interfaces.categories;

/**
 * Interface to be implemented by categorizable element adapters.
 */
public interface ICategorizableElementAdapter {

	/**
	 * Returns the unique categorizable id for the given element. The id
	 * must not change once queried and must be the same across session.
	 *
	 * @param The element. Must not or <code>null</code>.
	 * @return The unique categorizable element id, or <code>null</code>.
	 */
	public String getId(Object element);
}
