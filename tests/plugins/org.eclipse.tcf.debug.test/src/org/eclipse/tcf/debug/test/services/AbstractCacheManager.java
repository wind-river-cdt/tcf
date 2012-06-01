/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.test.services;

import java.util.LinkedHashMap;
import java.util.Map;

public class AbstractCacheManager {

    protected Map<Key<?>, Object> fMap = new LinkedHashMap<Key<?>, Object>();

    public AbstractCacheManager() {
        super();
    }

    public void dispose() {
    }

    protected <V> V getCache(Key<V> key) {
        @SuppressWarnings("unchecked")
        V cache = (V)fMap.get(key);
        return cache;
    }

    protected <V> V mapCache(Key<V> key) {
        @SuppressWarnings("unchecked")
        V cache = (V)fMap.get(key);
        if (cache != null) return cache;
        cache = key.createCache();
        fMap.put(key, cache);
        return cache;
    }

    protected boolean contains(Object[] elements, Object toFind) {
        for (int i = 0; i < elements.length; i++) {
            if (toFind.equals(elements[i])) {
                return true;
            }
        }
        return false;
    }

}