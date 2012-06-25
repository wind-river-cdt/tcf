/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.test.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.tcf.protocol.Protocol;

/**
 * 
 */
public class ResetMap {
    
    public interface IResettable {
        public void reset();
    }
    
    public static final String ANY_ID = "";
    
    private Map<String, List<IResettable>> fValid = new TreeMap<String, List<IResettable>>();
    private Map<String, Set<String>> fChildren = new TreeMap<String, Set<String>>();
    private Map<String, String> fParents = new TreeMap<String, String>();
    
    // Mapping of context IDs that were reset while a given cache was pending.
    private Map<IResettable, Set<String>> fPending = new LinkedHashMap<IResettable, Set<String>>();

    public Set<String> removePending(IResettable cache) {
        assert Protocol.isDispatchThread();
        Set<String> pendingIds = fPending.remove(cache);
        if (pendingIds == null) {
            pendingIds = Collections.emptySet();
        }
        return pendingIds;
    }

    public boolean clearPending(String id, IResettable cache) {
        assert Protocol.isDispatchThread();
        Set<String> pendingIds = fPending.remove(cache);
        if (pendingIds != null && pendingIds.contains(id)) {
            cache.reset();
            return true;
        }
        return false;
    }

    public void addValid(String id, IResettable cache) {
        assert Protocol.isDispatchThread();
        
        if (!clearPending(id, cache)) return;
        
        List<IResettable> list = fValid.get(id);
        if (list == null) {
            list = new LinkedList<IResettable>();
            fValid.put(id, list);
        }
        list.add(cache);
    }

    public void addValid(String id, String[] childrenIds, IResettable cache) {
        assert Protocol.isDispatchThread();
        
        if (!clearPending(id, cache)) return;
        
        List<IResettable> list = fValid.get(id);
        if (list == null) {
            list = new LinkedList<IResettable>();
            fValid.put(id, list);
        }
        list.add(cache);
        for (String childId : childrenIds) {
            fParents.put(childId, id);
        }
    }

    public void addValid(List<String> ids, IResettable cache) {
        assert Protocol.isDispatchThread();

        boolean valid = true;
        for (int i = 0; i < ids.size() - 1; i++) {
            valid = clearPending(ids.get(i), cache) && valid;
        }
        if (!valid) return;
        
        String id = ids.get(0);
        List<IResettable> list = fValid.get(id);
        if (list == null) {
            list = new ArrayList<IResettable>();
            fValid.put(id, list);
        }
        list.add(cache);
        
        for (int i = 0; i < ids.size() - 1; i++) {
            Set<String> children = fChildren.get(ids.get(i + 1));
            if (children == null) {
                children = new TreeSet<String>();
                fChildren.put(ids.get(i + 1), children);
            }
            children.add(ids.get(i));
        }
    }

    public List<IResettable> getCaches(String id) {
        assert Protocol.isDispatchThread();
        
        List<IResettable> list = fValid.get(id);
        if (list == null) {
            list = Collections.emptyList();
        }
        return list;
    }

    public void reset(String id) {
        reset(id, true, true);
    }

    public void reset(String id, boolean resetChildren, boolean resetParents) {
        assert Protocol.isDispatchThread();
        
        // Do not call reset while holding lock to reset map.  Instead collect 
        // caches to reset and reset them outside the lock.
        List<IResettable> anyList = Collections.emptyList();
        List<IResettable> idList = Collections.emptyList();
        List<IResettable> parentList = Collections.emptyList();
        synchronized (this) {
            for (Set<String> pendingIds : fPending.values()) {
                pendingIds.add(id);
            }
            anyList = fValid.remove(ANY_ID);
            
            if (resetChildren && fChildren.containsKey(id)) {
                idList = new ArrayList<IResettable>();
                collectChildren(id, idList);
            } else {
                idList = fValid.remove(id);
            }
            
            if (resetParents) {
                String parentId = fParents.remove(id);
                if (parentId != null) {
                    parentList = fValid.remove(parentId);
                }
            }
        }
        resetList(anyList);
        resetList(idList);        
        resetList(parentList);        
    }
    
    private void collectChildren(String id, List<IResettable> caches) {
        caches.addAll( fValid.remove(id) );
        Set<String> children = fChildren.remove(id);
        if (children != null) {
            for (String child : children) {
                collectChildren(child, caches);
            }
        }
    }
    
    private void resetList(List<IResettable> list) {
        if (list != null) {
            for (IResettable cache : list) {
                cache.reset();
            }
        }
    }
    
    public void resetAll() {
        assert Protocol.isDispatchThread();
        Collection<List<IResettable>> valid = null;
        synchronized (this) {
            valid = fValid.values();
        }
        
        for (List<IResettable> validList : valid) {
            resetList(validList);
        }
    }

    public void addPending(IResettable cache) {
        assert Protocol.isDispatchThread();
        if (!fPending.containsKey(cache)) {
            fPending.put(cache, new TreeSet<String>());
        }
    }
}
