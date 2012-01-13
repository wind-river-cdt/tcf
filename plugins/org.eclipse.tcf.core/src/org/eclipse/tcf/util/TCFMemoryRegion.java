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
package org.eclipse.tcf.util;

import java.util.Map;

import org.eclipse.tcf.services.IMemoryMap;

/**
 * A utility class that implements IMemoryMap.MemoryRegion interface.
 */
public class TCFMemoryRegion implements IMemoryMap.MemoryRegion {

    final Map<String,Object> props;

    public TCFMemoryRegion(Map<String,Object> props) {
        this.props = props;
    }

    public Number getAddress() {
        return (Number)props.get(IMemoryMap.PROP_ADDRESS);
    }

    public Number getOffset() {
        return (Number)props.get(IMemoryMap.PROP_OFFSET);
    }

    public Number getSize() {
        return (Number)props.get(IMemoryMap.PROP_SIZE);
    }

    public String getFileName() {
        return (String)props.get(IMemoryMap.PROP_FILE_NAME);
    }

    public String getSectionName() {
        return (String)props.get(IMemoryMap.PROP_SECTION_NAME);
    }

    public boolean isBSS() {
        Boolean b = (Boolean)props.get(IMemoryMap.PROP_BSS);
        return b != null && b.booleanValue();
    }

    public int getFlags() {
        Number n = (Number)props.get(IMemoryMap.PROP_FLAGS);
        if (n != null) return n.intValue();
        return 0;
    }

    public String getContextQuery() {
        return (String)props.get(IMemoryMap.PROP_CONTEXT_QUERY);
    }

    public Map<String,Object> getProperties() {
        return props;
    }

    public String toString() {
        return props.toString();
    }
}
