/*******************************************************************************
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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.tcf.internal.debug.ui.model.TCFNodeExecContext.MemoryRegion;
import org.eclipse.tcf.util.TCFDataCache;

/**
 * Provides and caches memory regions (modules) for a context.
 */
public class TCFChildrenModules extends TCFChildren {

    public TCFChildrenModules(TCFNode node) {
        super(node, 128);
    }

    void onMemoryMapChanged() {
        reset();
    }

    @Override
    protected boolean startDataRetrieval() {
        TCFNodeExecContext exe = (TCFNodeExecContext)node;
        TCFDataCache<MemoryRegion[]> map_cache = exe.getMemoryMap();
        if (!map_cache.validate(this)) return false;
        MemoryRegion[] map = map_cache.getData();
        Map<String, TCFNode> data = new HashMap<String, TCFNode>();
        if (map != null) {
            for (MemoryRegion region : map) {
                String id = node.id + ".Module-" + region.region.getFileName() + '@' + region.region.getAddress();
                TCFNodeModule module = (TCFNodeModule) node.model.getNode(id);
                if (module == null) module = new TCFNodeModule(node, id);
                module.setRegion(region.region);
                data.put(id, module);
            }
        }
        set(null, map_cache.getError(), data);
        return true;
    }
}
