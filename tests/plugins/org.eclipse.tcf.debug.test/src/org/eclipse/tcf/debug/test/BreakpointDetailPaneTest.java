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

import java.util.regex.Pattern;

import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualItem;
import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualTreeModelViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.debug.ui.ITCFDebugUIConstants;
import org.eclipse.tcf.internal.cdt.ui.breakpoints.TCFBreakpointScopeDetailPane.ContextQueryElement;
import org.eclipse.tcf.internal.cdt.ui.breakpoints.TCFBreakpointScopeDetailPane.ScopeDetailInputObject;
import org.eclipse.tcf.internal.debug.ui.launch.TCFLaunchContext;
import org.junit.Assert;

@SuppressWarnings("restriction")
public class BreakpointDetailPaneTest extends AbstractTcfUITest
{
    private BreakpointsListener fBpListener;
    
    protected VirtualTreeModelViewer fContextQueryViewViewer;
    protected VirtualViewerUpdatesListener fContextQueryViewListener;
    

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
        
        final Display display = Display.getDefault();
        display.syncExec(new Runnable() {
            public void run() {
                fContextQueryViewViewer = new VirtualTreeModelViewer(
                    display, SWT.NONE, new PresentationContext(ITCFDebugUIConstants.ID_CONTEXT_QUERY_VIEW));
                fContextQueryViewListener = new VirtualViewerUpdatesListener(fContextQueryViewViewer);
            }
        });

    }

    @Override
    protected void tearDown() throws Exception {
        final Display display = Display.getDefault();
        display.syncExec(new Runnable() {
            public void run() {
                fContextQueryViewListener.dispose();
                fContextQueryViewViewer.dispose();
            }
        });
        fBpListener.dispose();
        super.tearDown();
    }

    public void testContextAddedOnLineBrakpointCreate() throws Exception {
        initProcessModel("tcf_test_func0");

        final String query = "pid="+fProcessId;
        
        fContextQueryViewListener.reset();
        Display.getDefault().syncExec(new Runnable() { public void run() {
            fContextQueryViewViewer.setAutoExpandLevel(-1);
            fContextQueryViewViewer.setInput(new ScopeDetailInputObject(new ContextQueryElement(query, null)));
        }});
        fContextQueryViewListener.waitTillFinished(CONTENT_SEQUENCE_COMPLETE | LABEL_UPDATES_RUNNING);                
        
        VirtualItem scopeItem = fContextQueryViewListener.findElement(new Pattern[] { Pattern.compile(".*pid\\="+fProcessId+"*.") });
        if (scopeItem == null) {
            Assert.fail("Scope item not found. \n\nContext query view dump: \n:" + fContextQueryViewViewer.toString());
        }

    }

}
