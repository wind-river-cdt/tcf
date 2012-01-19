package org.eclipse.tcf.debug.test;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.tcf.debug.test.util.Transaction;
import org.eclipse.tcf.services.IBreakpoints;
import org.eclipse.tcf.services.ILineNumbers.CodeArea;
import org.eclipse.tcf.services.IRunControl.RunControlContext;
import org.eclipse.tcf.services.ISymbols.Symbol;
import org.junit.Assert;

public class BreakpointsTest extends AbstractTcfUITest 
{
    private String fTestId;
    private RunControlContext fTestCtx;
    private String fProcessId = "";
    private String fThreadId = "";
    private RunControlContext fThreadCtx;
    
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
                @SuppressWarnings("unchecked")
                Map<String, Object>[] bps = (Map<String, Object>[])new Map[] { fBp };
                validate( fBreakpointsCM.set(bps, this) );
                
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
    

    private void initProcessModel(String bpId, String testFunc) throws Exception {
        createBreakpoint(bpId, testFunc);
        fDebugViewListener.reset();
        
        startProcess();        
        fDebugViewListener.waitTillFinished(MODEL_CHANGED_COMPLETE | CONTENT_SEQUENCE_COMPLETE | LABEL_SEQUENCE_COMPLETE | LABEL_UPDATES);
    }
    
    public void testCreateBreakpoint() throws Exception {
        String bpId = "TestStepBP";
        initProcessModel(bpId, "tcf_test_func0");
        
        CodeArea bpCodeArea = new Transaction<CodeArea>() {
            @Override
            protected CodeArea process() throws InvalidCacheException, ExecutionException {
                String symId = validate ( fSymbolsCM.find(fProcessId, BigInteger.valueOf(0), "tcf_test_func0") );
                Symbol sym = validate ( fSymbolsCM.getContext(symId) );
                CodeArea[] area = validate ( fLineNumbersCM.mapToSource(
                    fProcessId, 
                    sym.getAddress(), 
                    new BigInteger(sym.getAddress().toString()).add(BigInteger.valueOf(1))) );
                return area[0];
            }
        }.get();
        
        
        
        CDIDebugModel.createLineBreakpoint(bpCodeArea.file, ResourcesPlugin.getWorkspace().getRoot(), bpCodeArea.start_line, true, 0, "", false);
        
        checkBreakpointForErrors(bpId, fProcessId);
    }
}
