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

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipse.tcf.debug.test.util.Callback;
import org.eclipse.tcf.debug.test.util.DataCallback;
import org.eclipse.tcf.debug.test.util.ICache;
import org.eclipse.tcf.debug.test.util.Query;
import org.eclipse.tcf.debug.test.util.RangeCache;
import org.eclipse.tcf.protocol.Protocol;

/**
 * Tests that exercise the DataCache object.
 */
public class RangeCacheTests extends TestCase {

    class TestRangeCache extends RangeCache<Integer> {
        
        @Override
        protected void retrieve(long offset, int count, DataCallback<List<Integer>> rm) {
            fRetrieveInfos.add(new RetrieveInfo(offset, count, rm));
        }
        
        @Override
        public void reset() {
            super.reset();
        }

    }

    class TestQuery extends Query<List<Integer>> {
        long fOffset;
        int fCount;
        TestQuery(long offset, int count) {
            fOffset = offset;
            fCount = count;
        }        
        
        @Override
        protected void execute(final DataCallback<List<Integer>> rm) {
            fRangeCache = fTestCache.getRange(fOffset, fCount);
            if (fRangeCache.isValid()) {
                rm.setData(fRangeCache.getData());
                rm.setError(fRangeCache.getError());
                rm.done();
            } else {
                fRangeCache.wait(new Callback(rm) {
                    @Override
                    protected void handleSuccess() {
                        rm.setData(fRangeCache.getData());
                        rm.done();
                    }
                });
            }
        }
    }

    class RetrieveInfo implements Comparable<RetrieveInfo> {
        long fOffset;
        int fCount;
        DataCallback<List<Integer>> fRm;
        RetrieveInfo(long offset, int count, DataCallback<List<Integer>> rm) {
            fOffset = offset;
            fCount = count;
            fRm = rm;
        }
        
        public int compareTo(RetrieveInfo o) {
            if (fOffset > o.fOffset) {
                return 1;
            } else if (fOffset == o.fOffset) {
                return 0;
            } else /*if (fOffset < o.fOffset)*/ {
                return -1;
            }
        }
    }

    TestRangeCache fTestCache;    
    SortedSet<RetrieveInfo> fRetrieveInfos;
    ICache<List<Integer>> fRangeCache;
    
    private List<Integer> makeList(long offset, int count) {
        List<Integer> list = new ArrayList<Integer>(count);
        for (int i = 0; i < count; i++) {
            list.add((int)(i + offset));
        }
        return list;
    }
    
    /**
     * There's no rule on how quickly the cache has to start data retrieval
     * after it has been requested.  It could do it immediately, or it could
     * wait a dispatch cycle, etc..
     */
    private void waitForRetrieveRm(int size) {
        synchronized(this) {
            while (fRetrieveInfos.size() < size) {
                try {
                    wait(100);
                } catch (InterruptedException e) {
                    return;
                } 
            }
        }
    }
    
    protected void setUp() throws Exception {
        fTestCache = new TestRangeCache();
        fRetrieveInfos = new TreeSet<RetrieveInfo>();
        fRangeCache = null;
    }   
    
    protected void tearDown() throws ExecutionException, InterruptedException {
        fTestCache = null;
    }

    private void assertCacheValidWithData(ICache<List<Integer>> cache, long offset, int count) {
        Assert.assertTrue(cache.isValid());
        Assert.assertEquals(makeList(offset, count), cache.getData());
        Assert.assertTrue(cache.getError() == null);
    }

    private void assertCacheValidWithError(ICache<List<Integer>> cache) {
        Assert.assertTrue(cache.isValid());
        Assert.assertFalse(cache.getError() == null);
    }
    
    private void assertCacheWaiting(ICache<List<Integer>> cache) {
        Assert.assertFalse(cache.isValid());
        try {
            cache.getData();
            Assert.fail("Expected an IllegalStateException");
        } catch (IllegalStateException e) {}
        try {
            cache.getError();
            Assert.fail("Expected an IllegalStateException");
        } catch (IllegalStateException e) {}
    }

