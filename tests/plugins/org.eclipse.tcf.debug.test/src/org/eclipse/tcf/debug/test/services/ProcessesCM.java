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
package org.eclipse.tcf.debug.test.services;

import org.eclipse.tcf.debug.test.util.ICache;
import org.eclipse.tcf.debug.test.util.TokenCache;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IProcesses;
import org.eclipse.tcf.services.IProcesses.ProcessContext;

/**
 * 
 */
public class ProcessesCM extends AbstractCacheManager implements IProcesses.ProcessesListener  {
    
    private IProcesses fService;
    private final ResetMap fResetMap = new ResetMap();
    
    public ProcessesCM(IProcesses service) {
        fService = service;
        fService.addListener(this);
    }

    @Override
    public void dispose() {
        fService.removeListener(this);
        super.dispose();
    }

    public ICache<String[]> getChildren(final String id, final boolean attached_only) {
        class MyCache extends TokenCache<String[]> implements IProcesses.DoneGetChildren {
            @Override
            protected IToken retrieveToken() {
                return fService.getChildren(id, attached_only, this);
            }
            
            public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
                fResetMap.addValid(id, this);
                set(token, context_ids, error);
            }
        };

        class MyIdKey extends IdKey<MyCache> {

            private boolean fAttachedOnly = attached_only;

            public MyIdKey() {
                super(MyCache.class, id);
            }
            
            @Override MyCache createCache() { return new MyCache(); }
            
            @Override
            public boolean equals(Object obj) {
                return super.equals(obj) && fAttachedOnly == ((MyIdKey)obj).fAttachedOnly;
            }
            
            @Override
            public int hashCode() {
                return super.hashCode() + (fAttachedOnly ? Integer.MAX_VALUE / 2 : 0);
            }
        }
        
        return mapCache(new MyIdKey());
    }

    public ICache<ProcessContext> getContext(final String id) {
        class MyCache extends TokenCache<ProcessContext> implements IProcesses.DoneGetContext {
            @Override
            protected IToken retrieveToken() {
                return fService.getContext(id, this);
            }
            
            public void doneGetContext(IToken token, Exception error, ProcessContext context) {
                fResetMap.addValid(id, this);
                set(token, context, error);
            }
        };

        return mapCache(new IdKey<MyCache>(MyCache.class, id) {
            @Override MyCache createCache() { return new MyCache(); }
        });        
    }
    
    
    @Override
    public void exited(String process_id, int exit_code) {
        fResetMap.reset(process_id);
    }
    
}
