/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.test;

import java.util.Arrays;
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
    DataCallback<?>[] fRetrieveRms = new DataCallback<?>[NUM_CACHES];

    class TestCache extends CallbackCache<Integer> {
        
        final private int fIndex;
        
        public TestCache(int index) {
            fIndex = index;
        }

        @Override
        protected void retrieve(DataCallback<Integer> rm) {
            synchronized(TransactionTests.this) {
                fRetrieveRms[fIndex] = rm;
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
    
    /**
     * There's no rule on how quickly the cache has to start data retrieval
     * after it has been requested.  It could do it immediately, or it could
     * wait a dispatch cycle, etc..
     */
    private void waitForRetrieveRm(boolean all) {
        synchronized(this) {
            if (all) {
                while (Arrays.asList(fRetrieveRms).contains(null)) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            } else {
                while (fRetrieveRms[0] == null) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }
    }
    
    public void setUp() throws ExecutionException, InterruptedException {
        for (int i = 0; i < fTestCaches.length; i++) {
            fTestCaches[i] = new TestCache(i);
        }
    }   
    
    public void tearDown() throws ExecutionException, InterruptedException {
        fRetrieveRms = new DataCallback<?>[NUM_CACHES];
        fTestCaches = new TestCache[NUM_CACHES];
    }

    public void testSingleTransaction() throws InterruptedException, ExecutionException {
        final TestSingleTransaction testTransaction = new TestSingleTransaction();
        // Request data from cache
        Query<Integer> q = new Query<Integer>() { 
            @Override
            protected void execute(DataCallback<Integer> rm) {
                testTransaction.request(rm);
            }
        };
        q.invoke();
        
        // Wait until the cache starts data retrieval.
        waitForRetrieveRm(false);

        // Set the data to caches.  
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                ((DataCallback<Integer>)fRetrieveRms[0]).setData(1);
                fRetrieveRms[0].done();
            }
        });
        
        Assert.assertEquals(1, (int)q.get());
    }
    
    public void testSumTransaction() throws InterruptedException, ExecutionException {

        final TestSumTransaction testTransaction = new TestSumTransaction();
        // Request data from cache
        Query<Integer> q = new Query<Integer>() { 
            @Override
            protected void execute(DataCallback<Integer> rm) {
                testTransaction.request(rm);
            }
        };
        q.invoke();
        
        // Wait until the cache starts data retrieval.
        waitForRetrieveRm(true);

        
        // Set the data to caches.  
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                for (DataCallback<?> rm : fRetrieveRms) {
                    ((DataCallback<Integer>)rm).setData(1);
                    rm.done();
                }
            }
        });
        
        q.invoke();
        Assert.assertEquals(NUM_CACHES, (int)q.get());
    }

}
