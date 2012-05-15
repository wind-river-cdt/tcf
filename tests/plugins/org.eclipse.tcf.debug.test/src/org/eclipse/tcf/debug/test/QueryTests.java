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

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipse.tcf.debug.test.util.Callback;
import org.eclipse.tcf.debug.test.util.Callback.ICanceledListener;
import org.eclipse.tcf.debug.test.util.DataCallback;
import org.eclipse.tcf.debug.test.util.Query;
import org.eclipse.tcf.protocol.Protocol;
import org.junit.Test;

/**
 * Tests that exercise the Query object.
 */
public class QueryTests  extends TestCase{
    
    public void testSimpleGet() throws InterruptedException, ExecutionException {
        Query<Integer> q = new Query<Integer>() { 
            @Override
            protected void execute(DataCallback<Integer> rm) {
                rm.setData(1);
                rm.done();
            }
        };
        // Check initial state
        Assert.assertTrue(!q.isDone());
        Assert.assertTrue(!q.isCancelled());
        
        q.invoke();
        Assert.assertEquals(1, (int)q.get());
        
        // Check final state
        Assert.assertTrue(q.isDone());
        Assert.assertTrue(!q.isCancelled());

    }

    public void testGetError() throws InterruptedException, ExecutionException {
        final String error_message = "Test Error";
        
        Query<Integer> q = new Query<Integer>() { 
            @Override
            protected void execute(DataCallback<Integer> rm) {
                rm.setError(new Throwable(error_message)); //$NON-NLS-1$
                rm.done();
            }
        };

        // Check initial state
        Assert.assertTrue(!q.isDone());
        Assert.assertTrue(!q.isCancelled());
        
        q.invoke();
        
        try {
            q.get();
            Assert.fail("Expected exception");
        } catch (ExecutionException e) {
            Assert.assertEquals(e.getCause().getMessage(), error_message);
        }
        
        // Check final state
        Assert.assertTrue(q.isDone());
        Assert.assertTrue(!q.isCancelled());

    }
     
    public void testGetWithMultipleDispatches() throws InterruptedException, ExecutionException {
        Query<Integer> q = new Query<Integer>() { 
            @Override
            protected void execute(final DataCallback<Integer> rm) {
                Protocol.invokeLater(new Runnable() { 
                    public void run() {
                        rm.setData(1);
                        rm.done();
                    }
                    @Override
                    public String toString() { return super.toString() + "\n       getWithMultipleDispatchesTest() second runnable"; } //$NON-NLS-1$
                });
            }
            @Override
            public String toString() { return super.toString() + "\n       getWithMultipleDispatchesTest() first runnable (query)"; } //$NON-NLS-1$
        };
        q.invoke();
        Assert.assertEquals(1, (int)q.get()); 
    }

    public void testExceptionOnGet() throws InterruptedException, ExecutionException {
        Query<Integer> q = new Query<Integer>() { 
            @Override
            protected void execute(final DataCallback<Integer> rm) {
                rm.setError(new Throwable("")); //$NON-NLS-1$
                rm.done();
            }
        };
        
        q.invoke();
        
        try {
            q.get();
            Assert.fail("Excpected ExecutionExeption");
        } catch (ExecutionException e) {
        } finally {
            Assert.assertTrue(q.isDone());
            Assert.assertTrue(!q.isCancelled());
        }            
    }

    public void testCancelBeforeWaiting() throws InterruptedException, ExecutionException {
        final Query<Integer> q = new Query<Integer>() { 
            @Override protected void execute(final DataCallback<Integer> rm) {
                Assert.fail("Query was cancelled, it should not be called."); //$NON-NLS-1$
                rm.done();
            }
        };
        
        // Cancel before invoking the query.
        q.cancel(false);

        Assert.assertTrue(q.isDone());
        Assert.assertTrue(q.isCancelled());            

        // Start the query.
        q.invoke();
        
        
        
        // Block to retrieve data
        try {
            q.get();
        } catch (CancellationException e) {
            return; // Success
        } finally {
            Assert.assertTrue(q.isDone());
            Assert.assertTrue(q.isCancelled());            
        }            
        Assert.assertTrue("CancellationException should have been thrown", false); //$NON-NLS-1$
    }

    public void testCancelWhileWaiting() throws InterruptedException, ExecutionException {
        final DataCallback<?>[] rmHolder = new DataCallback<?>[1];   
        final Boolean[] cancelCalled = new Boolean[] { Boolean.FALSE };
        
        final Query<Integer> q = new Query<Integer>() { 
            @Override protected void execute(final DataCallback<Integer> rm) {
                synchronized (rmHolder) {
                    rmHolder[0] = rm;
                    rmHolder.notifyAll();
                }
            }
        };
        
        // Start the query.
        q.invoke();

        // Wait until the query is started
        synchronized (rmHolder) {
            while(rmHolder[0] == null) {
                rmHolder.wait();
            }
        }        
        
        // Add a cancel listener to the query RM
        rmHolder[0].addCancelListener(new ICanceledListener() {
            
            public void requestCanceled(Callback rm) {
                cancelCalled[0] = Boolean.TRUE;
            }
        });
        
        // Cancel running request.
        q.cancel(false);
        
        Assert.assertTrue(cancelCalled[0]);
        Assert.assertTrue(rmHolder[0].isCanceled());
        Assert.assertTrue(q.isCancelled());
        Assert.assertTrue(q.isDone());
        
        // Retrieve data
        try {
            q.get();
        } catch (CancellationException e) {
            return; // Success
        } finally {
            Assert.assertTrue(q.isDone());
            Assert.assertTrue(q.isCancelled());            
        }            
        
        // Complete rm and query.
        @SuppressWarnings("unchecked")
        DataCallback<Integer> drm = (DataCallback<Integer>)rmHolder[0]; 
        drm.setData(new Integer(1));
        rmHolder[0].done();
        
        // Try to retrieve data again, it should still result in 
        // cancellation exception.
        try {
            q.get();
        } catch (CancellationException e) {
            return; // Success
        } finally {
            Assert.assertTrue(q.isDone());
            Assert.assertTrue(q.isCancelled());            
        }            

        
        Assert.assertTrue("CancellationException should have been thrown", false); //$NON-NLS-1$
    }

    
    public void testGetTimeout() throws InterruptedException, ExecutionException {
        final Query<Integer> q = new Query<Integer>() { 
            @Override
            protected void execute(final DataCallback<Integer> rm) {
                // Call done with a delay of 1 second, to avoid stalling the tests.
                Protocol.invokeLater(
                    60000,
                    new Runnable() {
                        public void run() { rm.done(); }
                    });
            }
        };

        q.invoke();

        // Note: no point in checking isDone() and isCancelled() here, because
        // the value could change on timing.
        
        try {
            q.get(1, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            return; // Success
        } finally {
            Assert.assertFalse("Query should not be done yet, it should have timed out first.", q.isDone()); //$NON-NLS-1$
        }            
        Assert.assertTrue("TimeoutException should have been thrown", false); //$NON-NLS-1$
    }

}
