/*********** ********************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.debug.ui.model;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.tcf.protocol.Protocol;

/**
 * Provides and caches memory regions (modules) for a context.
 */
public class TCFChildrenExecStatus extends TCFChildren {

    public TCFChildrenExecStatus(TCFNodeExecContext node) {
        super(node, 128);
    }

    void onExeStateChanged() {
        reset();
    }
    
    @Override
    protected boolean startDataRetrieval() {
        assert command == null;
        
        Protocol.invokeLater(new Runnable() {
            public void run() {
                TCFNodeExecContext execNode = (TCFNodeExecContext)node;
                if (!execNode.getState().validate(this)) return;
                if (execNode.getState().getError() != null) {
                    set(null, null, null);
                    return;
                }
                
                Map<String, TCFNode> children = new TreeMap<String, TCFNode>();
                Map<String, Object> params = execNode.getState().getData().suspend_params;
                for (Map.Entry<String, Object> paramEntry : params.entrySet()) {
                    String id = node.id + ".Param@" + paramEntry.getKey();
                    children.put( 
                        id, new TCFNodeExecContextStatusParameter((TCFNodeExecContext)node, id, paramEntry.getKey()) );
                }
                set(null, null, children);
            }
        });
        return false;
    }
}
