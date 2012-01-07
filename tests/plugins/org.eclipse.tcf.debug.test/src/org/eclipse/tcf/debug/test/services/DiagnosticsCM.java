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
package org.eclipse.tcf.debug.test.services;

import org.eclipse.tcf.debug.test.util.ICache;
import org.eclipse.tcf.debug.test.util.TokenCache;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IDiagnostics;
import org.eclipse.tcf.services.IDiagnostics.ISymbol;

/**
 * 
 */
public class DiagnosticsCM extends AbstractCacheManager{
    private IDiagnostics fService;
    
    public DiagnosticsCM(IDiagnostics service) {
        fService = service;
    }
    
    @Override
    public void dispose() {
        super.dispose();
    }

    public ICache<String> echo(final String msg, Object clientId) {
        class MyCache extends TokenCache<String> implements IDiagnostics.DoneEcho {
            @Override
            protected IToken retrieveToken() {
                return fService.echo(msg, this);
            }
            public void doneEcho(IToken token, Throwable error, String s) {
                set(token, s, error);
            }
        };
        
        return mapCache(new CommandKey<MyCache>(MyCache.class, clientId) {
                @Override MyCache createCache() { return new MyCache(); }
            });
    }
    
    public ICache<String[]> getTestList() {
        class MyCache extends TokenCache<String[]> implements IDiagnostics.DoneGetTestList {
            @Override
            protected IToken retrieveToken() {
                return fService.getTestList(this);
            }
            public void doneGetTestList(IToken token, Throwable error, String[] list) {
                set(token, list, error);
            }
        };
        
        return mapCache(new Key<MyCache>(MyCache.class) {
                @Override MyCache createCache() { return new MyCache(); }
            });
    }
    
    public ICache<String> runTest(final String name, Object clientId) {
        class MyCache extends TokenCache<String> implements IDiagnostics.DoneRunTest {
            @Override
            protected IToken retrieveToken() {
                return fService.runTest(name, this);
            }
            public void doneRunTest(IToken token, Throwable error, String context_id) {
                set(token, context_id, error);
            }
        };
         
        return mapCache(new CommandKey<MyCache>(MyCache.class, clientId) {
                @Override MyCache createCache() { return new MyCache(); }
            });
    }

    public ICache<Object> cancelTest(final String context_id, Object clientId) {
        class MyCache extends TokenCache<Object> implements IDiagnostics.DoneCancelTest {
            @Override
            protected IToken retrieveToken() {
                return fService.cancelTest(context_id, this);
            }
            public void doneCancelTest(IToken token, Throwable error) {
                set(token, null, error);
            }
        };
        
        return mapCache(new CommandKey<MyCache>(MyCache.class, clientId) {
                @Override MyCache createCache() { return new MyCache(); }
            });        
    }

    abstract class SymbolKey<V> extends IdKey<V> {
        String fSymbolName;
        
        public SymbolKey(Class<V> clazz, String id, String symbolName) {
            super(clazz, id);
            fSymbolName = symbolName;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (super.equals(obj) && obj instanceof SymbolKey<?>) {
                return ((SymbolKey<?>)obj).fSymbolName.equals(fSymbolName);
            } 
            return false;        
        }
        
        @Override
        public int hashCode() {
            return super.hashCode() + fSymbolName.hashCode();
        }
    }
    
    public ICache<IDiagnostics.ISymbol> getSymbol(final String context_id, final String symbol_name) {
        class MyCache extends TokenCache<IDiagnostics.ISymbol> implements IDiagnostics.DoneGetSymbol {
            @Override
            protected IToken retrieveToken() {
                return fService.getSymbol(context_id, symbol_name, this);
            }
            public void doneGetSymbol(IToken token, Throwable error, ISymbol symbol) {
                set(token, symbol, error);
            }
        };
        
        return mapCache(new SymbolKey<MyCache>(MyCache.class, context_id, symbol_name) {
                @Override MyCache createCache() { return new MyCache(); }
            });
    }
    
}
