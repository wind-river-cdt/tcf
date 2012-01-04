/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.debug.ui.model;

import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;


public class TCFNodeExecContextStatusParameter extends TCFNode {

    private final String fKey;
    
    public String getKey() { return fKey; } 
    
    protected TCFNodeExecContextStatusParameter(final TCFNodeExecContext parent, final String id, String key) {
        super(parent, id);
        fKey = key;
    }

    @Override
    protected boolean getData(final ILabelUpdate update, final Runnable done) {
        String value = "";
        TCFNodeExecContext execNode = (TCFNodeExecContext)parent;
        if (!execNode.getState().validate(done)) return false;
        if (execNode.getState().getError() != null) {
            value = execNode.getState().getError().getMessage();
        } else {
            Object param = execNode.getState().getData().suspend_params.get(fKey);
            if (param != null) {
                value = param.toString();
            } else {
                value = "";
            }
        }
        
        for (int i = 0; i < update.getColumnIds().length; i++) {
            String column = update.getColumnIds()[i];
            if ("Key".equals(column)) {
                update.setLabel(fKey, i);
            } else if ("Value".equals(column)) {
                update.setLabel(value, i);
            }
        }
        return true;

    }
}
