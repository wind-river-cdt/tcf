/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.ui;

import org.eclipse.tcf.services.IExpressions;
import org.eclipse.tcf.util.TCFDataCache;

/**
 * ITCFExpression is an interface that is implemented by TCF debug model elements
 * that represent a remote expression.
 * A visual element in a debugger view can be adapted to this interface -
 * if the element represents a remote TCF expression.
 */
public interface ITCFExpression extends ITCFObject {

    /**
     * Get expression properties cache.
     * @return The expression properties cache.
     */
    public TCFDataCache<IExpressions.Expression> getExpression();

    /**
     * Get expression value cache.
     * @return The expression value cache.
     */
    public TCFDataCache<IExpressions.Value> getValue();
}
