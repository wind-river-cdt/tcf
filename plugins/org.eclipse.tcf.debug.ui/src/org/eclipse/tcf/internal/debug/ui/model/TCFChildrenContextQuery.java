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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.tcf.util.TCFDataCache;

/**
 * Cache item that contains children of the node.
 * The list of children is filtered according to the context query and context filter.
 * An element in the list either matches both query and filter,
 * or it is an ancestor of a matching node.
 */
public class TCFChildrenContextQuery extends TCFChildren {

    private final TCFChildren children;

    private String[] query_data;
    private Set<String> filter;

    TCFChildrenContextQuery(TCFNode node, TCFChildren children) {
        super(node);
        this.children = children;
    }

    @Override
    public void dispose() {
        getNodes().clear();
        super.dispose();
    }

    boolean setQuery(String query, Set<String> filter, Runnable done) {
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

    private boolean retainAll(Map<String,TCFNode> map, String[] ids) {
        TCFModel model = node.getModel();
        Set<String> set = new HashSet<String>();
        for (String id : ids) {
            if (!model.createNode(id, this)) return false;
            if (isValid()) return true; // error creating a node
            TCFNode n = model.getNode(id);
            while (n != null) {
                if (n.parent == node) {
                    set.add(id);
                    break;
                }
                n = n.parent;
            }
        }
        map.keySet().retainAll(set);
        return true;
    }

    @Override
    protected boolean startDataRetrieval() {
        if (!children.validate(this)) return false;
        Map<String,TCFNode> map = new HashMap<String,TCFNode>();
        if (children.size() > 0) {
            for (TCFNode n : children.toArray()) map.put(n.id, n);
            if (query_data != null && !retainAll(map, query_data)) return false;
            if (isValid()) return true; // error creating a node
            if (filter != null && !retainAll(map, filter.toArray(new String[filter.size()]))) return false;
            if (isValid()) return true; // error creating a node
        }
        set(null, children.getError(), map);
        return true;
    }
}
