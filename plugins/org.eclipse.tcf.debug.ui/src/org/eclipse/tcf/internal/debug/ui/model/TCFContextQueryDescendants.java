/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.debug.ui.model;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.tcf.util.TCFDataCache;

/**
 * Cache item that contains children of the node.
 * The list of children is filtered according to the context query and context filter.
 * An element in the list either matches both query and filter,
 * or it is an ancestor of a matching node.
 */
public class TCFContextQueryDescendants extends TCFDataCache<Set<String>> {

    private final TCFNode node;

    private String[] query_data;
    private Set<String> filter;

    TCFContextQueryDescendants(TCFNode node) {
        super(node.getChannel());
        this.node = node;
    }

    public boolean setQuery(String query, Set<String> filter, Runnable done) {
        String[] query_data = null;
        TCFDataCache<String[]> query_cache = node.getModel().getLaunch().getContextQuery(query);
        if (query_cache != null) {
            if (!query_cache.validate(done)) return false;
            query_data = query_cache.getData();
        }
        if (!Arrays.equals(query_data, this.query_data)) reset();
        this.query_data = query_data;
        if (this.filter == filter) return true;
        if (filter != null && filter.equals(this.filter)) return true;
        this.filter = filter;
        reset();
        return true;
    }

    private boolean getDescendants(Set<String> descendants, String[] ids) {
        TCFModel model = node.getModel();
        for (String id : ids) {
            if (!model.createNode(id, this)) return false;
            if (isValid()) return true; // error creating a node
            TCFNode n = model.getNode(id);
            while (n != null) {
                if ( n.parent == node || (n.parent == null && !(node instanceof TCFNodeExecContext)) ) {
                    descendants.add(id);
                    break;
                }
                n = n.parent;
            }
        }
        return true;
    }
    
    
    @Override
    protected boolean startDataRetrieval() {
        Set<String> set = new TreeSet<String>();
        if (query_data != null && !getDescendants(set, query_data)) return false;
        if (isValid()) return true; // error creating a node
        if (filter != null) {
            Set<String> filtered = new TreeSet<String>();
            if (!getDescendants(filtered, filter.toArray(new String[filter.size()]))) return false;
            set.retainAll(filtered);
        }
        if (isValid()) return true; // error creating a node
        set(null, null, set);
        return true;
    }
}
