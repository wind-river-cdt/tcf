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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.eclipse.tcf.debug.test.services.ResetMap.IResettable;
import org.eclipse.tcf.debug.test.util.AbstractCache;
import org.eclipse.tcf.debug.test.util.CallbackCache;
import org.eclipse.tcf.debug.test.util.DataCallback;
import org.eclipse.tcf.debug.test.util.ICache;
import org.eclipse.tcf.debug.test.util.TokenCache;
import org.eclipse.tcf.debug.test.util.Transaction;
import org.eclipse.tcf.debug.test.util.Transaction.InvalidCacheException;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IMemoryMap;
import org.eclipse.tcf.services.IMemoryMap.MemoryMapListener;
import org.eclipse.tcf.services.IRunControl.RunControlContext;
import org.eclipse.tcf.services.IRunControl.RunControlListener;
import org.eclipse.tcf.services.ISymbols;
import org.eclipse.tcf.services.ISymbols.Symbol;

/**
 * 
 */
public class SymbolsCM extends AbstractCacheManager {
    
    private ResetMap fRunControlResetMap = new ResetMap();
    private ResetMap fMemoryResetMap = new ResetMap();
    
    private ISymbols fService;
    private IMemoryMap fMemoryMap;
    private RunControlCM fRunControlCM;
    
    public SymbolsCM(ISymbols service, RunControlCM runControl, IMemoryMap memoryMap) {
        fService = service;
        fRunControlCM = runControl;
        fRunControlCM.addListener(fRunControlListener);
        fMemoryMap = memoryMap;
        fMemoryMap.addListener(fMemoryListener);
    }

    @Override
    public void dispose() {
        fRunControlCM.removeListener(fRunControlListener);
        fMemoryMap.removeListener(fMemoryListener);
        super.dispose();
    }
    
    private static final List<String> ANY_ID_PARENTS = new ArrayList<String>(1);
    {
        ANY_ID_PARENTS.add(ResetMap.ANY_ID);
    }

    abstract private class SymbolCache<V> extends CallbackCache<V> implements IResettable {
        protected final AbstractCache<V> fInner;
        private Symbol fSymbol;
        private List<String> fParents = new ArrayList<String>(4);
        
        public SymbolCache(AbstractCache<V> inner) {
            fInner = inner;
        }
        
        public void reset() {
            super.reset();
            if (fInner.isValid()) fInner.reset();
        }
        
        abstract protected String getSymbolId();
        
        @Override
        protected void retrieve(final DataCallback<V> rm) {
            fRunControlResetMap.addPending(this);
            fMemoryResetMap.addPending(this);
            Transaction<V> transaction = new Transaction<V>() {
                protected V process() throws InvalidCacheException, ExecutionException {
                    V retVal = processInner(this);
                    fSymbol = processSymbol(this);
                    fParents = processParents(this);
                    return retVal;            
                }
            };
            transaction.request(rm);
        }

        protected V processInner(Transaction<V> t) throws InvalidCacheException, ExecutionException {
            return t.validate(fInner);
        }
        
        protected Symbol processSymbol(Transaction<V> t) throws InvalidCacheException, ExecutionException {
            return t.validate( getContext(getSymbolId()) );
        }
        
        protected List<String> processParents(Transaction<V> t) throws InvalidCacheException, ExecutionException {
            List<String> parents = new ArrayList<String>(2);
            String rcContextId = fSymbol.getOwnerID(); 
            while( rcContextId != null ) {
                parents.add(rcContextId);
                RunControlContext rcContext = t.validate( fRunControlCM.getContext(rcContextId) );
                rcContextId = rcContext.getParentID();
            }
            return parents;
        }
        
        @Override
        protected void handleCompleted(V data, Throwable error, boolean canceled) {
            if (canceled) return;
            
            // If we cannot retrieve the symbol's context.  Reset the cache on 
            // any rc event.
            List<String> parents = ANY_ID_PARENTS;
            int updatePolicy = ISymbols.UPDATE_ON_EXE_STATE_CHANGES;
            if (error == null) {
                parents = fParents;
                updatePolicy = fSymbol.getUpdatePolicy();
            } 
            updateRunControlResetMap(parents, updatePolicy, data, error);
            updateMemoryMapResetMap(parents, data, error);
        }
        
