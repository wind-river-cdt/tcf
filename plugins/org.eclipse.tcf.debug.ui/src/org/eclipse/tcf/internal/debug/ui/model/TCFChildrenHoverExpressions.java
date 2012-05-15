/*******************************************************************************
 * Copyright (c) 2010, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.debug.ui.model;

import java.util.HashMap;

/**
 * Provides the cache of root nodes for the expression hover.
 */
class TCFChildrenHoverExpressions extends TCFChildren {

    private String expression;

    TCFChildrenHoverExpressions(TCFNode parent) {
        super(parent, 16);
    }

    void setExpression(String expression) {
        if (expression == this.expression) return;
        if (expression != null && expression.equals(this.expression)) return;
        this.expression = expression;
        cancel();
    }

    void onSuspended(boolean func_call) {
        for (TCFNode n : getNodes()) ((TCFNodeExpression)n).onSuspended(func_call);
    }

    void onRegisterValueChanged() {
        for (TCFNode n : getNodes()) ((TCFNodeExpression)n).onRegisterValueChanged();
    }

    void onMemoryChanged() {
        for (TCFNode n : getNodes()) ((TCFNodeExpression)n).onMemoryChanged();
    }

    void onMemoryMapChanged() {
        for (TCFNode n : getNodes()) ((TCFNodeExpression)n).onMemoryMapChanged();
    }

    private TCFNodeExpression findScript(String text) {
        for (TCFNode n : getNodes()) {
            TCFNodeExpression e = (TCFNodeExpression)n;
            if (text.equals(e.getScript())) return e;
        }
        return null;
    }

    @Override
    protected boolean startDataRetrieval() {
        HashMap<String,TCFNode> data = new HashMap<String,TCFNode>();
        if (expression != null) {
            TCFNodeExpression expression_node = findScript(expression);
            if (expression_node == null) {
                add(expression_node = new TCFNodeExpression(node, expression, null, null, null, -1, false));
            }
            data.put(expression_node.id, expression_node);
        }
        set(null, null, data);
        return true;
    }
}
