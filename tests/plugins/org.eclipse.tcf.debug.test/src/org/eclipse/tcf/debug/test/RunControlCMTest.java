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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.tcf.debug.test.services.IWaitForEventCache;
import org.eclipse.tcf.debug.test.services.RunControlCM;
import org.eclipse.tcf.debug.test.services.RunControlCM.ContextState;
import org.eclipse.tcf.debug.test.util.AbstractCache;
import org.eclipse.tcf.debug.test.util.ICache;
import org.eclipse.tcf.debug.test.util.Transaction;
import org.eclipse.tcf.services.IRunControl;
import org.eclipse.tcf.services.IRunControl.RunControlContext;
import org.junit.Assert;

public class RunControlCMTest extends AbstractCMTest {

    public void testStateResetOnResumeSuspend() throws Exception {
        final TestProcessInfo processInfo = startProcess("tcf_test_func0");

        createBreakpoint("test", "tcf_test_func0");

        // Create and validate cache
        final ICache<ContextState> stateCache = new Transaction<ICache<ContextState>>() {
            public ICache<ContextState> process() throws InvalidCacheException, ExecutionException  {
                ICache<ContextState> cache = fRunControlCM.getState(processInfo.fThreadId);
                validate(cache);
                Assert.assertTrue(cache.getData().suspended == true);
                return cache;
            };
        }.get();

        // Resume thread and wait for suspend
        new Transaction<Object>() {
            @Override
            protected Object process() throws InvalidCacheException, ExecutionException {
                IWaitForEventCache<Object> waitResume = fRunControlCM.waitForContextResumed(processInfo.fThreadId, this);
                IWaitForEventCache<Object> waitSuspend = fRunControlCM.waitForContextSuspended(processInfo.fThreadId, this);
                validate(fRunControlCM.resume(processInfo.fThreadCtx, this, IRunControl.RM_RESUME, 1));
                validate(waitResume);
                if (!waitSuspend.isValid()) {
                    // The state cache should either be invalid, or it should contain updated (running) state.
                    Assert.assertTrue(stateCache.isValid() == false || stateCache.getData().suspended == false);
                    validate(stateCache);
                    Assert.assertTrue(stateCache.getData().suspended == false);                    
                }
                validate(waitSuspend);
                // The state cache should either be invalid again, or it should contain updated (suspended) state.
                Assert.assertTrue(stateCache.isValid() == false || stateCache.getData().suspended == true);
                validate(stateCache);
                Assert.assertTrue(stateCache.getData().suspended == true);
                
                return null;
            }
        }.get();
    }

    public void testStateResetOnTerminate() throws Exception {
        final TestProcessInfo processInfo = startProcess("tcf_test_func0");

        // Create and validate cache
        final ICache<ContextState> stateCache = new Transaction<ICache<ContextState>>() {
            public ICache<ContextState> process() throws InvalidCacheException, ExecutionException  {
                ICache<ContextState> cache = fRunControlCM.getState(processInfo.fThreadId);
                validate(cache);
                Assert.assertTrue(cache.getData().suspended == true);
                return cache;
            };
        }.get();

        // Terminate process and check state
        new Transaction<Object>() {
            @Override
            protected Object process() throws InvalidCacheException, ExecutionException {
                IWaitForEventCache<String[]> wait = fRunControlCM.waitForContextRemoved(processInfo.fProcessId, this);
                validate(fRunControlCM.terminate(processInfo.fTestCtx, this));
                validate(wait);
                
                // The state cache should either be invalid again, or it should contain updated (suspended) state.
                Assert.assertTrue(stateCache.isValid() == false || stateCache.getError() != null);
                if (!validateUnchecked(stateCache)) throw new InvalidCacheException();
                Assert.assertTrue(stateCache.getError() != null);
                
                return null;
            }
        }.get();
    }