        private void updateRunControlResetMap(List<String> parents, int updatePolicy, V data, Throwable error) {
            Set<String> pendingIds = fRunControlResetMap.removePending(this);
            if (updatePolicy == ISymbols.UPDATE_ON_EXE_STATE_CHANGES) {
                String ownerId = parents.get(0);
                if (pendingIds.contains(ownerId) || (ResetMap.ANY_ID.equals(ownerId) && !pendingIds.isEmpty())) {
                    // Reset cache immediately after setting value.
                    set(data, error, false);
                } else {
                    fRunControlResetMap.addValid(ownerId, this);
                    set(data, error, true);
                }
            }
        }

        private void updateMemoryMapResetMap(List<String> parents, V data, Throwable error) {
            Set<String> pendingIds = fMemoryResetMap.removePending(this);
            boolean resetPending = false;
            if (!pendingIds.isEmpty()) {
                if (ResetMap.ANY_ID.equals(parents.get(0))) {
                    resetPending = true;
                } else {
                    for (String parent : parents) {
                        if (pendingIds.contains(parent)) {
                            resetPending = true;
                        }
                    }
                }
            }
            
            if (resetPending) {
                // Reset cache immediately after setting value.
                set(data, error, false);
            } else {
                fMemoryResetMap.addValid(parents, this);
                set(data, error, true);
            }
        }
    }
    
    private class ChildrenCache extends SymbolCache<String[]> {
        public ChildrenCache(InnerChildrenCache inner) {
            super(inner);
        }
        
        @Override
        protected String getSymbolId() {
            return ((InnerChildrenCache)fInner).fId;
        }
    }        
    
    private class InnerChildrenCache extends TokenCache<String[]> implements ISymbols.DoneGetChildren {
        private final String fId;
        public InnerChildrenCache(String id) {
            fId = id;
        }
        
