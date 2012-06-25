/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.test.util;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.tcf.debug.test.services.ResetMap.IResettable;
import org.eclipse.tcf.protocol.IToken;

/**
 * 
 */
public abstract class TokenCache<V> extends AbstractCache<V> implements IResettable {

    private AtomicReference<IToken> fToken = new AtomicReference<IToken>();
    
    @Override
    final protected void retrieve() {
        fToken.set(retrieveToken());
    }
    
    protected boolean checkToken(IToken token) {
        return fToken.compareAndSet(token, null);
    }
    
    abstract protected IToken retrieveToken();
    
    protected void set(IToken token, V data, Throwable error) {
        if (checkToken(token) ) {
            set(data, error, true);
        }
    }
    
    @Override
    public void set(V data, Throwable error, boolean valid) {
        super.set(data, error, valid);
        // If new value was set to the cache but a command is still 
        // outstanding.  Cancel the command.
        IToken token = fToken.getAndSet(null);
        if (token != null) token.cancel();
    }
    
    @Override
    protected void canceled() {
        IToken token = fToken.getAndSet(null);
        token.cancel();
    }
}
