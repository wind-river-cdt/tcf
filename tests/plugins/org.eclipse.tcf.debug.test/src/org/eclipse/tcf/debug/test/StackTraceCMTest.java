/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.eclipse.tcf.debug.test.util.ICache;
import org.eclipse.tcf.debug.test.util.RangeCache;
import org.eclipse.tcf.debug.test.util.Transaction;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.IRunControl;
import org.eclipse.tcf.services.IRunControl.RunControlContext;
import org.eclipse.tcf.services.IStackTrace.StackTraceContext;
import org.junit.Assert;

public class StackTraceCMTest extends AbstractCMTest {

    public void testStackTraceCMResetOnResumeSuspend() throws Exception {
        final TestProcessInfo processInfo = startProcess("tcf_test_func2");
        doTestStackTraceCMReset(
            processInfo,
            new Callable<Object>() { 
                public Object call() throws Exception {
                    resumeAndWaitForSuspend(processInfo.fThreadCtx, IRunControl.RM_STEP_OUT);
                    return null;
                }
            }, 
            true);

    }

    public void testStackTraceCMResetOnContextChange() throws Exception {
        final TestProcessInfo processInfo = startProcess("tcf_test_func2");
        doTestStackTraceCMReset(
            processInfo,
            new Callable<Object>() { 
                public Object call() throws Exception {
                    Protocol.invokeAndWait(new Runnable() {
                        public void run() {
                            fStackTraceCM.fRunControlListener.contextChanged(new RunControlContext[] { processInfo.fThreadCtx });
                        }
                    });
                    return null;
                }
            }, 
            false);
    }

    public void testStackTraceCMResetOnMemoryChange() throws Exception {
        final TestProcessInfo processInfo = startProcess("tcf_test_func2");
        doTestStackTraceCMReset(
            processInfo,
            new Callable<Object>() { 
                public Object call() throws Exception {
                    Protocol.invokeAndWait(new Runnable() {
                        public void run() {
                            fStackTraceCM.fMemoryListener.memoryChanged(processInfo.fProcessId, new Number[0], new long[0]);
                        }
                    });
                    return null;
                }
            }, 
            false);
    }

    public void testStackTraceCMResetOnMemoryMapChange() throws Exception {
        final TestProcessInfo processInfo = startProcess("tcf_test_func2");
        doTestStackTraceCMReset(
            processInfo,
            new Callable<Object>() { 
                public Object call() throws Exception {
                    Protocol.invokeAndWait(new Runnable() {
                        public void run() {
                            fStackTraceCM.fMemoryMapListener.changed(processInfo.fProcessId);
                        }
                    });
                    return null;
                }
            }, 
            false);
    }

    private void doTestStackTraceCMReset(final TestProcessInfo processInfo, Callable<?> resetCallable, 
        final boolean pcShouldChange) throws Exception 
    {
        // Retrieve the current PC and top frame for use later
        final String pc = new Transaction<String>() {
            @Override
            protected String process() throws InvalidCacheException, ExecutionException {
                return validate(fRunControlCM.getState(processInfo.fThreadId)).pc;
                
            }
        }.get();

        // Retrieve data from caches (make them valid).
        new Transaction<Object>() {
            @Override
            protected String process() throws InvalidCacheException, ExecutionException {
                String[] frameIds = validate( fStackTraceCM.getChildren(processInfo.fThreadId) );
                validate (fStackTraceCM.getContexts(frameIds));
                RangeCache<StackTraceContext> framesRange = fStackTraceCM.getContextRange(processInfo.fThreadId);
                List<StackTraceContext> frames = validate( framesRange.getRange(0, frameIds.length) );
                StackTraceContext topFrame = frames.get(frames.size() - 1);
                Assert.assertTrue("Expected PC to match", pc.equals(topFrame.getInstructionAddress().toString()));
                return null;
            }
        }.get();

        // Call the runnable that should reset the stack caches' state.
        resetCallable.call();

        // End test, check that all caches were reset and now return an error.
        new Transaction<Object>() {
            @Override
            protected Object process() throws InvalidCacheException, ExecutionException {
                ICache<String[]> frameIdsCache = fStackTraceCM.getChildren(processInfo.fThreadId);
                Assert.assertFalse("Expected cache to be reset", frameIdsCache.isValid());
                return null;
            }
        }.get();

        new Transaction<Object>() {
            @Override
            protected Object process() throws InvalidCacheException, ExecutionException {
                String[] frameIds = validate( fStackTraceCM.getChildren(processInfo.fThreadId) );
                ICache<StackTraceContext[]> cache = fStackTraceCM.getContexts(frameIds);
                Assert.assertFalse("Expected cache to be reset", cache.isValid());

                RangeCache<StackTraceContext> framesRange = fStackTraceCM.getContextRange(processInfo.fThreadId);
                ICache<List<StackTraceContext>> framesRangeCache = framesRange.getRange(0, frameIds.length);
                Assert.assertFalse("Expected cache to be reset", framesRangeCache.isValid());

                return null;
            }
        }.get();
        
        new Transaction<Object>() {
            @Override
            protected Object process() throws InvalidCacheException, ExecutionException {
                String[] frameIds = validate( fStackTraceCM.getChildren(processInfo.fThreadId) );
                
                RangeCache<StackTraceContext> framesRange = fStackTraceCM.getContextRange(processInfo.fThreadId);
                List<StackTraceContext> frames = validate(framesRange.getRange(frameIds.length - 1, 1));
                if (pcShouldChange) { 
                    StackTraceContext topFrame = frames.get(0);
                    Assert.assertFalse("Expected PC to be updated", pc.equals(topFrame.getInstructionAddress().toString()));
                }
                return null;
            }
        }.get();
    }

}
