/*******************************************************************************
 * Copyright (c) 2009, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.services.remote;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tcf.core.Command;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.protocol.JSON;
import org.eclipse.tcf.services.IPathMap;
import org.eclipse.tcf.util.TCFPathMapRule;

public class PathMapProxy implements IPathMap {

    private final IChannel channel;
    private final Map<PathMapListener,IChannel.IEventListener> listeners =
            new HashMap<PathMapListener,IChannel.IEventListener>();

    public PathMapProxy(IChannel channel) {
        this.channel = channel;
    }

    public String getName() {
        return NAME;
    }

    public IToken get(final DoneGet done) {
        return new Command(channel, this, "get", null) {
            @Override
            public void done(Exception error, Object[] args) {
                PathMapRule[] map = null;
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                    map = toPathMap(args[1]);
                }
                done.doneGet(token, error, map);
            }
        }.token;
    }

    public IToken set(PathMapRule[] map, final DoneSet done) {
        return new Command(channel, this, "set", new Object[]{ map }) {
            @Override
            public void done(Exception error, Object[] args) {
                if (error == null) {
                    assert args.length == 1;
                    error = toError(args[0]);
                }
                done.doneSet(token, error);
            }
        }.token;
    }

    @SuppressWarnings("unchecked")
    private PathMapRule[] toPathMap(Object o) {
        if (o == null) return null;
        int i = 0;
        Collection<Object> c = (Collection<Object>)o;
        PathMapRule[] map = new PathMapRule[c.size()];
        for (Object x : c) map[i++] = toPathMapRule(x);
        return map;
    }

    @SuppressWarnings("unchecked")
    private PathMapRule toPathMapRule(Object o) {
        if (o == null) return null;
        return new TCFPathMapRule((Map<String,Object>)o);
    }

    public void addListener(final PathMapListener listener) {
        IChannel.IEventListener l = new IChannel.IEventListener() {

            public void event(String name, byte[] data) {
                try {
                    Object[] args = JSON.parseSequence(data);
                    if (name.equals("changed")) {
                        assert args.length == 0;
                        listener.changed();
                    }
                    else {
                        throw new IOException("Path Map service: unknown event: " + name);
                    }
                }
                catch (Throwable x) {
                    channel.terminate(x);
                }
            }
        };
        channel.addEventListener(this, l);
        listeners.put(listener, l);
    }

    public void removeListener(PathMapListener listener) {
        IChannel.IEventListener l = listeners.remove(listener);
        if (l != null) channel.removeEventListener(this, l);
    }

    static {
        JSON.addObjectWriter(PathMapRule.class, new JSON.ObjectWriter<PathMapRule>() {
            public void write(PathMapRule r) throws IOException {
                JSON.writeObject(r.getProperties());
            }
        });
    }
}
