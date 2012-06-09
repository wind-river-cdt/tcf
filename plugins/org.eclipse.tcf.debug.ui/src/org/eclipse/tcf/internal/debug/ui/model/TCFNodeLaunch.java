/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.debug.ui.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.tcf.debug.ui.ITCFDebugUIConstants;
import org.eclipse.tcf.services.IMemory;
import org.eclipse.tcf.services.IRunControl;


public class TCFNodeLaunch extends TCFNode implements ISymbolOwner {

    private final TCFChildrenExecContext children;

    private final TCFChildren filtered_children;
    private final TCFChildrenContextQuery children_query;
    private final TCFContextQueryDescendants query_descendants_count;
    private final Map<String,TCFNodeSymbol> symbols = new HashMap<String,TCFNodeSymbol>();

    TCFNodeLaunch(final TCFModel model) {
        super(model);
        children = new TCFChildrenExecContext(this);
        query_descendants_count = new TCFContextQueryDescendants(this);
        filtered_children = new TCFChildren(this) {
            @Override
            protected boolean startDataRetrieval() {
                Set<String> filter = launch.getContextFilter();
                if (filter == null) {
                    if (!children.validate(this)) return false;
                    set(null, children.getError(), children.getData());
                    return true;
                }
                Map<String,TCFNode> nodes = new HashMap<String,TCFNode>();
                for (String id : filter) {
                    if (!model.createNode(id, this)) return false;
                    if (isValid()) {
                        // Ignore invalid IDs
                        reset();
                    }
                    else {
                        nodes.put(id, model.getNode(id));
                    }
                }
                set(null, null, nodes);
                return true;
            }
            @Override
            public void dispose() {
                getNodes().clear();
                super.dispose();
            }
        };
        children_query = new TCFChildrenContextQuery(this, children);
    }

    @Override
    void dispose() {
        ArrayList<TCFNodeSymbol> l = new ArrayList<TCFNodeSymbol>(symbols.values());
        for (TCFNodeSymbol s : l) s.dispose();
        assert symbols.size() == 0;
        super.dispose();
    }

    @SuppressWarnings("unchecked")
    private boolean setQuery(IViewerUpdate result, Runnable done) {
        IPresentationContext context = result.getPresentationContext();
        String query = (String)context.getProperty(ITCFDebugUIConstants.PROP_CONTEXT_QUERY);
        Set<String> filter = (Set<String>)context.getProperty(ITCFDebugUIConstants.PROP_FILTER_CONTEXTS);
        return children_query.setQuery(query, model.getLaunch().getModelContexts(filter), done) &&
                children_query.validate(done);
    }

    @Override
    protected boolean getData(IChildrenCountUpdate result, Runnable done) {
        String view_id = result.getPresentationContext().getId();
        if (IDebugUIConstants.ID_DEBUG_VIEW.equals(view_id)) {
            if (!filtered_children.validate(done)) return false;
            result.setChildCount(filtered_children.size());
        }
        else if (ITCFDebugUIConstants.ID_CONTEXT_QUERY_VIEW.equals(view_id)) {
            if (!setQuery(result, done)) return false;
            result.setChildCount(children_query.size());
        }
        else {
            result.setChildCount(0);
        }
        return true;
    }

    @Override
    protected boolean getData(IChildrenUpdate result, Runnable done) {
        String view_id = result.getPresentationContext().getId();
        if (IDebugUIConstants.ID_DEBUG_VIEW.equals(view_id)) {
            return filtered_children.getData(result, done);
        }
        else if (ITCFDebugUIConstants.ID_CONTEXT_QUERY_VIEW.equals(view_id)) {
            if (!setQuery(result, done)) return false;
            return children_query.getData(result, done);
        }
        return true;
    }

    @Override
    protected boolean getData(IHasChildrenUpdate result, Runnable done) {
        String view_id = result.getPresentationContext().getId();
        if (IDebugUIConstants.ID_DEBUG_VIEW.equals(view_id)) {
            if (!filtered_children.validate(done)) return false;
            result.setHasChilren(filtered_children.size() > 0);
        }
        else if (ITCFDebugUIConstants.ID_CONTEXT_QUERY_VIEW.equals(view_id)) {
            if (!setQuery(result, done)) return false;
            result.setHasChilren(children_query.size() > 0);
        }
        else {
            result.setHasChilren(false);
        }
        return true;
    }

    void onContextAdded(IRunControl.RunControlContext context) {
        Set<String> filter = launch.getContextFilter();
        if (filter != null) {
            String c = context.getCreatorID();
            while (c != null) {
                if (filter.contains(c)) {
                    filter.add(context.getID());
                    break;
                }
                Object o = model.getContextMap().get(c);
                if (o instanceof IRunControl.RunControlContext) {
                    c = ((IRunControl.RunControlContext)o).getParentID();
                }
                else {
                    break;
                }
            }
        }
        children.onContextAdded(context);
    }

    void onContextAdded(IMemory.MemoryContext context) {
        children.onContextAdded(context);
    }

    void onAnyContextSuspendedOrChanged() {
        for (TCFNodeSymbol s : symbols.values()) s.onMemoryMapChanged();
    }

    void onAnyContextAddedOrRemoved() {
        filtered_children.reset();
        children_query.reset();
        query_descendants_count.reset();
    }

    public void addSymbol(TCFNodeSymbol s) {
        assert symbols.get(s.id) == null;
        symbols.put(s.id, s);
    }

    public void removeSymbol(TCFNodeSymbol s) {
        assert symbols.get(s.id) == s;
        symbols.remove(s.id);
    }

    public TCFChildren getChildren() {
        return children;
    }

    public TCFChildren getFilteredChildren() {
        return filtered_children;
    }
    
    public TCFContextQueryDescendants getContextQueryDescendants() {
        return query_descendants_count;
    }

}
