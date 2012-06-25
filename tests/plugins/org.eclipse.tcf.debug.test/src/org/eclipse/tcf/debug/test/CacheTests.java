/***********************************************s********************************
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
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipse.tcf.debug.test.util.Callback;
import org.eclipse.tcf.debug.test.util.CallbackCache;
import org.eclipse.tcf.debug.test.util.DataCallback;
import org.eclipse.tcf.debug.test.util.Query;
import org.eclipse.tcf.protocol.Protocol;

/**
 * Tests that exercise the DataCache object.
 */
public class CacheTests extends TestCase {

    TestCache fTestCache;
    DataCallback<Integer> fRetrieveRm;
    
    class TestCache extends CallbackCache<Integer> {
        
        @Override
        protected void retrieve(DataCallback<Integer> rm) {
            synchronized(CacheTests.this) {
                fRetrieveRm = rm;
                CacheTests.this.notifyAll();
            }
        }
        
        @Override
        protected void handleCompleted(Integer data, Throwable error, boolean canceled) {
            // TODO Auto-generated method stub
            super.handleCompleted(data, error, canceled);
        }
        
    }

    class TestQuery extends Query<Integer> {
        @Override
        protected void execute(final DataCallback<Integer> rm) {
            fTestCache.wait(new DataCallback<Integer>(rm) {
                @Override
                protected void handleSuccess() {
                    rm.setData(fTestCache.getData());
                    rm.done();
                }
            });
        }
    }
    
    /**
     * There's no rule on how quickly the cache has to start data retrieval
     * after it has been requested.  It could do it immediately, or it could
     * wait a dispatch cycle, etc..
     */
    private void waitForRetrieveRm() {
        synchronized(this) {
            while (fRetrieveRm == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }
    
    public void setUp() throws ExecutionException, InterruptedException {
        fTestCache = new TestCache();
    }   
    
    public void tearDown() throws ExecutionException, InterruptedException {
        fRetrieveRm = null;
        fTestCache = null;
    }

    private void assertCacheValidWithData(Object data) {
        Assert.assertTrue(fTestCache.isValid());
        Assert.assertEquals(data, fTestCache.getData());
        Assert.assertNull(fTestCache.getError());
    }

    private void assertCacheResetWithoutData() {
        Assert.assertFalse(fTestCache.isValid());
        try {
            fTestCache.getData();
            Assert.fail("Expected an IllegalStateException");
        } catch (IllegalStateException e) {}
        try {
            fTestCache.getError();
            Assert.fail("Expected an IllegalStateException");
        } catch (IllegalStateException e) {}
    }

    private void assertCacheValidWithoutData() {
        Assert.assertTrue(fTestCache.isValid());
        Assert.assertEquals(null, fTestCache.getData());
        Assert.assertNotNull(fTestCache.getError());
        Assert.assertEquals(fTestCache.getError(), ERROR_TARGET_RUNNING);
    }

    private void assertCacheWaiting() {
        Assert.assertFalse(fTestCache.isValid());
        try {
            fTestCache.getData();
            Assert.fail("Expected an IllegalStateException");
        } catch (IllegalStateException e) {}
        try {
            fTestCache.getError();
            Assert.fail("Expected an IllegalStateException");
        } catch (IllegalStateException e) {}
        Assert.assertFalse(fRetrieveRm.isCanceled());
    }

    private void assertCacheInvalidAndWithCanceledRM() {
        Assert.assertFalse(fTestCache.isValid());
        try {
            fTestCache.getData();
            Assert.fail("Expected an IllegalStateException");
        } catch (IllegalStateException e) {}
        try {
            fTestCache.getError();
            Assert.fail("Expected an IllegalStateException");
        } catch (IllegalStateException e) {}
        Assert.assertTrue(fRetrieveRm.isCanceled());
    }

    public void testGet() throws InterruptedException, ExecutionException {
        // Request data from cache
        Query<Integer> q = new TestQuery();
        
        // Check initial state
        Assert.assertFalse(fTestCache.isValid());

        q.invoke();
        
        // Wait until the cache requests the data.
        waitForRetrieveRm();
        
        // Check state while waiting for data
        Assert.assertFalse(fTestCache.isValid());

        // Complete the cache's retrieve data request.
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                fRetrieveRm.setData(1);
                fRetrieveRm.done();
    
                // Check that the data is available in the cache immediately
                // (in the same dispatch cycle).
                Assert.assertEquals(1, (int)fTestCache.getData());
                Assert.assertTrue(fTestCache.isValid());
            }
        });
        
