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
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import junit.framework.AssertionFailedError;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.internal.ui.breakpoints.provisional.IBreakpointOrganizer;
import org.eclipse.debug.internal.ui.breakpoints.provisional.IBreakpointUIConstants;
import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.tcf.debug.test.services.RunControlCM.ContextState;
import org.eclipse.tcf.debug.test.util.Transaction;
import org.eclipse.tcf.internal.cdt.ui.breakpoints.BreakpointScopeOrganizer;
import org.eclipse.tcf.internal.debug.model.TCFBreakpointsModel;
import org.eclipse.tcf.internal.debug.ui.launch.TCFLaunchContext;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.ILineNumbers.CodeArea;
import org.eclipse.tcf.services.ISymbols.Symbol;
import org.junit.Assert;

@SuppressWarnings("restriction")
public class BreakpointsViewTest extends AbstractTcfUITest
{
    private BreakpointsListener fBpListener;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fBpListener = new BreakpointsListener();

        // CDT Breakpoint integration depends on the TCF-CDT breakpoint
        // integration to be active.  This is normally triggered by selecting
        // a stack frame in the UI.  Here force activation of the plugin
        // artificially.  None of the cdt integration packages are exported, so
        // use the TCF Launch Context extension point indirectly to force the
        // plugin to load.
        TCFLaunchContext.getLaunchContext(null);
    }

    @Override
    protected void tearDown() throws Exception {
        fBpListener.dispose();
        super.tearDown();
    }

    private CodeArea getFunctionCodeArea(final TestProcessInfo processInfo, String functionName) throws Exception {
        return new Transaction<CodeArea>() {
            @Override
            protected CodeArea process() throws InvalidCacheException, ExecutionException {
            	ContextState state = validate ( fRunControlCM.getState(processInfo.fThreadId) );
                String symId = validate ( fSymbolsCM.find(processInfo.fProcessId, new BigInteger(state.pc), "tcf_test_func0") );
                Symbol sym = validate ( fSymbolsCM.getContext(symId) );
                CodeArea[] area = validate ( fLineNumbersCM.mapToSource(
                    processInfo.fProcessId,
                		sym.getAddress(),
                		new BigInteger(sym.getAddress().toString()).add(BigInteger.valueOf(1))) );
                return area[0];
            }
        }.get();
    }

    private ICLineBreakpoint createLineBreakpoint(String file, int line, String query, String contexts) throws CoreException, ExecutionException, InterruptedException {
        // Initiate wait for the context changed event.
        final Object contextChangedWaitKey = new Object();
        Protocol.invokeAndWait(new Runnable() { public void run() {
            fBreakpointsCM.waitContextAdded(contextChangedWaitKey);
        }});

        final ICLineBreakpoint bp = CDIDebugModel.createBlankLineBreakpoint();
        Map<String, Object> attrs = new TreeMap<String, Object>();
        CDIDebugModel.setLineBreakpointAttributes(attrs, file, ICBreakpointType.REGULAR, line, true, 0, "");
        attrs.put(TCFBreakpointsModel.ATTR_CONTEXT_QUERY, query);
        attrs.put(TCFBreakpointsModel.ATTR_CONTEXTIDS, contexts);
        CDIDebugModel.createBreakpointMarker(bp, ResourcesPlugin.getWorkspace().getRoot(), attrs, true);

        return bp;
    }

    public void testScopeGrouping() throws Exception {
        TestProcessInfo processInfo = initProcessModel("tcf_test_func0");

        CodeArea func0CodeArea = getFunctionCodeArea(processInfo, "tcf_test_func0");
        createLineBreakpoint(func0CodeArea.file, func0CodeArea.start_line, null, null);

        CodeArea func1CodeArea = getFunctionCodeArea(processInfo, "tcf_test_func1");
        createLineBreakpoint(func1CodeArea.file, func1CodeArea.start_line, "test_query", null);

        CodeArea func2CodeArea = getFunctionCodeArea(processInfo, "tcf_test_func2");
        createLineBreakpoint(func2CodeArea.file, func2CodeArea.start_line, null, "test_contexts");

        CodeArea func3CodeArea = getFunctionCodeArea(processInfo, "tcf_test_func3");
        createLineBreakpoint(func3CodeArea.file, func3CodeArea.start_line, "test_query", "test_contexts");

        try {
            fBreakpointsViewListener.reset();
            fBreakpointsViewViewer.getPresentationContext().setProperty(
                IBreakpointUIConstants.PROP_BREAKPOINTS_ORGANIZERS, 
                new IBreakpointOrganizer[] { createScopeOrganizer() }); 
            fBreakpointsViewViewer.setAutoExpandLevel(-1);
            fBreakpointsViewViewer.setActive(true);
            fBreakpointsViewListener.waitTillFinished(CONTENT_SEQUENCE_COMPLETE | LABEL_UPDATES_RUNNING);

            VirtualItem bpItem = fBreakpointsViewListener.findElement(new Pattern[] { 
                Pattern.compile(".*Global.*"), 
                Pattern.compile(".*" + new Path(func0CodeArea.file).lastSegment() + ".*line: " + func0CodeArea.start_line + ".*"),}); // thread
            Assert.assertTrue(bpItem != null);

            bpItem = fBreakpointsViewListener.findElement(new Pattern[] { 
                Pattern.compile(".*test_query.*"), 
                Pattern.compile(".*" + new Path(func1CodeArea.file).lastSegment() + ".*line: " + func1CodeArea.start_line + ".*"),}); // thread
            Assert.assertTrue(bpItem != null);

            bpItem = fBreakpointsViewListener.findElement(new Pattern[] { 
                Pattern.compile(".*test_contexts.*"), 
                Pattern.compile(".*" + new Path(func2CodeArea.file).lastSegment() + ".*line: " + func2CodeArea.start_line + ".*"),}); // thread
            Assert.assertTrue(bpItem != null);

            bpItem = fBreakpointsViewListener.findElement(new Pattern[] { 
                Pattern.compile(".*test_query.*test_contexts.*"), 
                Pattern.compile(".*" + new Path(func3CodeArea.file).lastSegment() + ".*line: " + func3CodeArea.start_line + ".*"),}); // thread
            Assert.assertTrue(bpItem != null);

        } catch (AssertionFailedError e) {
            System.out.print("Breakpoints view dump: \n:" + fBreakpointsViewViewer.toString());
            throw e;
        }
    }

    private IBreakpointOrganizer createScopeOrganizer()  {
        class MyBreakpointOrganizer extends BreakpointScopeOrganizer implements IBreakpointOrganizer {
            public String getLabel() {
                return "Scope";
            }

            public ImageDescriptor getImageDescriptor() {
                return null;
            }
            
            public String getIdentifier() {
                return "tcf_scope";
            }
            
            public String getOthersLabel() {
                return "others";
            }
            
        }
        return new MyBreakpointOrganizer();
    }
    
}
