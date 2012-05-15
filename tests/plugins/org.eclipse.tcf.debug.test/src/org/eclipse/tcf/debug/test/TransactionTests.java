/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipse.tcf.debug.test.util.CallbackCache;
import org.eclipse.tcf.debug.test.util.DataCallback;
import org.eclipse.tcf.debug.test.util.Query;
import org.eclipse.tcf.debug.test.util.Transaction;
import org.eclipse.tcf.protocol.Protocol;

/**
 * Tests that exercise the Transaction object.
 */
public class TransactionTests extends TestCase {
    final static private int NUM_CACHES = 5; 
        
    TestCache[] fTestCaches = new TestCache[NUM_CACHES];
    Map<DataCallback<?>, Boolean> fRetrieveRms;

    class TestCache extends CallbackCache<Integer> {
        
        final private int fIndex;
        
        public TestCache(int index) {
            fIndex = index;
        }

        @Override
        protected void retrieve(DataCallback<Integer> rm) {
            synchronized(TransactionTests.this) {
                fRetrieveRms.put(rm, true);
                TransactionTests.this.notifyAll();
            }
        }
        
    }

    class TestSingleTransaction extends Transaction<Integer> {

        @Override
        protected Integer process() throws InvalidCacheException, ExecutionException {
            validate(fTestCaches[0]);
            return fTestCaches[0].getData();
        }
    }

    class TestSumTransaction extends Transaction<Integer> {
        @Override
        protected Integer process() throws InvalidCacheException, ExecutionException {
            validate(fTestCaches);
            
            int sum =  0;
            for (CallbackCache<Integer> cache : fTestCaches) {
                sum += cache.getData();
            }
            return sum;
        }
    }

    class TestSingleTransactionUnchecked extends Transaction<Integer> {

        @Override
        protected boolean processUnchecked() {
            if (!validateUnchecked(fTestCaches[0])) {
                return false;
            }
            assert fTestCaches[0].getError() == null;
            setData(fTestCaches[0].getData());
            return true;
        }
    }

    class TestSumTransactionUnchecked extends Transaction<Integer> {
        @Override
        protected boolean processUnchecked() {
            if (!validateUnchecked(fTestCaches)) {
                return false;
            }
            
            int sum =  0;
            for (CallbackCache<Integer> cache : fTestCaches) {
                assert cache.getError() == null;
                sum += cache.getData();
            }
            setData(sum);
            return true;
        }
    }

    class TestSumTransactionIterative extends Transaction<Integer> {
        @Override
        protected boolean processUnchecked() {
            int sum =  0;
            for (CallbackCache<Integer> cache : fTestCaches) {
                if (!validateUnchecked(cache)) {
                    return false;
                }
                assert cache.getError() == null;
                sum += cache.getData();
            }
            setData(sum);
            return true;
        }
    }
    
    /**
     * There's no rule on how quickly the cache has to start data retrieval
     * after it has been requested.  It could do it immediately, or it could
     * wait a dispatch cycle, etc..
     */
    private int waitForRetrieveRm() {
        synchronized(this) {
            while (!checkRetrieveRms()) {
                try {
                    wait(100);
                } catch (InterruptedException e) {
                    return NUM_CACHES;
                }
            }
            return fRetrieveRms.size(); 
        }
    }
    
    private boolean checkRetrieveRms() {
        if (fRetrieveRms.size() == NUM_CACHES) return true;
        
        boolean retVal = false;
        for (DataCallback<?> rm : fRetrieveRms.keySet()) { 
            if (fRetrieveRms.get(rm)) {
                retVal = true;
            }
        }
        return retVal;
    }
    
    public void setUp() throws ExecutionException, InterruptedException {
        fRetrieveRms = new HashMap<DataCallback<?>, Boolean>();
        for (int i = 0; i < fTestCaches.length; i++) {
            fTestCaches[i] = new TestCache(i);
        }
    }   
    
    public void tearDown() throws ExecutionException, InterruptedException {
        fTestCaches = new TestCache[NUM_CACHES];
    }

    private void doTestSingleTransaction(final Transaction<Integer> testTransaction) throws InterruptedException, ExecutionException {
        // Request data from cache
        Query<Integer> q = new Query<Integer>() { 
            @Override
            protected void execute(DataCallback<Integer> rm) {
                testTransaction.request(rm);
            }
        };
        q.invoke();
        
        // Wait until the cache starts data retrieval.
        waitForRetrieveRm();

        // Set the data to caches.  
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                @SuppressWarnings("unchecked")
                DataCallback<Integer> cb = ((DataCallback<Integer>)fRetrieveRms.keySet().iterator().next()); 
                cb.setData(1);
                cb.done();
            }
        });
        
        Assert.assertEquals(1, (int)q.get());
    }

    public void testSingleTransaction() throws InterruptedException, ExecutionException {
        doTestSingleTransaction(new TestSingleTransaction());
    }

    private void doTestSumTransaction(final Transaction<Integer> testTransaction) throws InterruptedException, ExecutionException {

        // Request data from cache
        Query<Integer> q = new Query<Integer>() { 
            @Override
            protected void execute(DataCallback<Integer> rm) {
                testTransaction.request(rm);
            }
        };
        q.invoke();
        
        
        int numRms = 0;
        while (numRms != NUM_CACHES) {
            // Wait until the cache starts data retrieval.
            numRms = waitForRetrieveRm();
            
            // Set the data to caches.  
            Protocol.invokeAndWait(new Runnable() {
                public void run() {
                    for (Iterator<DataCallback<?>> itr = fRetrieveRms.keySet().iterator(); itr.hasNext();) {
                        @SuppressWarnings("unchecked")
                        DataCallback<Integer> rm =((DataCallback<Integer>)itr.next()); 
                        if (fRetrieveRms.get(rm)) {
                            rm.setData(1);
                            rm.done();
                            fRetrieveRms.put(rm, false);
                            itr = fRetrieveRms.keySet().iterator();
                        }
                    }
                }
            });
        }
        
        q.invoke();
        Assert.assertEquals(NUM_CACHES, (int)q.get());
    }

    public void testSumTransaction() throws InterruptedException, ExecutionException {
        doTestSumTransaction(new TestSumTransaction());
    }

    public void testSingleTransactionUnchecked() throws InterruptedException, ExecutionException {
        doTestSingleTransaction(new TestSingleTransactionUnchecked());
    }

    public void testSumTransactionUnchecked() throws InterruptedException, ExecutionException {
        doTestSumTransaction(new TestSumTransactionUnchecked());
    }
    
    
    public void testSumTransactionIterative() throws InterruptedException, ExecutionException {

        doTestSumTransaction(new TestSumTransactionIterative());
       
    }

}