        Assert.assertEquals(1, (int)q.get());
        
        // Re-check final state
        assertCacheValidWithData(1);
    }

    public void testGetWithCompletionDelay() throws InterruptedException, ExecutionException {
        // Check initial state
        Assert.assertFalse(fTestCache.isValid());
        
        // Request data from cache
        Query<Integer> q = new TestQuery();
        q.invoke();
        
        // Wait until the cache starts data retrieval.
        waitForRetrieveRm();

        // Check state while waiting for data
        Assert.assertFalse(fTestCache.isValid());

        // Set the data to the callback  
        Protocol.invokeLater(
            100, 
            new Runnable() {
                public void run() {
                    fRetrieveRm.setData(1);
                    fRetrieveRm.done();
                    
                }
            });
        
        Assert.assertEquals(1, (int)q.get());

        // Check final state
        assertCacheValidWithData(1);
    }

    public void testGetWithTwoClients() throws InterruptedException, ExecutionException {
        // Check initial state
        Assert.assertFalse(fTestCache.isValid());
        
        // Request data from cache
        Query<Integer> q1 = new TestQuery();
        q1.invoke();

        // Request data from cache again
        Query<Integer> q2 = new TestQuery(); 
        q2.invoke();
        
        // Wait until the cache starts data retrieval.
        waitForRetrieveRm();

        // Check state while waiting for data
        Assert.assertFalse(fTestCache.isValid());

        // Set the data to the callback  
        Protocol.invokeLater(new Runnable() {
            public void run() {
                fRetrieveRm.setData(1);
                fRetrieveRm.done();
                
            }
        });
        
        Assert.assertEquals(1, (int)q1.get());
        Assert.assertEquals(1, (int)q2.get());

        // Check final state
        assertCacheValidWithData(1);
    }

    public void testGetWithManyClients() throws InterruptedException, ExecutionException {
        // Check initial state
        Assert.assertFalse(fTestCache.isValid());
        
        // Request data from cache
        List<Query<Integer>> qList = new ArrayList<Query<Integer>>(); 
        for (int i = 0; i < 10; i++) {
            Query<Integer> q = new TestQuery();
            q.invoke();
            qList.add(q);
        }
        // Wait until the cache starts data retrieval.
        waitForRetrieveRm();

        // Check state while waiting for data
        Assert.assertFalse(fTestCache.isValid());

        // Set the data to the callback  
        Protocol.invokeLater(new Runnable() {
            public void run() {
                fRetrieveRm.setData(1);
                fRetrieveRm.done();
                
            }
        });
        
        for (Query<Integer> q : qList) {
            Assert.assertEquals(1, (int)q.get());            
        }
        
        // Check final state
        assertCacheValidWithData(1);
    }
    
    private static final Exception ERROR_TARGET_RUNNING = new Exception("Target is running");
    
	// DISABLE TESTS
	//
	// We say a cache is "disabled" when its most recent attempt to update from
	// the source failed. Also, a cache may make itself disabled as a reaction
	// to a state change notification from its source (e.g., the target
	// resumed). In either case, the cache is in the valid state but it has no
	// data and the status reflects an error. Keep in mind that the 'valid'
	// state is not a reflection of the quality of the data, but merely whether
	// the cache object's representation of the data is stale or
	// not. A transaction that uses a "disabled" cache object will simply fail;
	// it will not ask the cache to update its data from the source. Only a
	// change in the source's state would cause the cache to put itself back in
	// the invalid state, thus opening the door to another update.

	/**
	 * Test behavior when a cache object is asked to update itself after it has
	 * become "disabled". Since a "disabled" cache is in the valid state, a
	 * request for it to update from the source should be ignored.  However, the 
	 * client callback is not completed until next state change in cache.
	 */
    public void testDisableBeforeRequest() throws InterruptedException, ExecutionException {
        // Disable the cache
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                fTestCache.set(null, ERROR_TARGET_RUNNING, true);
            }
        });
        
        assertCacheValidWithoutData();
        
        // Try to request data from cache
        Query<Integer> q = new TestQuery();
        q.invoke();
        
        Thread.sleep(100);
        
        // Retrieval should never have been made.
        Assert.assertEquals(null, fRetrieveRm);

        // Disable the cache.  This should trigger the qery to complete.
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                fTestCache.set(null, new Throwable("Cache invalid"), false);
            }
        });

        // The cache has no data so the query should have failed  
        try {
            q.get();
            Assert.fail("expected an exeption");
        } catch (ExecutionException e) {
            // expected the exception
        }
    }

	/**
	 * Test behavior when a cache object goes into the "disabled" state while an
	 * update request is ongoing. The subsequent completion of the request should 
	 * have no effect on the  cache
	 */
    public void testDisableWhilePending() throws InterruptedException, ExecutionException {
        // Request data from cache
        Query<Integer> q = new TestQuery();
        q.invoke();

        // Disable the cache
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                fTestCache.set(null, ERROR_TARGET_RUNNING, true);
            }
        });
        
        assertCacheValidWithoutData();

		// Complete the retrieve RM. Note that the disabling of the cache above
		// disassociates it from its retrieval RM. Thus regardless of how that
		// request completes, it does not affect the cache.
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                fRetrieveRm.setData(1);
                fRetrieveRm.done();
            }
        });
        
        // Validate that cache is still disabled without data.
        assertCacheValidWithoutData();
    }

	/**
	 * Test behavior when a cache object goes into the "disabled" state while
	 * it's in the valid state. The cache remains in the valid state but it
	 * loses its data and obtains an error status.
	 */
    public void testDisableWhileValid() throws InterruptedException, ExecutionException {
        // Request data from cache
        Query<Integer> q = new TestQuery(); 
        q.invoke();
        
        // Wait until the cache starts data retrieval.
        waitForRetrieveRm();
        
        // Complete the request
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                fRetrieveRm.setData(1);
                fRetrieveRm.done();
            }
        });

        Assert.assertEquals(Integer.valueOf(1), q.get());
        
        // Disable the cache
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                fTestCache.set(null, ERROR_TARGET_RUNNING, true);
            }
        });
        
        // Check final state
        assertCacheValidWithoutData();
    }

    public void testSetWithValue() throws InterruptedException, ExecutionException {
        // Disable the cache
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                fTestCache.set(2, null, true);
            }
        });
        
        // Validate that cache is disabled without data.
        assertCacheValidWithData(2);
    }
    
    
    public void testCancelWhilePending() throws InterruptedException, ExecutionException {
        // Request data from cache 
        Query<Integer> q = new TestQuery();
        q.invoke();

        // Wait until the cache starts data retrieval.
        waitForRetrieveRm();

        // Cancel the client request
        q.cancel(true);
        try {
            q.get();
            Assert.fail("Expected a cancellation exception");
        } catch (CancellationException e) {} // Expected exception;
        
        assertCacheInvalidAndWithCanceledRM();

		// Simulate the retrieval completing successfully despite the cancel
		// request. Perhaps the retrieval logic isn't checking the RM status.
		// Even if it is checking, it may have gotten passed its last checkpoint
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                fRetrieveRm.setData(1);
                fRetrieveRm.done();
            }
        });

        // Validate that cache didn't accept the result after its RM was canceled  
        assertCacheInvalidAndWithCanceledRM();
    }

    public void testCancelWhilePending2() throws InterruptedException, ExecutionException {
        // Request data from cache 
        Query<Integer> q = new TestQuery();
        q.invoke();

        // Wait until the cache starts data retrieval.
        waitForRetrieveRm();

        // Cancel the client request
        q.cancel(true);
        try {
            q.get();
            Assert.fail("Expected a cancellation exception");
        } catch (CancellationException e) {} // Expected exception;
        
        assertCacheInvalidAndWithCanceledRM();

		// Simulate retrieval logic that is regularly checking the RM's cancel
		// status and has discovered that the request has been canceled. It
		// technically does not need to explicitly set a cancel status object in
		// the RM, thanks to RequestMonitor.getStatus() automatically returning
		// Status.CANCEL_STATUS when its in the cancel state. So here we
		// simulate the retrieval logic just aborting its operations and
		// completing the RM. Note that it hasn't provided the data to the
		// cache. 
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                fRetrieveRm.done();
            }
        });

        assertCacheInvalidAndWithCanceledRM();
    }

    public void testCancelWhilePending3() throws InterruptedException, ExecutionException {
        // Request data from cache 
        Query<Integer> q = new TestQuery();
        q.invoke();

        // Wait until the cache starts data retrieval.
        waitForRetrieveRm();

        // Cancel the client request
        q.cancel(true);
        try {
            q.get();
            Assert.fail("Expected a cancellation exception");
        } catch (CancellationException e) {} // Expected exception;
        
        assertCacheInvalidAndWithCanceledRM();

		// Simulate retrieval logic that is regularly checking the RM's cancel
		// status and has discovered that the request has been canceled. It
		// aborts its processing, sets STATUS.CANCEL_STATUS in the RM and
		// completes it. 
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
            	fRetrieveRm.setError(new CancellationException());
                fRetrieveRm.done();
            }
        });

        // Validate that cache didn't accept the result after its RM was canceled
        assertCacheInvalidAndWithCanceledRM();
    }

    public void testCancelWhilePendingWithoutClientNotification() throws InterruptedException, ExecutionException {
	    final boolean canceledCalled[] = new boolean[] { false };
	    
	    fTestCache = new TestCache() {
	        protected synchronized void canceled() {
	            canceledCalled[0] = true;
	        };
	    };
	    
        // Request data from cache 
        Query<Integer> q = new Query<Integer>() { 
            @Override
            protected void execute(final DataCallback<Integer> rm) {
                
                fTestCache.wait(new Callback(rm) {
                    @Override
                    public synchronized void addCancelListener(ICanceledListener listener) {
                        // Do not add the cancel listener so that the cancel request is not
                        // propagated to the cache.
                    }
                    
                    @Override
                    protected void handleSuccess() {
                        rm.setData(fTestCache.getData());
                        rm.done();
                    }
                });
            }
        };
        q.invoke();

        // Wait until the cache starts data retrieval.
        waitForRetrieveRm();

        // Cancel the client request
        q.cancel(true);
        
        assertCacheInvalidAndWithCanceledRM();
        
        // AbstractCache.canceled() should be called after isCanceled() 
        // discovers that the client has canceled its request.  The canceled() method is 
        // called in a separate dispatch cycle, so we have to wait one cycle of the executor 
        // after is canceled is called.
        fRetrieveRm.isCanceled();
        Protocol.invokeAndWait(new Runnable() { public void run() {} }); 
        Assert.assertTrue(canceledCalled[0]);        

        try {
            q.get();
            Assert.fail("Expected a cancellation exception");
        } catch (CancellationException e) {} // Expected exception;


        // Completed the retrieve RM
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                fRetrieveRm.setData(1);
                fRetrieveRm.done();
            }
        });

        // Validate that cache didn't accept the result after its RM was canceled
        assertCacheInvalidAndWithCanceledRM();
    }

    /**
     * This test forces a race condition where a client that requested data 
     * cancels.  While shortly after a second client starts a new request.
     * The first request's cancel should not interfere with the second 
     * request.  
     */
    public void testCancelAfterCompletedRaceCondition() throws InterruptedException, ExecutionException {

        // Create a client request with a badly behaved cancel implementation.
        final Callback[] rmBad = new Callback[1] ;
        final boolean qBadCanceled[] = new boolean[] { false };
        Query<Integer> qBad = new Query<Integer>() { 
            @Override
            protected void execute(final DataCallback<Integer> rm) {
                rmBad[0] = new Callback(rm) {
                    @Override
                    public synchronized void removeCancelListener(ICanceledListener listener) {
                        // Do not add the cancel listener so that the cancel request is not
                        // propagated to the cache.
                    }
                    
                    @Override
                    public void cancel() {
                        if (qBadCanceled[0]) {
                            super.cancel();
                        }
                    }
                    
                    @Override
                    public synchronized boolean isCanceled() {
                        return qBadCanceled[0];
                    }
                    
                    @Override
                    public synchronized void done() {
                        // Avoid clearing cancel listeners list
                    };
                    
                    @Override
					protected void handleSuccess() {
                        rm.setData(fTestCache.getData());
                        rm.done();
                    };
                };
                
                fTestCache.wait(rmBad[0]);
            }
        };
        qBad.invoke();

        // Wait until the cache starts data retrieval.
        waitForRetrieveRm();

        // Reset the cache
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                fRetrieveRm = null;
                fTestCache.set(null, null, true);
                fTestCache.reset();
            }
        });
        
        Query<Integer> qGood = new TestQuery();
        qGood.invoke();

        // Wait until the cache starts data retrieval.
        waitForRetrieveRm();
        
        qBadCanceled[0] = true;
        rmBad[0].cancel();

        Assert.assertFalse(fRetrieveRm.isCanceled());
        
        // Completed the retrieve RM
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                fRetrieveRm.setData(1);
                fRetrieveRm.done();
            }
        });

        qGood.get();
        
        assertCacheValidWithData(1);
    }
    
    public void testCancelWhilePendingWithTwoClients() throws InterruptedException, ExecutionException {
    	
		// Request data from cache. Use an additional invokeAndWait to 
        // ensure both update requests are initiated before we wait 
        // for retrieval to start
        Query<Integer> q1 = new TestQuery();
        q1.invoke();   
        Protocol.invokeAndWait(new Runnable() { public void run() {} }); 

        // Request data from cache again
        Query<Integer> q2 = new TestQuery();
        q2.invoke();
        Protocol.invokeAndWait(new Runnable() { public void run() {} });         

        // Wait until the cache starts data retrieval.
        waitForRetrieveRm();

        // Cancel the first client request
        q1.cancel(true);
        try {
            q1.get();
            Assert.fail("Expected a cancellation exception");
        } catch (CancellationException e) {} // Expected exception;
        assertCacheWaiting();

        // Cancel the second request
        q2.cancel(true);
        try {
            q2.get();
            Assert.fail("Expected a cancellation exception");
        } catch (CancellationException e) {} // Expected exception;

        assertCacheInvalidAndWithCanceledRM();

        // Completed the retrieve RM
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                fRetrieveRm.setData(1);
                fRetrieveRm.done();
            }
        });

        // Validate that cache didn't accept the result after its RM was canceled
        assertCacheInvalidAndWithCanceledRM();
    }

    public void testCancelWhilePendingWithManyClients() throws InterruptedException, ExecutionException {
        // Request data from cache 
        List<Query<Integer>> qList = new ArrayList<Query<Integer>>(); 
        for (int i = 0; i < 10; i++) {
            Query<Integer> q = new TestQuery();
            q.invoke();   
            Protocol.invokeAndWait(new Runnable() { public void run() {} }); 
            qList.add(q);
        }

        // Wait until the cache starts data retrieval.
        waitForRetrieveRm();

        // Cancel some client requests
        int[] toCancel = new int[] { 0, 2, 5, 9};
        for (int i = 0; i < toCancel.length; i++) {
            
            // Cancel request and verify that its canceled
            Query<Integer> q = qList.get(toCancel[i]);
            q.cancel(true);
            try {
                q.get();
                Assert.fail("Expected a cancellation exception");
            } catch (CancellationException e) {} // Expected exception;
            qList.set(toCancel[i], null);
            
            assertCacheWaiting();
        }

        // Replace canceled requests with new ones
        for (int i = 0; i < toCancel.length; i++) {
            Query<Integer> q = new TestQuery();
            q.invoke();   
            Protocol.invokeAndWait(new Runnable() { public void run() {} }); 
            qList.set(toCancel[i], q);
            assertCacheWaiting();
        }

        // Now cancel all requests
        for (int i = 0; i < (qList.size() - 1); i++) {
            // Validate that cache is still waiting and is not canceled
            assertCacheWaiting();
            qList.get(i).cancel(true);
        }
        qList.get(qList.size() - 1).cancel(true);
        assertCacheInvalidAndWithCanceledRM();

        // Completed the retrieve RM
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                fRetrieveRm.setData(1);
                fRetrieveRm.done();
            }
        });

        // Validate that cache didn't accept the result after its RM was canceled
        assertCacheInvalidAndWithCanceledRM();
    }

    public void testResetWhileValid() throws InterruptedException, ExecutionException {
        // Request data from cache
        Query<Integer> q = new TestQuery();
        q.invoke();

        // Wait until the cache starts data retrieval.
        waitForRetrieveRm();
        
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                fRetrieveRm.setData(1);
                fRetrieveRm.done();
            }
        });

        q.get();
        
        // Disable cache
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                fTestCache.reset();
            }
        });
        
        // Check final state
        assertCacheResetWithoutData();
    }
    
    public void testSetAndReset() throws InterruptedException, ExecutionException {
        fTestCache = new TestCache() {
            @Override
            protected void handleCompleted(Integer data, Throwable error, boolean canceled) {
                if (!canceled) {
                    // USE 'false' for valid argument.  Cache should be left in 
                    // invalid state.
                    set(data, error, false);
                }
            }
        };
        
        // Request data from cache
        Query<Integer> q = new TestQuery();
        q.invoke();

        // Wait until the cache starts data retrieval.
        waitForRetrieveRm();
        
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                fRetrieveRm.setData(1);
                fRetrieveRm.done();
            }
        });

        // Query should complete with the data from request monitor.
        try {
            q.get();
            Assert.fail("Expected InvalidCacheException");
        } catch(ExecutionException e) {}

        // No need to disable cache, it should already be disabled.
        
        // Check final state
        assertCacheResetWithoutData();
    }

}