    public void testRunControlCMChildrenInvalidation() throws Exception {
        final TestProcessInfo processInfo = startProcess("tcf_test_func0");

        createBreakpoint("testRunControlCMChildrenInvalidation", "tcf_test_func0");

        // Wait for each threads to start.
        final String[] threads = new Transaction<String[]>() {
            List<String> fThreads = new ArrayList<String>();
            @Override
            protected String[] process() throws InvalidCacheException, ExecutionException {
                IWaitForEventCache<RunControlContext[]> waitCache = fRunControlCM.waitForContextAdded(processInfo.fProcessId, this);
                validate(fRunControlCM.resume(processInfo.fTestCtx, this, IRunControl.RM_RESUME, 1));
                RunControlContext[] addedContexts = validate(waitCache);
                for (RunControlContext addedContext : addedContexts) {
                    fThreads.add(addedContext.getID());
                }
                if (fThreads.size() < 4) {
                    waitCache.reset();
                    validate(waitCache);
                }
                // Validate children cache
                String[] children = validate (fRunControlCM.getChildren(processInfo.fProcessId));
                Assert.assertTrue(
                    "Expected children array to contain added ids",
                    Arrays.asList(children).containsAll(fThreads));

                return fThreads.toArray(new String[fThreads.size()]);
            }
        }.get();

        // Wait for each thread to suspend, update caches
        for (final String thread : threads) {
            new Transaction<Object>() {
                @Override
                protected Object process() throws InvalidCacheException, ExecutionException {
                    RunControlCM.ContextState state = validate(fRunControlCM.getState(thread));
                    if (!state.suspended) {
                        validate( fRunControlCM.waitForContextSuspended(thread, this) );
                    }
                    String symId = validate( fSymbolsCM.find(thread, new BigInteger(state.pc), "tcf_test_func0") );
                    Number symAddr = validate( fSymbolsCM.getContext(symId) ).getAddress();
                    Assert.assertEquals("Expected thread to suspend at breakpoint address", symAddr.toString(), state.pc);
                    String[] children = validate( fRunControlCM.getChildren(thread));
                    Assert.assertEquals("Expected thread to have no children contexts", 0, children.length);
                    return null;
                }
            }.get();
        }

        // End test, check for removed events and that state caches were cleared
        new Transaction<String>() {
            @Override
            protected String process() throws InvalidCacheException, ExecutionException {
                // Create wait caches
                fRunControlCM.waitForContextRemoved(processInfo.fProcessId, this);
                IWaitForEventCache<?>[] waitCaches = new IWaitForEventCache<?>[threads.length];
                for (int i = 0; i < threads.length; i++) {
                    waitCaches[i] = fRunControlCM.waitForContextRemoved(threads[i], this);
                }
                validate( fDiagnosticsCM.cancelTest(processInfo.fTestId, this) );
                validate(waitCaches);
                validate(fRunControlCM.waitForContextRemoved(processInfo.fProcessId, this));

                try {
                    validate( fRunControlCM.getContext(processInfo.fProcessId) );
                    Assert.fail("Expected error");
                } catch (ExecutionException e) {}
                try {
                    validate( fRunControlCM.getState(processInfo.fProcessId) );
                    Assert.fail("Expected error");
                } catch (ExecutionException e) {}
                try {
                    String children[] = validate( fRunControlCM.getChildren(processInfo.fProcessId) );
                    Assert.assertEquals("Expected no children", 0, children.length);
                } catch (ExecutionException e) {}

                for (String thread : threads) {
                    try {
                        validate( fRunControlCM.getContext(thread) );
                        Assert.fail("Expected error");
                    } catch (ExecutionException e) {}
                    try {
                        validate( fRunControlCM.getState(thread) );
                        Assert.fail("Expected error");
                    } catch (ExecutionException e) {}
                }

                return null;
            }
        }.get();

        removeBreakpoint("testRunControlCMChildrenInvalidation");
    }
    
