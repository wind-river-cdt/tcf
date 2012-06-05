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

import org.eclipse.tcf.util.TCFDataCache;

/**
 * TCF clients can implement ITCFPrettyExpressionProvider to provide human
 * readable "pretty expression" strings that represent values of the TCF debug model objects
 * to be shown in the debugger views.
 *
 * TCF will use internal pretty expression if no suitable provider is found
 * through "pretty_expression_provider" extension point.
 */
public interface ITCFPrettyExpressionProvider {

    /**
     * Get cache item that contains human readable "pretty expression" string
     * that represents value of the object.
     * @param object - TCF debug model object
     * @return cache item or null if "pretty expression" not available for the object
     */
    TCFDataCache<String> getText(ITCFObject object);

    /**
     * Get cache item that contains expressions of "pretty expression" children.
     * The debugger will evaluate those expressions using Expressions service.
     * Results of the evaluation will be shown as children of the object.
     * @param object - TCF debug model object
     * @return cache item or null if children not available for the object
     */
    TCFDataCache<String[]> getChildren(ITCFObject object);

    /**
     * Cancel pending "pretty expression" evaluations and invalidate caches
     * associated with a model object.
     * @param object - TCF debug model object
     */
    void cancel(ITCFObject object);

    /**
     * Dispose "pretty expression" caches associated with a model object.
     * @param object - TCF debug model object
     */
    void dispose(ITCFObject object);
}
