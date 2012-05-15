/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
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

import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDeltaVisitor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ITreeModelViewer;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.AbstractDebugContextProvider;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Copied from org.eclipse.debug.internal.ui.views.launch.LaunchView class.
 * 
 * This debug context provider emulates the Debug view debug context 
 * provider behavior. 
 */
@SuppressWarnings("restriction")
public class TestDebugContextProvider extends AbstractDebugContextProvider implements IModelChangedListener, ISelectionChangedListener{
    
    private IWorkbenchWindow fWindow = null;
    private ISelection fContext = null;
    private ITreeModelViewer fViewer = null;
    private Visitor fVisitor = new Visitor();
    
    class Visitor implements IModelDeltaVisitor {
        public boolean visit(IModelDelta delta, int depth) {
            if ((delta.getFlags() & (IModelDelta.STATE | IModelDelta.CONTENT)) > 0) {
                // state and/or content change
                if ((delta.getFlags() & IModelDelta.SELECT) == 0) {
                    // no select flag
                    if ((delta.getFlags() & IModelDelta.CONTENT) > 0) {
                        // content has changed without select >> possible re-activation
                        possibleChange(getViewerTreePath(delta), DebugContextEvent.ACTIVATED);
                    } else if ((delta.getFlags() & IModelDelta.STATE) > 0) {
                        // state has changed without select >> possible state change of active context
                        possibleChange(getViewerTreePath(delta), DebugContextEvent.STATE);
                    }
                }
            }
            return true;
        }   
    }
    
    /**
     * Returns a tree path for the node, *not* including the root element.
     * 
     * @param node
     *            model delta
     * @return corresponding tree path
     */
    private TreePath getViewerTreePath(IModelDelta node) {
        ArrayList<Object> list = new ArrayList<Object>();
        IModelDelta parentDelta = node.getParentDelta();
        while (parentDelta != null) {
            list.add(0, node.getElement());
            node = parentDelta;
            parentDelta = node.getParentDelta();
        }
        return new TreePath(list.toArray());
    }
    
    public TestDebugContextProvider(ITreeModelViewer viewer) {
        super(null);
        fViewer = viewer;
        fViewer.addModelChangedListener(this);
        fViewer.addSelectionChangedListener(this);
        fWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (fWindow != null) {
            DebugUITools.getDebugContextManager().getContextService(fWindow).addDebugContextProvider(this);
        }
    }
    
    protected void dispose() { 
        if (fWindow != null) {
            DebugUITools.getDebugContextManager().getContextService(fWindow).removeDebugContextProvider(this);
        }
        fContext = null;
        fViewer.removeModelChangedListener(this);
        fViewer.removeSelectionChangedListener(this);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.contexts.IDebugContextProvider#getActiveContext()
     */
    public synchronized ISelection getActiveContext() {
        return fContext;
    }   
    
    protected void activate(ISelection selection) {
        synchronized (this) {
            fContext = selection;
        }
        fire(new DebugContextEvent(this, selection, DebugContextEvent.ACTIVATED));
    }
    
    protected void possibleChange(TreePath element, int type) {
        DebugContextEvent event = null;
        synchronized (this) {
            if (fContext instanceof ITreeSelection) {
                ITreeSelection ss = (ITreeSelection) fContext;
                if (ss.size() == 1) {
                    TreePath current = ss.getPaths()[0];
                    if (current.startsWith(element, null)) {
                        if (current.getSegmentCount() == element.getSegmentCount()) {
                            event = new DebugContextEvent(this, fContext, type);
                        } else {
                            // if parent of the currently selected element 
                            // changes, issue event to update STATE only
                            event = new DebugContextEvent(this, fContext, DebugContextEvent.STATE);
                        }
                    }
                }
            } 
        }
        if (event == null) {
            return;
        }
        if (Display.getDefault().getThread() == Thread.currentThread()) {
            fire(event);
        } else {
            final DebugContextEvent finalEvent = event;
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    // verify selection is still the same context since job was scheduled
                    synchronized (TestDebugContextProvider.this) {
                        if (fContext instanceof IStructuredSelection) {
                            IStructuredSelection ss = (IStructuredSelection) fContext;
                            Object changed = ((IStructuredSelection)finalEvent.getContext()).getFirstElement();
                            if (!(ss.size() == 1 && ss.getFirstElement().equals(changed))) {
                                return;
                            }
                        }
                    }
                    fire(finalEvent);
                }
            });
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener#modelChanged(org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta)
     */
    public void modelChanged(IModelDelta delta, IModelProxy proxy) {
        delta.accept(fVisitor);
    }

    public void selectionChanged(SelectionChangedEvent event) {
        activate(event.getSelection());
    }
    
}
