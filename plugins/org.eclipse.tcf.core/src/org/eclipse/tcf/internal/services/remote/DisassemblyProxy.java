/*******************************************************************************
 * Copyright (c) 2010, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.services.remote;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.eclipse.tcf.core.Command;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.protocol.JSON;
import org.eclipse.tcf.services.IDisassembly;

public class DisassemblyProxy implements IDisassembly {

    private final IChannel channel;

    private static class DisassemblyLine implements IDisassemblyLine {

        final Number addr;
        final int size;
        final byte[] data;
        final Map<String,Object>[] instruction;

        @SuppressWarnings("unchecked")
        DisassemblyLine(Map<String,Object> m) {
            addr = (Number)m.get("Address");
            data = JSON.toByteArray(m.get("OpcodeValue"));
            Number size = (Number)m.get("Size");
            if (size != null) this.size = size.intValue();
            else if (data != null) this.size = data.length;
            else this.size = 0;
            Collection<Map<String,Object>> c = (Collection<Map<String,Object>>)m.get("Instruction");
            instruction = c.toArray(new Map[c.size()]);
        }

        public Number getAddress() {
            return addr;
        }

        public int getSize() {
            return size;
        }

        public byte[] getOpcodeValue() {
            return data;
        }

        public Map<String,Object>[] getInstruction() {
            return instruction;
        }

        public String toString() {
            StringBuffer bf = new StringBuffer();
            bf.append('[');
            bf.append(addr.toString());
            bf.append(' ');
            bf.append(size);
            bf.append(' ');
            bf.append(Arrays.toString(data));
            bf.append(' ');
            for (Map<String,Object> m : instruction) bf.append(m.toString());
            bf.append(']');
            return bf.toString();
        }
    }

    public DisassemblyProxy(IChannel channel) {
        this.channel = channel;
    }

    public String getName() {
        return NAME;
    }

    public IToken getCapabilities(String context_id, final DoneGetCapabilities done) {
        return new Command(channel, this, "getCapabilities", new Object[]{ context_id }) {
            @Override
            public void done(Exception error, Object[] args) {
                Map<String,Object>[] arr = null;
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                    arr = toCapabilitiesArray(args[1]);
                }
                done.doneGetCapabilities(token, error, arr);
            }
        }.token;
    }

    public IToken disassemble(String context_id, Number addr, int size, Map<String, Object> params, final DoneDisassemble done) {
        return new Command(channel, this, "disassemble", new Object[]{ context_id, addr, size, params }) {
            @Override
            public void done(Exception error, Object[] args) {
                IDisassemblyLine[] arr = null;
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                    arr = toDisassemblyArray(args[1]);
                }
                done.doneDisassemble(token, error, arr);
            }
        }.token;
    }

    @SuppressWarnings("unchecked")
    private static Map<String,Object>[] toCapabilitiesArray(Object o) {
        if (o == null) return null;
        Collection<Map<String,Object>> c = (Collection<Map<String,Object>>)o;
        return (Map<String,Object>[])c.toArray(new Map[c.size()]);
    }

    @SuppressWarnings("unchecked")
    private static IDisassemblyLine[] toDisassemblyArray(Object o) {
        if (o == null) return null;
        Collection<Map<String,Object>> c = (Collection<Map<String,Object>>)o;
        IDisassemblyLine[] arr = new IDisassemblyLine[c.size()];
        int i = 0;
        for (Map<String,Object> m : c) {
            arr[i++] = new DisassemblyLine(m);
        }
        return arr;
    }
}
