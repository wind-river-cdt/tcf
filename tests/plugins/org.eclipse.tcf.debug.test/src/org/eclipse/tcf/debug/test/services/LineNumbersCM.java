/*******************************************************************************sbsb
 * Copyright (c) 2012 Wind River Systems and others.
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

import org.eclipse.tcf.debug.test.util.ICache;
import org.eclipse.tcf.debug.test.util.TokenCache;
import org.eclipse.tcf.debug.test.util.TransactionCache;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.ILineNumbers;
import org.eclipse.tcf.services.ILineNumbers.CodeArea;
import org.eclipse.tcf.services.IMemoryMap;
import org.eclipse.tcf.services.IMemoryMap.MemoryMapListener;
import org.eclipse.tcf.services.IRunControl.RunControlContext;
import org.eclipse.tcf.services.IRunControl.RunControlListener;

/**
 * 
 */
public class LineNumbersCM extends AbstractCacheManager {

    private ResetMap fMemContextResetMap = new ResetMap();
    private ILineNumbers fService;
    private IMemoryMap fMemoryMap;
    private RunControlCM fRunControlCM;

    public LineNumbersCM(ILineNumbers lineNumbers, IMemoryMap memMap, RunControlCM runControlCM) {
        fService = lineNumbers;
        fMemoryMap = memMap;
        fMemoryMap.addListener(fMemoryMapListener);
        fRunControlCM = runControlCM;
        fRunControlCM.addListener(fRunControlListener);
    }
    
    @Override
    public void dispose() {
        fRunControlCM.removeListener(fRunControlListener);
        fMemoryMap.removeListener(fMemoryMapListener);
        super.dispose();
    }
     
    abstract private class MapToSourceKey<V> extends IdKey<V> {
        private final Number fStartAdddress;        
        private final Number fEndAddress; 
        
        public MapToSourceKey(Class<V> cacheClass, String id, Number startAddress, Number endAddress) {
            super(cacheClass, id);
            fStartAdddress = startAddress;
            fEndAddress = endAddress;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (super.equals(obj) && obj instanceof MapToSourceKey<?>) {
                MapToSourceKey<?> other = (MapToSourceKey<?>)obj;
                return fStartAdddress.equals(other.fStartAdddress) && fEndAddress.equals(other.fEndAddress);
            }
            return false;
        }
        @Override
        public int hashCode() {
            return super.hashCode() + fStartAdddress.hashCode() + fEndAddress.hashCode();
        }
    }
    
    public ICache<CodeArea[]> mapToSource(final String context_id, final Number start_address, final Number end_address) {
        class MyCache extends TransactionCache<ILineNumbers.CodeArea[]> {

            @Override
            protected CodeArea[] process() throws InvalidCacheException, ExecutionException {
                RunControlContext rcContext = validate(fRunControlCM.getContext(context_id));
                String mem_id = rcContext.getProcessID();
                if (mem_id == null) {
                    // TODO: is this the correct fall-back.  Should we save the parent ID for reset?
                    mem_id = context_id;
                }
                return validate( doMapToSource(mem_id, start_address, end_address) );
            }
        }
        
        return mapCache(new MapToSourceKey<MyCache>(MyCache.class, context_id, start_address, end_address) {
            @Override MyCache createCache() { return new MyCache(); }
        });
    }

    private ICache<CodeArea[]> doMapToSource(final String mem_id, final Number start_address, final Number end_address) {
        class MyCache extends TokenCache<CodeArea[]> implements ILineNumbers.DoneMapToSource {
            @Override
            protected IToken retrieveToken() {
                return fService.mapToSource(mem_id, start_address, end_address, this);
            }
            
            public void doneMapToSource(IToken token, Exception error, CodeArea[] areas) {
                fMemContextResetMap.addValid(mem_id, this);
                set(token, areas, error);
            }
        };
        
        return mapCache(new MapToSourceKey<MyCache>(MyCache.class, mem_id, start_address, end_address) {
                @Override MyCache createCache() { return new MyCache(); }
            });
    }

    abstract private class MapToMemoryKey<V> extends IdKey<V> {
        private final String fFile;        
        private final int fLine; 
        private final int fColumn;
        
        public MapToMemoryKey(Class<V> cacheClass, String id, String file, int line, int col) {
            super(cacheClass, id);
            fFile = file;
            fLine = line;
            fColumn = col;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (super.equals(obj) && obj instanceof MapToMemoryKey<?>) {
                MapToMemoryKey<?> other = (MapToMemoryKey<?>)obj;
                return fFile.equals(other.fFile) && fLine == other.fLine && fColumn == other.fColumn;
            }
            return false;
        }
        @Override
        public int hashCode() {
            return super.hashCode() + fFile.hashCode()^fLine^(fColumn + 1);
        }
    }

    public ICache<CodeArea[]> mapToMemory(final String context_id, final String file, final int line, final int column) {
        class MyCache extends TransactionCache<ILineNumbers.CodeArea[]> {
            private String fId = context_id;
            
            protected CodeArea[] process() throws InvalidCacheException, ExecutionException {
                RunControlContext rcContext = validate(fRunControlCM.getContext(fId));
                String mem_id = rcContext.getProcessID();
                if (mem_id == null) {
                    // TODO: is this the correct fall-back.  Should we save the parent ID for reset?
                    mem_id = fId;
                }
                return validate( doMapToMemory(mem_id, file, line, column) );
            }
        }            
        
        return mapCache(new MapToMemoryKey<MyCache>(MyCache.class, context_id, file, line, column) {
            @Override MyCache createCache() { return new MyCache(); }
        });        
    }
    
    private ICache<CodeArea[]> doMapToMemory(final String mem_id, final String file, final int line, final int column) {
        class MyCache extends TokenCache<CodeArea[]> implements ILineNumbers.DoneMapToMemory {
            @Override
            protected IToken retrieveToken() {
                return fService.mapToMemory(mem_id, file, line, column, this);
            }
            public void doneMapToMemory(IToken token, Exception error, CodeArea[] areas) {
                fMemContextResetMap.addValid(mem_id, this);
                set(token, areas, error);
            }
            
        };
        
        return mapCache(new MapToMemoryKey<MyCache>(MyCache.class, mem_id, file, line, column) {
                @Override MyCache createCache() { return new MyCache(); }
            });
        
    }

    interface DoneMapToMemory {
        void doneMapToMemory(IToken token, Exception error, CodeArea[] areas);
    }

    private RunControlListener fRunControlListener = new RunControlListener() {
        
        public void contextRemoved(String[] context_ids) {
            for (String id : context_ids) {
                resetContext(id);
            }
        }
    
        public void contextAdded(RunControlContext[] contexts) {}
        public void contextChanged(RunControlContext[] contexts) {}
        public void contextSuspended(String context, String pc, String reason, Map<String, Object> params) {}
        public void contextResumed(String context) {}
        public void containerSuspended(String context, String pc, String reason, Map<String, Object> params,
            String[] suspended_ids) {}
        public void containerResumed(String[] context_ids) {}
        public void contextException(String context, String msg) {}
    };

    private void resetContext(String id) {
        fMemContextResetMap.reset(id);
    }

    private MemoryMapListener fMemoryMapListener = new MemoryMapListener() {
        public void changed(String context_id) {
            resetContext(context_id);
        }
    };

}
