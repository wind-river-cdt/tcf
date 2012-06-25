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

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.tcf.debug.test.services.ResetMap.IResettable;
import org.eclipse.tcf.debug.test.util.AbstractCache;
import org.eclipse.tcf.debug.test.util.ICache;
import org.eclipse.tcf.debug.test.util.TokenCache;
import org.eclipse.tcf.debug.test.util.TransactionCache;
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
    

    abstract private class SymbolCache<V> extends TransactionCache<V> {
        protected final AbstractCache<V> fInner;
        
        public SymbolCache(AbstractCache<V> inner) {
            fInner = inner;
        }
        
        abstract protected String getSymbolId();

        @Override
        protected V process() throws InvalidCacheException, ExecutionException {
            validate(fInner);
            Symbol sym = validate( getContext(getSymbolId()) );
            addPending(sym, fInner);
            RunControlContext rcContext = validate(fRunControlCM.getContext(sym.getOwnerID()));
            addValid(sym, rcContext, fInner);
            return validate(fInner);            
        }
    }
        
    private void addPending(Symbol sym, IResettable cache) {
        if (sym.getUpdatePolicy() == ISymbols.UPDATE_ON_EXE_STATE_CHANGES) {
            fRunControlResetMap.addPending(cache);
        }
        fMemoryResetMap.addPending(cache);
    }
    
    private void addValid(Symbol sym, RunControlContext rcContext, IResettable cache) {
        if (sym.getUpdatePolicy() == ISymbols.UPDATE_ON_EXE_STATE_CHANGES) {
            String ownerId = sym.getOwnerID();
            fRunControlResetMap.addValid(ownerId, cache);
        }
        fMemoryResetMap.addValid(rcContext.getProcessID(), cache);
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

    public ICache<Symbol> getContext(final String id) {
        class MyCache extends TransactionCache<Symbol> {

            class InnerCache extends TokenCache<Symbol> implements ISymbols.DoneGetContext{
                @Override
                protected IToken retrieveToken() {
                    return fService.getContext(id, this);
                }
                
                @Override
                public void doneGetContext(IToken token, Exception error, Symbol context) {
                    set(token, context, error);
                }
            };
            
            private final InnerCache fInner = new InnerCache();
            
            @Override
            protected Symbol process() throws InvalidCacheException, ExecutionException {
                Symbol sym = validate(fInner);
                addPending(sym, fInner);
                RunControlContext rcContext = validate(fRunControlCM.getContext(sym.getOwnerID()));
                addValid(sym, rcContext, fInner);
                return validate(fInner);
            }
        };

        return mapCache(new IdKey<MyCache>(MyCache.class, id) {
            @Override MyCache createCache() { return new MyCache(); }
        });
    }

    public ICache<Map<String, Object>> getLocationInfo(final String symbol_id) {

        class InnerCache extends TokenCache<Map<String,Object>> implements ISymbols.DoneGetLocationInfo {
            @Override
            protected IToken retrieveToken() {
                return fService.getLocationInfo(symbol_id, this);
            }
            
            public void doneGetLocationInfo(IToken token, Exception error, Map<String,Object> props) {
                set(token, props, error);
            }
        }

        class MyCache extends SymbolCache<Map<String,Object>> {
            public MyCache() {
                super(new InnerCache());
            }
            @Override
            protected String getSymbolId() {
                return symbol_id;
            }
        }

        return mapCache(new IdKey<MyCache>(MyCache.class, symbol_id) {
            @Override MyCache createCache() { return new MyCache(); }
        });
    }

    /**
     * Client call back interface for getLocationInfo().
     */
    interface DoneGetLocationInfo {
        /**
         * Called when location information retrieval is done.
         * @param token - command handle.
         * @param error � error description if operation failed, null if succeeded.
         * @param props - symbol location properties, see LOC_*.
         */
        void doneGetLocationInfo(IToken token, Exception error, Map<String,Object> props);
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
