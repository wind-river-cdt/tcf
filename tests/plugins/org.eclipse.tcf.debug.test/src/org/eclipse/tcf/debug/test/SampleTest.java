package org.eclipse.tcf.debug.test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualItem;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.tcf.debug.test.services.IWaitForEventCache;
import org.eclipse.tcf.debug.test.services.RunControlCM;
import org.eclipse.tcf.debug.test.services.RunControlCM.ContextState;
import org.eclipse.tcf.debug.test.util.Transaction;
import org.eclipse.tcf.debug.ui.ITCFObject;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.services.IBreakpoints;
import org.eclipse.tcf.services.IDiagnostics.ISymbol;
import org.eclipse.tcf.services.IRunControl;
import org.eclipse.tcf.services.IRunControl.RunControlContext;
import org.eclipse.tcf.services.ISymbols;
import org.eclipse.tcf.services.ISymbols.Symbol;
import org.eclipse.tcf.services.ILineNumbers.CodeArea;
import org.junit.Assert;

@SuppressWarnings("restriction")
public class SampleTest extends AbstractTcfUITest 
{
    private String fTestId;
    private RunControlContext fTestCtx;
    private String fProcessId = "";
    private String fThreadId = "";
    private RunControlContext fThreadCtx;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearBreakpoints();
    }
    
    private void clearBreakpoints() throws InterruptedException, ExecutionException {
        new Transaction<Object>() {
            @Override
            protected Object process() throws InvalidCacheException, ExecutionException {
                // Initialize the event cache for breakpoint status
                @SuppressWarnings("unchecked")
                Map<String, Object>[] bps = (Map<String, Object>[])new Map[] { };
                validate( fBreakpointsCM.set(bps, this) );
                return null;
            }
        }.get();        
    }
    
    private void createBreakpoint(final String bpId, final String testFunc) throws InterruptedException, ExecutionException {
        new Transaction<Object>() {
            private Map<String,Object> fBp;
            
            {
                fBp = new TreeMap<String,Object>();
                fBp.put(IBreakpoints.PROP_ID, bpId);
                fBp.put(IBreakpoints.PROP_ENABLED, Boolean.TRUE);
                fBp.put(IBreakpoints.PROP_LOCATION, testFunc);
            }
            
            @Override
            protected Object process() throws InvalidCacheException, ExecutionException {
                
                // Initialize the event cache for breakpoint status
//                ICache<Map<String, Object>> waitStatusCache = fBreakpointsCM.waitStatusChanged(bpId, fTestRunKey);
                
                validate( fBreakpointsCM.add(fBp, this) );
                
//                // Wait for breakpoint status event and validate it.
//                Map<String, Object> status = validate(waitStatusCache);
//                String s = (String)status.get(IBreakpoints.STATUS_ERROR);
//                if (s != null) {
//                    Assert.fail("Invalid BP status: " + s);
//                }
//                @SuppressWarnings("unchecked")
//                Collection<Map<String,Object>> list = (Collection<Map<String,Object>>)status.get(IBreakpoints.STATUS_INSTANCES);
//                if (list != null) {
//                    String err = null;
//                    for (Map<String,Object> map : list) {
//                        String ctx = (String)map.get(IBreakpoints.INSTANCE_CONTEXT);
//                        if (processId.equals(ctx) && map.get(IBreakpoints.INSTANCE_ERROR) != null)
//                            err = (String)map.get(IBreakpoints.INSTANCE_ERROR);
//                    }
//                    if (err != null) {
//                        Assert.fail("Invalid BP status: " + s);
//                    }
//                }                
                return null;
            }
        }.get();
    }

    private void checkBreakpointForErrors(final String bpId, final String processId) throws InterruptedException, ExecutionException {
        new Transaction<Object>() {          
            @Override
            protected Object process() throws InvalidCacheException, ExecutionException {
                // Wait for breakpoint status event and validate it.
                Map<String, Object> status = validate( fBreakpointsCM.getStatus(bpId) );
                String s = (String)status.get(IBreakpoints.STATUS_ERROR);
                if (s != null) {
                    Assert.fail("Invalid BP status: " + s);
                }
                @SuppressWarnings("unchecked")
                Collection<Map<String,Object>> list = (Collection<Map<String,Object>>)status.get(IBreakpoints.STATUS_INSTANCES);
                if (list != null) {
                    String err = null;
                    for (Map<String,Object> map : list) {
                        String ctx = (String)map.get(IBreakpoints.INSTANCE_CONTEXT);
                        if (processId.equals(ctx) && map.get(IBreakpoints.INSTANCE_ERROR) != null)
                            err = (String)map.get(IBreakpoints.INSTANCE_ERROR);
                    }
                    if (err != null) {
                        Assert.fail("Invalid BP status: " + s);
                    }
                }                
                return null;
            }
        }.get();        
    }
    
    private void startProcess() throws InterruptedException, ExecutionException {
        new Transaction<Object>() {
            protected Object process() throws Transaction.InvalidCacheException ,ExecutionException {
                fTestId = validate( fDiagnosticsCM.runTest(getDiagnosticsTestName(), this) );
                fTestCtx = validate( fRunControlCM.getContext(fTestId) );
                fProcessId = fTestCtx.getProcessID();
                // Create the cache to listen for exceptions.
                fRunControlCM.waitForContextException(fTestId, fTestRunKey); 
                
                if (!fProcessId.equals(fTestId)) {
                    fThreadId = fTestId;
                } else {
                    String[] threads = validate( fRunControlCM.getChildren(fProcessId) );
                    fThreadId = threads[0];
                }
                fThreadCtx = validate( fRunControlCM.getContext(fThreadId) );
                
                Assert.assertTrue("Invalid thread context", fThreadCtx.hasState());
                return new Object();
            };
        }.get();
    }
    
    private boolean runToTestEntry(final String testFunc) throws InterruptedException, ExecutionException {
        return new Transaction<Boolean>() {
            Object fWaitForSuspendKey = new Object();
            boolean fSuspendEventReceived = false;
            protected Boolean process() throws Transaction.InvalidCacheException ,ExecutionException {
                ISymbol sym_func0 = validate( fDiagnosticsCM.getSymbol(fProcessId, testFunc) );
                String sym_func0_value = sym_func0.getValue().toString();
                ContextState state = validate (fRunControlCM.getState(fThreadId));
                if (state.suspended) {
                    if ( !new BigInteger(state.pc).equals(new BigInteger(sym_func0_value)) ) {
                        fSuspendEventReceived = true;
                        // We are not at test entry.  Create a new suspend wait cache.
                        fWaitForSuspendKey = new Object();
                        fRunControlCM.waitForContextSuspended(fThreadId, fWaitForSuspendKey);
                        // Run to entry point.
                        validate( fRunControlCM.resume(fThreadCtx, fWaitForSuspendKey, IRunControl.RM_RESUME, 1) );
                    }
                } else {
                    // Wait until we suspend.
                    validate( fRunControlCM.waitForContextSuspended(fThreadId, fWaitForSuspendKey) );
                }
                
                return fSuspendEventReceived;
            }
        }.get();
    }

    private void initProcessModel(String bpId, String testFunc) throws Exception {
        createBreakpoint(bpId, testFunc);
        fDebugViewListener.reset();
        
        ITCFObject processTCFContext = new ITCFObject() {  
            public String getID() { return fProcessId; }
            public IChannel getChannel() { return channels[0]; }
        };
        ITCFObject threadTCFContext = new ITCFObject() {  
            public String getID() { return fThreadId; }
            public IChannel getChannel() { return channels[0]; }
        };
        
        fDebugViewListener.addLabelUpdate(new TreePath(new Object[] { fLaunch, processTCFContext }));
        fDebugViewListener.addLabelUpdate(new TreePath(new Object[] { fLaunch, processTCFContext, threadTCFContext }));

        startProcess();        
        runToTestEntry(testFunc);
        
        final String topFrameId = new Transaction<String>() {
            @Override
            protected String process() throws InvalidCacheException, ExecutionException {
                String[] frameIds = validate( fStackTraceCM.getChildren(fThreadId) );
                Assert.assertTrue("No stack frames" , frameIds.length != 0);
                return frameIds[frameIds.length - 1];
            }
        }.get();

        ITCFObject frameTCFContext = new ITCFObject() {  
            public String getID() { return topFrameId; }
            public IChannel getChannel() { return channels[0]; }
        };
        fDebugViewListener.addLabelUpdate(new TreePath(new Object[] { fLaunch, processTCFContext, threadTCFContext, frameTCFContext }));
        
        fDebugViewListener.waitTillFinished(MODEL_CHANGED_COMPLETE | CONTENT_SEQUENCE_COMPLETE | LABEL_SEQUENCE_COMPLETE | LABEL_UPDATES);
    }
    
    public void testDebugViewContent() throws Exception {
        String bpId = "TestStepBP";
        initProcessModel(bpId, "tcf_test_func0");

        VirtualItem launchItem = fDebugViewListener.findElement(new Pattern[] { Pattern.compile(".*" + fLaunch.getLaunchConfiguration().getName() + ".*") }  );
        Assert.assertTrue(launchItem != null);
        
        VirtualItem processItem = fDebugViewListener.findElement(launchItem, new Pattern[] { Pattern.compile(".*agent.*") }  );
        Assert.assertTrue(processItem != null);

        VirtualItem threadItem = fDebugViewListener.findElement(processItem, new Pattern[] { Pattern.compile(".*" + fThreadId + ".*") }  );
        Assert.assertTrue(threadItem != null);

        VirtualItem frameItem = fDebugViewListener.findElement(threadItem, new Pattern[] { Pattern.compile(".*tcf_test_func0.*")});
        Assert.assertTrue(frameItem != null);
        
        checkBreakpointForErrors(bpId, fProcessId);
    }

    public void testSteppingDebugViewOnly() throws Exception {
        String bpId = "TestStepBP";
        initProcessModel(bpId, "tcf_test_func0");
        
        // Execute step loop
        String previousThreadLabel = null;
        for (int stepNum = 0; stepNum < 100; stepNum++) {
            fDebugViewListener.reset();
            
            resumeAndWaitForSuspend(fThreadCtx, IRunControl.RM_STEP_INTO_LINE);

            fDebugViewListener.waitTillFinished(MODEL_CHANGED_COMPLETE | CONTENT_SEQUENCE_COMPLETE | LABEL_UPDATES_RUNNING);
            VirtualItem topFrameItem = fDebugViewListener.findElement(
                new Pattern[] { Pattern.compile(".*"), Pattern.compile(".*"), Pattern.compile(".*" + fProcessId + ".*\\(Step.*"), Pattern.compile(".*")});
            Assert.assertTrue(topFrameItem != null);
            String topFrameLabel = ((String[])topFrameItem.getData(VirtualItem.LABEL_KEY))[0];
            Assert.assertTrue(!topFrameLabel.equals(previousThreadLabel));
            previousThreadLabel = topFrameLabel;
        }
        
        checkBreakpointForErrors(bpId, fProcessId);
    }
    
    public void testSteppingWithVariablesAndRegisters() throws Exception {
        fVariablesViewViewer.setActive(true);
        fRegistersViewViewer.setActive(true);

        initProcessModel("TestStepBP", "tcf_test_func0");
        
        // Execute step loop
        String previousThreadLabel = null;
        for (int stepNum = 0; stepNum < 100; stepNum++) {
            fDebugViewListener.reset();
            fVariablesViewListener.reset();
            fRegistersViewListener.reset();
            
            resumeAndWaitForSuspend(fThreadCtx, IRunControl.RM_STEP_INTO_LINE);

            fDebugViewListener.waitTillFinished(MODEL_CHANGED_COMPLETE | CONTENT_SEQUENCE_COMPLETE | LABEL_UPDATES_RUNNING);
            fVariablesViewListener.waitTillFinished(CONTENT_SEQUENCE_COMPLETE | LABEL_UPDATES_RUNNING);
            fRegistersViewListener.waitTillFinished(CONTENT_SEQUENCE_COMPLETE | LABEL_UPDATES_RUNNING);
            VirtualItem topFrameItem = fDebugViewListener.findElement(
                new Pattern[] { Pattern.compile(".*"), Pattern.compile(".*"), Pattern.compile(".*" + fProcessId + ".*\\(Step.*"), Pattern.compile(".*")});
            Assert.assertTrue(topFrameItem != null);
            String topFrameLabel = ((String[])topFrameItem.getData(VirtualItem.LABEL_KEY))[0];
            Assert.assertTrue(!topFrameLabel.equals(previousThreadLabel));
            previousThreadLabel = topFrameLabel;
        }
        
        checkBreakpointForErrors("TestStepBP", fProcessId);
    }
    
    public void testSymbolsCMResetOnContextRemove() throws Exception {
        createBreakpoint("TestStepBP", "tcf_test_func0");
        startProcess();        
        runToTestEntry("tcf_test_func0");
        
        // Retrieve the current PC for use later
        final String pc = new Transaction<String>() {
            @Override
            protected String process() throws InvalidCacheException, ExecutionException {
                return validate(fRunControlCM.getState(fThreadId)).pc;
            }
        }.get();
        
        // Find symbol by name and valide the cache.
        final String symbolId = new Transaction<String>() {
            @Override
            protected String process() throws InvalidCacheException, ExecutionException {
                String symId = validate( fSymbolsCM.find(fProcessId, new BigInteger(pc), "tcf_test_func0") );
                Symbol sym = validate( fSymbolsCM.getContext(symId) );
                Assert.assertEquals(ISymbols.UPDATE_ON_MEMORY_MAP_CHANGES, sym.getUpdatePolicy());
                return symId;
            }
        }.get();

        // Find symbol by address and validate its context.  Save address for later.
        final Number symAddr = new Transaction<Number>() {
            @Override
            protected Number process() throws InvalidCacheException, ExecutionException {
                Symbol sym = validate( fSymbolsCM.getContext(symbolId) );
                String symId2 = validate( fSymbolsCM.findByAddr(fProcessId, sym.getAddress()) );
                Symbol sym2 = validate( fSymbolsCM.getContext(symId2) );
                Assert.assertEquals(sym.getAddress(), sym2.getAddress());
                return sym.getAddress();
            }
        }.get();

        // End test, check that all caches were reset and now return an error.
        new Transaction<String>() {
            @Override
            protected String process() throws InvalidCacheException, ExecutionException {
                validate( fDiagnosticsCM.cancelTest(fTestId, this) );
                validate( fRunControlCM.waitForContextRemoved(fProcessId, this) );
                try {
                    validate( fSymbolsCM.getContext(symbolId) );
                    Assert.fail("Expected error");
                } catch (ExecutionException e) {}
                try {
                    validate( fSymbolsCM.find(fProcessId, new BigInteger(pc), "tcf_test_func0") );
                    Assert.fail("Expected error");
                } catch (ExecutionException e) {}
                try {
                    validate( fSymbolsCM.findByAddr(fProcessId, symAddr) );
                    Assert.fail("Expected error");
                } catch (ExecutionException e) {}

                return null;
            }
        }.get();
    }

    public void testLineNumbersCMResetOnContextRemove() throws Exception {
        createBreakpoint("TestStepBP", "tcf_test_func0");
        startProcess();        
        runToTestEntry("tcf_test_func0");
        
        // Retrieve the current PC for use later
        final String pc = new Transaction<String>() {
            @Override
            protected String process() throws InvalidCacheException, ExecutionException {
                return validate(fRunControlCM.getState(fThreadId)).pc;
            }
        }.get();
        
        final BigInteger pcNumber = new BigInteger(pc);
        final BigInteger pcNumberPlusOne = pcNumber.add(BigInteger.valueOf(1));

        // Retrieve the line number for current PC.
        final CodeArea[] pcCodeAreas = new Transaction<CodeArea[]>() {
            @Override
            protected CodeArea[] process() throws InvalidCacheException, ExecutionException {
                CodeArea[] areas = validate(fLineNumbersCM.mapToSource(fProcessId, pcNumber, pcNumberPlusOne));
                Assert.assertNotNull(areas);
                Assert.assertTrue(areas.length != 0);
                
                areas = validate(fLineNumbersCM.mapToSource(fThreadId, pcNumber, pcNumberPlusOne));
                Assert.assertNotNull(areas);
                Assert.assertTrue(areas.length != 0);
                
                CodeArea[] areas2 = validate(fLineNumbersCM.mapToMemory(fProcessId, areas[0].file, areas[0].start_line, areas[0].start_column));
                Assert.assertNotNull(areas2);
                Assert.assertTrue(areas2.length != 0);
                
                areas2 = validate(fLineNumbersCM.mapToMemory(fThreadId, areas[0].file, areas[0].start_line, areas[0].start_column));
                Assert.assertNotNull(areas2);
                Assert.assertTrue(areas2.length != 0);
                
                return areas;
            }
        }.get();

        // End test, check that all caches were reset and now return an error.
        new Transaction<String>() {
            @Override
            protected String process() throws InvalidCacheException, ExecutionException {
                validate( fDiagnosticsCM.cancelTest(fTestId, this) );
                validate( fRunControlCM.waitForContextRemoved(fProcessId, this) );
                try {
                    validate(fLineNumbersCM.mapToSource(fProcessId, pcNumber, pcNumberPlusOne));
                    Assert.fail("Expected error");
                } catch (ExecutionException e) {}
                try {
                    validate(fLineNumbersCM.mapToSource(fThreadId, pcNumber, pcNumberPlusOne));
                    Assert.fail("Expected error");
                } catch (ExecutionException e) {}
                try {
                    CodeArea[] areas3 = validate(fLineNumbersCM.mapToMemory(fProcessId, pcCodeAreas[0].file, pcCodeAreas[0].start_line, pcCodeAreas[0].start_column));
                    Assert.fail("Expected error");
                } catch (ExecutionException e) {}
                try {
                    validate(fLineNumbersCM.mapToMemory(fThreadId, pcCodeAreas[0].file, pcCodeAreas[0].start_line, pcCodeAreas[0].start_column));
                    Assert.fail("Expected error");
                } catch (ExecutionException e) {}

                return null;
            }
        }.get();
    }
    
    
    public void testSymbolsCMResetOnContextStateChange() throws Exception {
        createBreakpoint("TestStepBP", "tcf_test_func2");
        startProcess();        
        runToTestEntry("tcf_test_func2");
        
        // Retrieve the current PC and top frame for use later
        final String pc = new Transaction<String>() {
            @Override
            protected String process() throws InvalidCacheException, ExecutionException {
                return validate(fRunControlCM.getState(fThreadId)).pc;
            }
        }.get();
        final String topFrameId = new Transaction<String>() {
            @Override
            protected String process() throws InvalidCacheException, ExecutionException {
                String[] frameIds = validate( fStackTraceCM.getChildren(fThreadId) );
                return frameIds[frameIds.length - 1];
            }
        }.get();
        
        // Find symbol by name and valide the cache.
        final String symbolId = new Transaction<String>() {
            @Override
            protected String process() throws InvalidCacheException, ExecutionException {
                String symId = validate( fSymbolsCM.find(topFrameId, new BigInteger(pc), "func2_local1") );
                Symbol sym = validate( fSymbolsCM.getContext(symId) );
                Assert.assertEquals(ISymbols.UPDATE_ON_EXE_STATE_CHANGES, sym.getUpdatePolicy());
                return symId;
            }
        }.get();

        // Note: findByAddr doesn't seem to work on a local var.
//        // Find symbol by address and validate its context.  Save address for later.
//        final Number symAddr = new Transaction<Number>() {
//            @Override
//            protected Number process() throws InvalidCacheException, ExecutionException {
//                Symbol sym = validate( fSymbolsCM.getContext(symbolId) );
//                String symId2 = validate( fSymbolsCM.findByAddr(topFrameId, sym.getAddress()) );
//                Symbol sym2 = validate( fSymbolsCM.getContext(symId2) );
//                Assert.assertEquals(sym.getAddress(), sym2.getAddress());
//                return sym.getAddress();
//            }
//        }.get();
        
        // Execute a step.
        resumeAndWaitForSuspend(fThreadCtx, IRunControl.RM_STEP_OUT);

        // End test, check that all caches were reset and now return an error.
        new Transaction<Object>() {
            @Override
            protected Object process() throws InvalidCacheException, ExecutionException {
                Assert.assertFalse(
                    "Expected cache to be reset", 
                    fSymbolsCM.getContext(symbolId).isValid());
                Assert.assertFalse(
                    "Expected cache to be reset", 
                    fSymbolsCM.find(topFrameId, new BigInteger(pc), "func2_local1").isValid() );
                return null;
            }
        }.get();
    }
    
    public void testRunControlCMChildrenInvalidation() throws Exception {
        createBreakpoint("BP1", "tcf_test_func0");
        startProcess();
        runToTestEntry("tcf_test_func0");

        // Wait for each threads to start.
        final String[] threads = new Transaction<String[]>() {
            List<String> fThreads = new ArrayList<String>();
            @Override
            protected String[] process() throws InvalidCacheException, ExecutionException {
                IWaitForEventCache<RunControlContext[]> waitCache = fRunControlCM.waitForContextAdded(fProcessId, this);
                validate(fRunControlCM.resume(fTestCtx, this, IRunControl.RM_RESUME, 1));
                RunControlContext[] addedContexts = validate(waitCache);
                for (RunControlContext addedContext : addedContexts) {
                    fThreads.add(addedContext.getID());
                }
                if (fThreads.size() < 4) {
                    waitCache.reset();
                    validate(waitCache);
                }
                // Validate children cache
                String[] children = validate (fRunControlCM.getChildren(fProcessId));
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

        // End test, check for remvoed events and that state caches were cleared
        new Transaction<String>() {
            @Override
            protected String process() throws InvalidCacheException, ExecutionException {
                validate( fDiagnosticsCM.cancelTest(fTestId, this) );
                // Create wait caches
                IWaitForEventCache<?>[] waitCaches = new IWaitForEventCache<?>[threads.length];
                for (int i = 0; i < threads.length; i++) {
                    waitCaches[i] = fRunControlCM.waitForContextRemoved(threads[i], this); 
                }
                validate(fRunControlCM.waitForContextRemoved(fProcessId, this));

                try {
                    validate( fRunControlCM.getContext(fProcessId) );
                    Assert.fail("Expected error");
                } catch (ExecutionException e) {}
                try {
                    validate( fRunControlCM.getState(fProcessId) );
                    Assert.fail("Expected error");
                } catch (ExecutionException e) {}
                try {
                    String children[] = validate( fRunControlCM.getChildren(fProcessId) );
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
                    try {
                        String children[] = validate( fRunControlCM.getChildren(fProcessId) );
                        Assert.assertEquals("Expected no children", 0, children.length);
                    } catch (ExecutionException e) {}
                }

                return null;
            }
        }.get();
    }
    
}