        @Override
        protected IToken retrieveToken() {
            fRunControlResetMap.addPending(this);
            return fService.getChildren(fId, this);
        }
        public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
            set(token, context_ids, error);
        }
    };

    private class ChildrenCacheKey extends IdKey<ChildrenCache> {
        public ChildrenCacheKey(String id) {
            super(ChildrenCache.class, id);
        }
        @Override ChildrenCache createCache() { return new ChildrenCache( new InnerChildrenCache(fId) ); }        
    }
    
    public ICache<String[]> getChildren(String id) {
        return mapCache(new ChildrenCacheKey(id));
    }

    private class ContextCache extends SymbolCache<Symbol> {
        public ContextCache(InnerContextCache inner) {
            super(inner);
        }
        @Override
        protected String getSymbolId() {
            return fInner.getData().getID();
        }
        @Override
        protected Symbol processSymbol(Transaction<Symbol> t) throws InvalidCacheException, ExecutionException {
            return fInner.getData();
        }
    }

    class InnerContextCache extends TokenCache<Symbol> implements ISymbols.DoneGetContext {
        private final String fId;
        
        public InnerContextCache(String id) {
            fId = id;
        }
        @Override
        protected IToken retrieveToken() {
            return fService.getContext(fId, this);
        }
        public void doneGetContext(IToken token, Exception error, Symbol symbol) {
            set(token, symbol, error);
        }
        public void resetContext() {
            if (isValid()) reset();
        }
    }
    
    private class ContextCacheKey extends IdKey<ContextCache> {
        public ContextCacheKey(String id) {
            super(ContextCache.class, id);
        }
        @Override ContextCache createCache() { return new ContextCache( new InnerContextCache(fId)); }        
    }
    
    public ICache<Symbol> getContext(String id) {
        return mapCache(new ContextCacheKey(id));
    }

    private class FindCache extends SymbolCache<String> {
        public FindCache(InnerFindCache inner) {
            super(inner);
        }
        @Override
        protected String getSymbolId() {
            return fInner.getData();
        }
    }

    class InnerFindCache extends TokenCache<String> implements ISymbols.DoneFind {
        private final String fId;
        private final Number fIp;
        private final String fName;
        
        public InnerFindCache(String id, Number ip, String name) {
            fId = id;
            fIp = ip;
            fName = name;
        }
        @Override
        protected IToken retrieveToken() {
            return fService.find(fId, fIp, fName, this);
        }
        
        public void doneFind(IToken token, Exception error, String symbol_id) {
            set(token, symbol_id, error);
        }
    }
    
    private class FindCacheKey extends IdKey<FindCache> {
        private final Number fIp;
        private final String fName;        
        
        public FindCacheKey(String id, Number ip, String name) {
            super(FindCache.class, id);
            fIp = ip;
            fName = name;
        }
        @Override FindCache createCache() { return new FindCache(new InnerFindCache(fId, fIp, fName)); }
        
        @Override
        public boolean equals(Object obj) {
            if (super.equals(obj) && obj instanceof FindCacheKey) {
                FindCacheKey other = (FindCacheKey)obj;
                return fIp.equals(other.fIp) && fName.equals(other.fName);
            }
            return false;
        }
        @Override
        public int hashCode() {
            return super.hashCode() + fIp.hashCode() + fName.hashCode();
        }
    }

    public ICache<String> find(String context_id, Number ip, String name) {
        return mapCache(new FindCacheKey(context_id, ip, name));
    }

    private class FindByAddrCache extends SymbolCache<String>  {
        
        public FindByAddrCache(InnerFindByAddrCache inner) {
            super(inner);
        }
        
        @Override
        protected String getSymbolId() {
            return fInner.getData();
        }
    }

    private class InnerFindByAddrCache extends TokenCache<String> implements ISymbols.DoneFind {
        private final String fId;
        private final Number fAddr;
        
        public InnerFindByAddrCache(String id, Number addr) {
            fId = id;
            fAddr = addr;
        }
        @Override
        protected IToken retrieveToken() {
            return fService.findByAddr(fId, fAddr, this);
        }
        
        public void doneFind(IToken token, Exception error, String symbol_id) {
            set(token, symbol_id, error);
        }
    }
    
    private class FindByAddrCacheKey extends IdKey<FindByAddrCache> {
        private final Number fAddr;
        
        public FindByAddrCacheKey(String id, Number addr) {
            super(FindByAddrCache.class, id);
            fAddr = addr;
        }
        @Override FindByAddrCache createCache() { return new FindByAddrCache(new InnerFindByAddrCache(fId, fAddr)); }
        
        @Override
        public boolean equals(Object obj) {
            if (super.equals(obj) && obj instanceof FindByAddrCacheKey) {
                FindByAddrCacheKey other = (FindByAddrCacheKey)obj;
                return fAddr.equals(other.fAddr);
            }
            return false;
        }
        @Override
        public int hashCode() {
            return super.hashCode() + fAddr.hashCode();
        }
    }
    
    public ICache<String> findByAddr(String context_id, Number addr) {
        return mapCache(new FindByAddrCacheKey(context_id, addr));
    }

    private RunControlListener fRunControlListener = new RunControlListener() {
    
        public void contextAdded(RunControlContext[] contexts) {
        }
    
        public void contextChanged(RunControlContext[] contexts) {
            for (RunControlContext context : contexts) {
                resetRunControlContext(context.getID());
            }
        }
    
        public void contextRemoved(String[] context_ids) {
            for (String id : context_ids) {
                resetRunControlContext(id);
                fMemoryResetMap.reset(id);
            }
        }
    
        public void contextSuspended(String context, String pc, String reason, Map<String, Object> params) {
            resetRunControlContext(context);
        }
    
        public void contextResumed(String context) {
            resetRunControlContext(context);
        }
    
        public void containerSuspended(String context, String pc, String reason, Map<String, Object> params,
            String[] suspended_ids) 
        {
            for (String id : suspended_ids) {
                resetRunControlContext(id);
            }
        }
    
        public void containerResumed(String[] context_ids) {
            for (String id : context_ids) {
                resetRunControlContext(id);
            }
        }
    
        public void contextException(String context, String msg) {
            resetRunControlContext(context);
        }
    };

    private void resetRunControlContext(String id) {
        fRunControlResetMap.reset(id);
    }

    private MemoryMapListener fMemoryListener = new MemoryMapListener() {
        public void changed(String context_id) {
            fMemoryResetMap.reset(context_id);
        }
    };
}
