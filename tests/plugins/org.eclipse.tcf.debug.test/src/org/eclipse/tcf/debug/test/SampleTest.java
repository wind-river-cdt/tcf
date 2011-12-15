package org.eclipse.tcf.debug.test;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualItem;
import org.eclipse.tcf.debug.test.util.DataCallback;
import org.eclipse.tcf.debug.test.util.Query;
import org.eclipse.tcf.debug.test.util.Task;
import org.eclipse.tcf.services.IDiagnostics.ISymbol;
import org.eclipse.tcf.services.IRunControl;
import org.eclipse.tcf.services.IRunControl.RunControlContext;
import org.junit.Assert;

@SuppressWarnings("restriction")
public class SampleTest extends AbstractTcfUITest 
{

    private TestBreakpointsListener fBpListener;
    private String fTestId;
    private RunControlContext fTestCtx;
    private String fProcessId;
    private String fThreadId;
    private RunControlContext fThreadCtx;
    
    @Override
    protected void setUpServiceListeners() throws Exception {
        super.setUpServiceListeners();
        fBpListener = new TestBreakpointsListener(bp);
    }

    @Override
    protected void tearDownServiceListeners() throws Exception {
        fBpListener.dispose();
        super.tearDownServiceListeners();
    }
    
    private void createBreakpoint(String bpId, String testFunc) throws InterruptedException, ExecutionException {
        fBpListener.setBreakpointId(bpId);
        setBreakpoint(bpId, testFunc);        
    }

    private void startProcess() throws InterruptedException, ExecutionException {
        fTestId = startDiagnosticsTest();
        fTestCtx =  getRunControlContext(fTestId);
        fProcessId = getProcessIdFromRunControlContext(fTestCtx);
        fBpListener.setProcess(fProcessId);
        fThreadId = null; 
        if (!fProcessId.equals(fTestId)) {
            fThreadId = fTestId;
        } else {
            fThreadId = getSingleThreadIdFromProcess(fProcessId);
        }
        fThreadCtx = getRunControlContext(fThreadId);
        Assert.assertTrue("Invalid thread context", getRunControlContextHasState(fThreadCtx));
    }
    
    protected String getProcessNameFromRunControlContext(final RunControlContext rcContext) throws InterruptedException, ExecutionException {
        return new Task<String>() {
            @Override
            public String call() throws Exception {
                return rcContext.getName();
            }
        }.get();
    }
    
    private void runToTestEntry(String testFunc) throws InterruptedException, ExecutionException {
        final String suspended_pc = new Query<String> () {
            @Override
            protected void execute(DataCallback<String> callback) {
                fRcListener.waitForSuspend(fThreadCtx, callback);
            }
        }.get();
        ISymbol sym_func0 = getDiagnosticsSymbol(fProcessId, testFunc);
        String sym_func0_value = getSymbolValue(sym_func0).toString();
        if (!new BigInteger(sym_func0_value).equals(new BigInteger(suspended_pc))) {
            resumeAndWaitForSuspend(fThreadCtx, IRunControl.RM_RESUME);
        }        
    }

    private void initProcessModel(String bpId, String testFunc) throws Exception {
        createBreakpoint(bpId, testFunc);
        
        fDebugViewListener.reset();
        fDebugViewListener.setDelayContentUntilProxyInstall(true);
        startProcess();
        fDebugViewListener.waitTillFinished(MODEL_CHANGED_COMPLETE | MODEL_PROXIES_INSTALLED | CONTENT_SEQUENCE_COMPLETE | LABEL_UPDATES_RUNNING);
        runToTestEntry(testFunc);
    }
    
    public void testDebugViewContent() throws Exception {
        initProcessModel("TestStepBP", "tcf_test_func0");
        
        VirtualItem launchItem = fDebugViewListener.findElement(new Pattern[] { Pattern.compile(".*" + fLaunch.getLaunchConfiguration().getName() + ".*") }  );
        Assert.assertTrue(launchItem != null);
        
        VirtualItem processItem = fDebugViewListener.findElement(launchItem, new Pattern[] { Pattern.compile(".*") }  );
        Assert.assertTrue(processItem != null);

        VirtualItem threadItem = fDebugViewListener.findElement(processItem, new Pattern[] { Pattern.compile(".*" + fThreadId + ".*") }  );
        Assert.assertTrue(threadItem != null);

        VirtualItem frameItem = fDebugViewListener.findElement(threadItem, new Pattern[] { Pattern.compile(".*")});
        Assert.assertTrue(frameItem != null);
        
        fBpListener.checkError();
    }

    public void testSteppingDebugViewOnly() throws Exception {
        initProcessModel("TestStepBP", "tcf_test_func0");
        
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
        
        fBpListener.checkError();
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
        
        fBpListener.checkError();
    }
    
    
}
