/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.adapters.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.core.expressions.ICountable;
import org.eclipse.core.expressions.IIterable;
import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.te.launch.core.selection.interfaces.ISelectionContext;

/**
 * Selection context adapter implementation.
 */
public class SelectionContextAdapter implements IIterable, ICountable {
	private final ISelectionContext context;

	/**
     * Constructor.
     *
     * @param context The selection context. Must not be <code>null</code>.
     */
    public SelectionContextAdapter(ISelectionContext context) {
    	Assert.isNotNull(context);
    	this.context = context;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.expressions.ICountable#count()
     */
    @Override
    public int count() {
        return context.getSelections() != null ? context.getSelections().length : 0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.expressions.IIterable#iterator()
     */
    @Override
    public Iterator iterator() {
    	Iterator<Object> iterator = null;

    	if (context.getSelections() != null) {
    		iterator = Arrays.asList(context.getSelections()).iterator();
    	} else {
    		iterator = Collections.emptyList().iterator();
    	}

        return iterator;
    }

}
