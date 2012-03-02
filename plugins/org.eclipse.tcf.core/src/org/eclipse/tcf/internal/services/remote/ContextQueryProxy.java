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
package org.eclipse.tcf.internal.services.remote;

import java.util.Collection;

import org.eclipse.tcf.core.Command;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IContextQuery;

public class ContextQueryProxy implements IContextQuery {

    private final IChannel channel;

    public ContextQueryProxy(IChannel channel) {
        this.channel = channel;
    }

    public String getName() {
        return NAME;
    }

    public IToken query(String query, final DoneQuery done) {
        return new Command(channel, this, "query", new Object[]{ query }) {
            @Override
            public void done(Exception error, Object[] args) {
                String[] contexts = null;
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                    contexts = toStringArray(args[1]);
                }
                done.doneQuery(token, error, contexts);
            }
        }.token;
    }

    public IToken getAttrNames(final DoneGetAttrNames done) {
        return new Command(channel, this, "getAttrNames", null) {
            @Override
            public void done(Exception error, Object[] args) {
                String[] names = null;
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                    names = toStringArray(args[1]);
                }
                done.doneGetAttrNames(token, error, names);
            }
        }.token;
    }

    @SuppressWarnings("unchecked")
    private String[] toStringArray(Object o) {
        if (o == null) return null;
        Collection<String> c = (Collection<String>)o;
        return (String[])c.toArray(new String[c.size()]);
    }
}