    public void testChildrenResetOnAddedRemoved() throws Exception {
        final TestProcessInfo processInfo = startProcess("tcf_test_func0");

        createBreakpoint("test", "tcf_test_func0");

        // Create and validate cache
        final ICache<String[]> childrenCache = new Transaction<ICache<String[]>>() {
            public ICache<String[]> process() throws InvalidCacheException, ExecutionException  {
                ICache<String[]> cache = fRunControlCM.getChildren(processInfo.fProcessId);
                validate(cache);
                Assert.assertTrue(cache.getData().length == 1);
                return cache;
            };
        }.get();

        // Wait for each threads to start.
        final String[] threads = new Transaction<String[]>() {
            List<String> fThreads = new ArrayList<String>();
            @Override
            protected String[] process() throws InvalidCacheException, ExecutionException {
                IWaitForEventCache<RunControlContext[]> waitCache = fRunControlCM.waitForContextAdded(processInfo.fProcessId, this);
                validate(fRunControlCM.resume(processInfo.fTestCtx, this, IRunControl.RM_RESUME, 1));
                RunControlContext[] addedContexts = validate(waitCache);
                for (RunControlContext addedContext : addedContexts) {
                    fThreads.add(addedContext.getID());
                }
                if (fThreads.size() < 4) {
                    waitCache.reset();
                    validate(waitCache);
                }
                
                // Validate children cache
                String[] children = validate (childrenCache);

                Assert.assertTrue(
                    "Expected children array to contain added ids",
                    Arrays.asList(children).containsAll(fThreads));

                return fThreads.toArray(new String[fThreads.size()]);
            }
        }.get();

        // Wait for each thread to suspend, 
        for (final String thread : threads) {
            new Transaction<Object>() {
                @Override
                protected Object process() throws InvalidCacheException, ExecutionException {
                    RunControlCM.ContextState state = validate(fRunControlCM.getState(thread));
                    if (!state.suspended) {
                        validate( fRunControlCM.waitForContextSuspended(thread, this) );
                    }
                    String[] children = validate( fRunControlCM.getChildren(thread));
                    Assert.assertEquals("Expected thread to have no children contexts", 0, children.length);
                    return null;
                }
            }.get();
        }

        // End test, check for removed events and that state caches were cleared
        new Transaction<String>() {
            @Override
            protected String process() throws InvalidCacheException, ExecutionException {
                // Create wait caches
                fRunControlCM.waitForContextRemoved(processInfo.fProcessId, this);
                IWaitForEventCache<?>[] waitCaches = new IWaitForEventCache<?>[threads.length];
                for (int i = 0; i < threads.length; i++) {
                    waitCaches[i] = fRunControlCM.waitForContextRemoved(threads[i], this);
                }
                validate( fDiagnosticsCM.cancelTest(processInfo.fTestId, this) );
                validate(waitCaches);
                validate(fRunControlCM.waitForContextRemoved(processInfo.fProcessId, this));

                try {
                    validate( fRunControlCM.getContext(processInfo.fProcessId) );
                    Assert.fail("Expected error");
                } catch (ExecutionException e) {}
                try {
                    validate( fRunControlCM.getState(processInfo.fProcessId) );
                    Assert.fail("Expected error");
                } catch (ExecutionException e) {}
                try {
                    String children[] = validate( fRunControlCM.getChildren(processInfo.fProcessId) );
                    Assert.assertEquals("Expected no children", 0, children.length);
                } catch (ExecutionException e) {}

                for (String thread : threads) {
                    try {
                        validate( fRunControlCM.getContext(thread) );
                        Assert.fail("Expected error");
                    } catch (ExecutionException e) {}
                    try {
                        validate( fRunControlCM.getState(thread) );
                        Assert.fail("Expected error");
                    } catch (ExecutionException e) {}
                }

                return null;
            }
        }.get();
    }

    public void testMappingCommandCaches() throws Exception {
        final TestProcessInfo processInfo = startProcess("tcf_test_func0");

        // Wait for each threads to start.
        new Transaction<Object>() {
            ICache<Object> fFirstCommandCache;
            
            @Override
            protected Object process() throws InvalidCacheException, ExecutionException {
                ICache<RunControlContext> contextCache = fRunControlCM.getContext(processInfo.fThreadId);
                validate(contextCache);
                
                ICache<Object> resumeCache = fRunControlCM.resume(contextCache.getData(), this, IRunControl.RM_RESUME, 1);
                if (fFirstCommandCache == null) {
                    // Reset context objet cache to force a new context object to be created and retry.
                    fFirstCommandCache = resumeCache;
                    ((AbstractCache<?>)contextCache).reset();
                    validate(contextCache);
                }
                Assert.assertTrue(resumeCache == fFirstCommandCache);
                return null;
            }
        }.get();

    }
}
