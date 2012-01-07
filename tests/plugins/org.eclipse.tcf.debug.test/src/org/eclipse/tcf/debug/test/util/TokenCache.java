/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems and others.
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

import org.eclipse.tcf.protocol.IToken;

/**
 * 
 */
public abstract class TokenCache<V> extends AbstractCache<V> {

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
    protected void canceled() {
        IToken token = fToken.getAndSet(null);
        token.cancel();
    }
}
