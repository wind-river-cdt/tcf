/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.test;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextProvider;
import org.eclipse.debug.ui.sourcelookup.ISourceDisplay;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * 
 */
public class TestSourceDisplayService implements IDebugContextListener {

    private IDebugContextProvider fDebugContextProvider;
    
    public TestSourceDisplayService(IDebugContextProvider debugContextProvider) {
        fDebugContextProvider = debugContextProvider;
        fDebugContextProvider.addDebugContextListener(this);
    }
    
    public void dispose() {
        fDebugContextProvider.removeDebugContextListener(this);
    }
    
    public void debugContextChanged(DebugContextEvent event) {
        if ((event.getFlags() & DebugContextEvent.ACTIVATED) == 0) return;
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) return;
        
        if (event.getContext() instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection)event.getContext();
            if (structuredSelection.size() == 1) {
                Object context = (structuredSelection).getFirstElement();
                IWorkbenchPage page = null;
                page = window.getActivePage();
                ISourceDisplay adapter = (ISourceDisplay) DebugPlugin.getAdapter(context, ISourceDisplay.class);
                if (adapter != null) { 
                    adapter.displaySource(context, page, false);
                }
            }
        }
    }
    
}
