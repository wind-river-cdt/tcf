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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.tcf.debug.ui.ITCFDebugUIConstants;
import org.eclipse.tcf.util.TCFDataCache;

/**
 * Cache item that contains children of the node.
 * The list of children is filtered according to the context query and context filter.
 * An element in the list either matches both query and filter,
 * or it is an ancestor of a matching node.
 */
public class TCFChildrenContextQuery extends TCFChildren {

    TCFChildrenContextQuery(TCFNode node) {
        super(node);
    }

    @Override
    public void dispose() {
        getNodes().clear();
        super.dispose();
    }

    public static class Descendants {
        public Map<String,String> map;
        public boolean include_parent;
    }

    private static String[] getFilterIDs(TCFModel model, Set<String> filter) {
        if (filter != null) {
            ILaunchConfiguration launchConfig = model.getLaunch().getLaunchConfiguration();
            if (launchConfig != null) {
                Set<String> set = new HashSet<String>();
                String sessionId = launchConfig.getName();
                for (String context : filter) {
                    int slashPos = context.indexOf('/');
                    if (slashPos > 0 && context.length() > slashPos + 1) {
                        if ( sessionId.equals(context.substring(0, slashPos)) ) {
                            set.add(context.substring(slashPos + 1));
                        }
                    }
                }
                return set.toArray(new String[set.size()]);
            }
        }
        return null;
    }

    private static boolean getDescendants(TCFNode node, String[] ids, Descendants res, Runnable done) {
        Map<String,String> map = res.map;
        boolean include_parent = res.include_parent;
        res.map = new HashMap<String,String>();
        TCFModel model = node.getModel();
        for (String id : ids) {
            assert !(done instanceof TCFDataCache<?>);
            if (!model.createNode(id, done)) return false;
            TCFNode n = model.getNode(id);
            while (n != null) {
                if (n == node) {
                    res.include_parent = true;
                    break;
                }
                if (n.parent == node) {
                   res.map.put(id, n.id);
                   break;
                }
                n = n.parent;
            }
        }
        if (map != null) {
            res.map.keySet().retainAll(map.keySet());
            res.include_parent = res.include_parent && include_parent;
        }
        return true;
    }

    /**
     * Get node descendants that match both query and filter.
     * If both query and filter are null, return empty map.
     * The map maps descendant ID to the node child ID.
     */
    public static Descendants getDescendants(TCFNode node, String query, Set<String> filter, Runnable done) {
        Descendants res = new Descendants();

        String[] query_data = null;
        TCFDataCache<String[]> query_cache = node.getModel().getLaunch().getContextQuery(query);
        if (query_cache != null) {
            if (!query_cache.validate(done)) return null;
            query_data = query_cache.getData();
            if (query_data != null && !getDescendants(node, query_data, res, done)) return null;
        }

        String[] filter_ids = getFilterIDs(node.getModel(), filter);
        if (filter_ids != null && !getDescendants(node, filter_ids, res, done)) return null;

        return res;
    }

    /**
     * Get node descendants that match both query and filter in presentation context of given update.
     * If both query and filter are null, return empty map.
     * The map maps descendant ID to the node child ID.
     */
    @SuppressWarnings("unchecked")
    public static Descendants getDescendants(TCFNode node, IViewerUpdate update, Runnable done) {
        IPresentationContext context = update.getPresentationContext();
        String query = (String)context.getProperty(ITCFDebugUIConstants.PROP_CONTEXT_QUERY);
        Set<String> filter = (Set<String>)context.getProperty(ITCFDebugUIConstants.PROP_FILTER_CONTEXTS);
        return getDescendants(node, query, filter, done);
    }

    /**
     * Get node children which descendants match both query and filter in presentation context of given update.
     * If both query and filter are null, return unfiltered children list.
     */
    @SuppressWarnings("unchecked")
    boolean setQuery(IViewerUpdate update, Runnable done) {
        IPresentationContext context = update.getPresentationContext();
        String query = (String)context.getProperty(ITCFDebugUIConstants.PROP_CONTEXT_QUERY);
        Set<String> filter = (Set<String>)context.getProperty(ITCFDebugUIConstants.PROP_FILTER_CONTEXTS);
        Map<String,TCFNode> map = new HashMap<String,TCFNode>();

        TCFChildren cache = null;
        if (node instanceof TCFNodeExecContext) cache = ((TCFNodeExecContext)node).getChildren();
        else if (node instanceof TCFNodeLaunch) cache = ((TCFNodeLaunch)node).getChildren();

        if (cache != null) {
            if (!cache.validate(done)) return false;
            Map<String,TCFNode> cache_data = cache.getData();
            if (cache_data != null && cache_data.size() > 0) {
                if (query != null || filter != null) {
                    Descendants des = getDescendants(node, query, filter, done);
                    if (des == null) return false;
                    for (String id : des.map.values()) {
                        TCFNode n = cache_data.get(id);
                        if (n != null) map.put(id, n);
                    }
                }
                else {
                    map.putAll(cache_data);
                }
            }
        }

        reset(map);
        return true;
    }

    @Override
    protected boolean startDataRetrieval() {
        // The method should not be called - the cache is always valid
        assert false;
        return true;
    }
}
