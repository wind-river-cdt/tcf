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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.tcf.debug.test.util.AbstractCache;
import org.eclipse.tcf.protocol.Protocol;

/**
 * 
 */
public class ResetMap {
    
    public static final String ANY_ID = "";
    
    private Map<String, List<AbstractCache<?>>> fValid = new TreeMap<String, List<AbstractCache<?>>>();
    private Map<String, Set<String>> fChildren = new TreeMap<String, Set<String>>();
    private Map<String, String> fParents = new TreeMap<String, String>();
    private Map<AbstractCache<?>, Set<String>> fPending = new LinkedHashMap<AbstractCache<?>, Set<String>>();

    public synchronized Set<String> removePending(AbstractCache<?> cache) {
        Set<String> pendingIds = fPending.remove(cache);
        if (pendingIds == null) {
            pendingIds = Collections.emptySet();
        }
        return pendingIds;
    }

    public synchronized void addValid(String id, AbstractCache<?> cache) {
        assert !fPending.containsKey(cache);
        
        List<AbstractCache<?>> list = fValid.get(id);
        if (list == null) {
            list = new ArrayList<AbstractCache<?>>();
            fValid.put(id, list);
        }
        list.add(cache);
    }

    public synchronized void addValid(String id, String[] childrenIds, AbstractCache<?> cache) {
        assert !fPending.containsKey(cache);
        
        List<AbstractCache<?>> list = fValid.get(id);
        if (list == null) {
            list = new ArrayList<AbstractCache<?>>();
            fValid.put(id, list);
        }
        list.add(cache);
        for (String childId : childrenIds) {
            fParents.put(childId, id);
        }
    }

    public synchronized void addValid(List<String> ids, AbstractCache<?> cache) {
        assert !fPending.containsKey(cache);

        String id = ids.get(0);
        List<AbstractCache<?>> list = fValid.get(id);
        if (list == null) {
            list = new ArrayList<AbstractCache<?>>();
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

    public synchronized List<AbstractCache<?>> getCaches(String id) {
        List<AbstractCache<?>> list = fValid.get(id);
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
        List<AbstractCache<?>> anyList = Collections.emptyList();
        List<AbstractCache<?>> idList = Collections.emptyList();
        List<AbstractCache<?>> parentList = Collections.emptyList();
        synchronized (this) {
            for (Set<String> pendingIds : fPending.values()) {
                pendingIds.add(id);
            }
            anyList = fValid.remove(ANY_ID);
            
            if (resetChildren && fChildren.containsKey(id)) {
                idList = new ArrayList<AbstractCache<?>>();
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
    
    private void collectChildren(String id, List<AbstractCache<?>> caches) {
        caches.addAll( fValid.remove(id) );
        Set<String> children = fChildren.remove(id);
        if (children != null) {
            for (String child : children) {
                collectChildren(child, caches);
            }
        }
    }
    
    private void resetList(List<AbstractCache<?>> list) {
        if (list != null) {
            for (AbstractCache<?> cache : list) {
                if (cache.isValid()) {
                    cache.reset();
                }
            }
        }
    }
    
    public synchronized void addPending(AbstractCache<?> cache) {
        fPending.put(cache, new TreeSet<String>());
    }
}