    private void completeInfo(final RetrieveInfo info, final long offset, final int count) {
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                info.fRm.setData(makeList(offset, count));
                info.fRm.done();
            }
        });
    }
            
    private void getRange(long queryOffset, int queryCount, long[] retrieveOffsets, int retrieveCounts[]) throws InterruptedException, ExecutionException {
        assert retrieveOffsets.length == retrieveCounts.length;
        int retrieveCount = retrieveOffsets.length;
        
        // Request data from cache
        TestQuery q = new TestQuery(queryOffset, queryCount);

        fRangeCache = null;
        fRetrieveInfos.clear();

        q.invoke();
        
        // Wait until the cache requests the data.
        waitForRetrieveRm(retrieveOffsets.length);
        
        if (retrieveCount != 0) {
            assertCacheWaiting(fRangeCache);
            
            // Set the data
            Assert.assertEquals(retrieveCount, fRetrieveInfos.size());
            int i = 0; 
            for (RetrieveInfo info : fRetrieveInfos) {
                completeInfo(info, retrieveOffsets[i], retrieveCounts[i]);
                i++;
            }
        }
        
        // Wait for data.
        Assert.assertEquals(makeList(queryOffset, queryCount), q.get());
        
        // Check state while waiting for data
        assertCacheValidWithData(fRangeCache, queryOffset, queryCount);
    }
    
    public void testGetOneRangeTest() throws InterruptedException, ExecutionException {
        getRange(0, 100, new long[] { 0 }, new int[] { 100 });
    }
    
    public void testGetMultipleRangesTest() throws InterruptedException, ExecutionException {
        // Retrieve a range in-between two cached ranges
        getRange(0, 100, new long[] { 0 }, new int[] { 100 });
        getRange(200, 100, new long[] { 200 }, new int[] { 100 });
        getRange(0, 300, new long[] { 100 }, new int[] { 100 });

        // Retrieve a range overlapping two cached ranges
        getRange(1000, 100, new long[] { 1000 }, new int[] { 100 });
        getRange(1200, 100, new long[] { 1200 }, new int[] { 100 });
        getRange(900, 500, new long[] { 900, 1100, 1300 }, new int[] { 100, 100, 100 });

        // Retrieve a range that's a subset of a cached range.
        getRange(2000, 100, new long[] { 2000 }, new int[] { 100 });
        getRange(2000, 50, new long[] {}, new int[] {});
        getRange(2025, 50, new long[] {}, new int[] {});
        getRange(2050, 50, new long[] {}, new int[] {});
    }

    public void testGetIncompleteRangeTest() throws InterruptedException, ExecutionException {
        long queryOffset = 0;
        int queryCount = 100;
        long[] retrieveOffsets = new long[] { 0 };
        int retrieveCounts[] = new int[] { 50 };
    
        assert retrieveOffsets.length == retrieveCounts.length;
        int retrieveCount = retrieveOffsets.length;
        
        // Request data from cache
        TestQuery q = new TestQuery(queryOffset, queryCount);

        fRangeCache = null;
        fRetrieveInfos.clear();

        q.invoke();
        
        // Wait until the cache requests the data.
        waitForRetrieveRm(retrieveOffsets.length);
        
        if (retrieveCount != 0) {
            assertCacheWaiting(fRangeCache);
            
            // Set the data
            Assert.assertEquals(retrieveCount, fRetrieveInfos.size());
            int i = 0; 
            for (RetrieveInfo info : fRetrieveInfos) {
                completeInfo(info, retrieveOffsets[i], retrieveCounts[i]);
                i++;
            }
        }
        
        // Wait for data.
        List<Integer> data = q.get();
        Assert.assertEquals(100, data.size());
        for (int i = 50; i < 100; i++) {
            Assert.assertEquals(null, data.get(i));
        }
        
        // Check state while waiting for data
        assertCacheValidWithData(fRangeCache, 0, 50);
    }

    
    private void cancelRange(long queryOffset, int queryCount, long[] retrieveOffsets, int retrieveCounts[]) throws Exception {
        int retrieveCount = retrieveOffsets.length;
        
        // Request data from cache
        TestQuery q = new TestQuery(queryOffset, queryCount);

        fRangeCache = null;
        fRetrieveInfos.clear();
        
        q.invoke();
        
        // Wait until the cache requests the data.
        waitForRetrieveRm(retrieveCount);
        
        assertCacheWaiting(fRangeCache);

        // Set the data without using an executor.
        Assert.assertEquals(retrieveCount, fRetrieveInfos.size());
        int i = 0; 
        for (RetrieveInfo info : fRetrieveInfos) {
            Assert.assertEquals(retrieveOffsets[i], info.fOffset);
            Assert.assertEquals(retrieveCounts[i], info.fCount);
            Assert.assertFalse(info.fRm.isCanceled());
            i++;
        }
        
        q.cancel(true);
        try {
            q.get();
            Assert.fail("Expected a cancellation exception");
        } catch (CancellationException e) {} // Expected exception;
        
        for (RetrieveInfo info : fRetrieveInfos) {
            Assert.assertTrue(info.fRm.isCanceled());
        }
    }

    public void testCancelOneRangeTest() throws Exception {
        cancelRange(0, 100, new long[] { 0 }, new int[] { 100 });
    }

    public void testCancelMultipleRangesTest() throws Exception {
        // Cancel a couple of ranges.
        cancelRange(0, 100, new long[] { 0 }, new int[] { 100 });
        cancelRange(200, 100, new long[] { 200 }, new int[] { 100 });
        
        // Cancel a range overlapping two previously canceled ranges.
        cancelRange(0, 300, new long[] { 0 }, new int[] { 300 });
    }

    public void testGetAndCancelMultipleRangesTest() throws Exception {
        // Cancel a range, then retrieve the same range 
        cancelRange(0, 100, new long[] { 0 }, new int[] { 100 });
        getRange(0, 100, new long[] { 0 }, new int[] { 100 });
        
        // Cancel a range overlapping a cached range.
        cancelRange(0, 200, new long[] { 100 }, new int[] { 100 });
    }

    public void testResetOneRangeTest() throws InterruptedException, ExecutionException {
        getRange(0, 100, new long[] { 0 }, new int[] { 100 });
        
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                fTestCache.reset();
            };
        });
        
        getRange(0, 100, new long[] { 0 }, new int[] { 100 });
    }
    
    public void testResetMultipleRangesTest() throws InterruptedException, ExecutionException {
        // Retrieve a range in-between two cached ranges
        getRange(0, 100, new long[] { 0 }, new int[] { 100 });
        getRange(200, 100, new long[] { 200 }, new int[] { 100 });
        getRange(0, 300, new long[] { 100 }, new int[] { 100 });

        // Retrieve a range overlapping two cached ranges
        getRange(1000, 100, new long[] { 1000 }, new int[] { 100 });
        getRange(1200, 100, new long[] { 1200 }, new int[] { 100 });
        getRange(900, 500, new long[] { 900, 1100, 1300 }, new int[] { 100, 100, 100 });

        // Retrieve a range that's a subset of a cached range.
        getRange(2000, 100, new long[] { 2000 }, new int[] { 100 });
        getRange(2000, 50, new long[] {}, new int[] {});
        getRange(2025, 50, new long[] {}, new int[] {});
        getRange(2050, 50, new long[] {}, new int[] {});
        
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                fTestCache.reset();
            };
        });
        
        // Retrieve a range in-between two cached ranges
        getRange(0, 100, new long[] { 0 }, new int[] { 100 });
        getRange(200, 100, new long[] { 200 }, new int[] { 100 });
        getRange(0, 300, new long[] { 100 }, new int[] { 100 });

        // Retrieve a range overlapping two cached ranges
        getRange(1000, 100, new long[] { 1000 }, new int[] { 100 });
        getRange(1200, 100, new long[] { 1200 }, new int[] { 100 });
        getRange(900, 500, new long[] { 900, 1100, 1300 }, new int[] { 100, 100, 100 });

        // Retrieve a range that's a subset of a cached range.
        getRange(2000, 100, new long[] { 2000 }, new int[] { 100 });
        getRange(2000, 50, new long[] {}, new int[] {});
        getRange(2025, 50, new long[] {}, new int[] {});
        getRange(2050, 50, new long[] {}, new int[] {});
    }

    public void testResetWhileInvalidTest() throws InterruptedException, ExecutionException {
        // Request data from cache
        TestQuery q = new TestQuery(10, 100);

        fRangeCache = null;
        fRetrieveInfos.clear();
        
        q.invoke();
        
        // Wait until the cache requests the data.
        waitForRetrieveRm(1);
        
        assertCacheWaiting(fRangeCache);
        
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                fTestCache.reset();
            };
        });
       
        // Set the data without using an executor.
        Assert.assertEquals(1, fRetrieveInfos.size());
        completeInfo(fRetrieveInfos.first(), 10, 100);
        
        // Wait for data.
        Assert.assertEquals(makeList(10, 100), q.get());
        
        // Check state while waiting for data
        assertCacheValidWithData(fRangeCache, 10, 100);
    }

    public void testSetRangeErrorTest() throws InterruptedException, ExecutionException {
        
        // Retrieve a range of data.
        getRange(0, 100, new long[] { 0 }, new int[] { 100 });
        
        // Force an error status into the range cache.
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                fTestCache.set(0, 100, null, new Throwable( "Cache invalid" ));
            };
        });

        // Retrieve a range cache and check that it immediately contains the error status in it.
        fRangeCache = null;
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                fRangeCache = fTestCache.getRange(0, 100);
            }
        });

        assertCacheValidWithError(fRangeCache);
        
        // Request an update from the range and check for exception. 
        TestQuery q = new TestQuery(10, 90);

        fRangeCache = null;
        fRetrieveInfos.clear();
        
        q.invoke();

        try {
            q.get();
            Assert.fail("Expected an ExecutionException");            
        } catch (ExecutionException e) {}
    }

    public void testGetOneRangeUsingDifferentRangeInstanceTest() throws InterruptedException, ExecutionException {
        // Request data from cache
        TestQuery q = new TestQuery(0, 100);

        fRangeCache = null;
        fRetrieveInfos.clear();
        
        q.invoke();
        
        // Wait until the cache requests the data.
        waitForRetrieveRm(1);
        
        assertCacheWaiting(fRangeCache);
        
        // Set the data without using an executor.
        Assert.assertEquals(1, fRetrieveInfos.size());
        RetrieveInfo info = fRetrieveInfos.iterator().next();
        completeInfo(info, 0, 100);

        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                fRangeCache = fTestCache.getRange(0, 100);
            }
        });
        
        // Check state while waiting for data
        assertCacheValidWithData(fRangeCache, 0, 100);
    }
    
    
    
}
