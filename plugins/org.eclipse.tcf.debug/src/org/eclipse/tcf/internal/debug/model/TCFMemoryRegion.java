/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.debug.model;

import java.math.BigInteger;
import java.util.Map;

import org.eclipse.tcf.protocol.JSON;
import org.eclipse.tcf.services.IMemoryMap;

/**
 * A comparable extension of TCFMemoryRegion.
 */
public class TCFMemoryRegion extends org.eclipse.tcf.util.TCFMemoryRegion implements Comparable<TCFMemoryRegion> {

    public final BigInteger addr;
    public final BigInteger size;

    public TCFMemoryRegion(Map<String,Object> props) {
        super(props);
        this.addr = JSON.toBigInteger((Number)props.get(IMemoryMap.PROP_ADDRESS));
        this.size = JSON.toBigInteger((Number)props.get(IMemoryMap.PROP_SIZE));
    }

    public int compareTo(TCFMemoryRegion r) {
        if (addr == null && r.addr == null) return 0;
        if (addr == null) return -1;
        if (r.addr == null) return +1;
        return addr.compareTo(r.addr);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TCFMemoryRegion) {
            return compareTo((TCFMemoryRegion)o) == 0;
        }
        return false;
    }
}
