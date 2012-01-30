/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.debug.ui.commands;

import java.util.ArrayList;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.tcf.internal.debug.model.TCFLaunch;
import org.eclipse.tcf.internal.debug.ui.model.TCFModelManager;
import org.eclipse.tcf.internal.debug.ui.model.TCFNode;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public abstract class AbstractActionDelegate
implements IViewActionDelegate, IActionDelegate2, IWorkbenchWindowActionDelegate, IObjectActionDelegate {

    private IAction action;
    private IViewPart view;
    private IWorkbenchWindow window;
    private ISelection selection;

    public void init(IAction action) {
        this.action = action;
    }

    public void init(IViewPart view) {
        this.view = view;
    }

    public void init(IWorkbenchWindow window) {
        this.window = window;
    }

    public void dispose() {
        action = null;
        view = null;
        window = null;
    }

    public void setActivePart(IAction action, IWorkbenchPart part) {
        this.action = action;
        view = null;
        if (part instanceof IViewPart) view = (IViewPart)part;
        window = part.getSite().getWorkbenchWindow();
    }

    public void run(IAction action) {
        IAction action0 = this.action;
        try {
            this.action = action;
            run();
        }
        finally {
            this.action = action0;
        }
    }

    public void runWithEvent(IAction action, Event event) {
        run(action);
    }

    public void selectionChanged(IAction action, ISelection selection) {
        this.selection = selection;
        IAction action0 = this.action;
        try {
            this.action = action;
            selectionChanged();
        }
        finally {
            this.action = action0;
        }
    }

    public IAction getAction() {
        return action;
    }

    public IViewPart getView() {
        return view;
    }

    public IWorkbenchWindow getWindow() {
        if (view != null) return view.getSite().getWorkbenchWindow();
        if (window != null) return window;
        return null;
    }

    public ISelection getSelection() {
        return selection;
    }

    public TCFNode getSelectedNode() {
        if (selection instanceof IStructuredSelection) {
            final Object o = ((IStructuredSelection)selection).getFirstElement();
            if (o instanceof TCFNode) return (TCFNode)o;
            if (o instanceof TCFLaunch) return TCFModelManager.getRootNodeSync((TCFLaunch)o);
        }
        return null;
    }

    public TCFNode[] getSelectedNodes() {
        ArrayList<TCFNode> list = new ArrayList<TCFNode>();
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection s = (IStructuredSelection)selection;
            if (s.size() > 0) {
                for (final Object o : s.toArray()) {
                    if (o instanceof TCFNode) {
                        list.add((TCFNode)o);
                    }
                    else if (o instanceof TCFLaunch) {
                        TCFNode n = TCFModelManager.getRootNodeSync((TCFLaunch)o);
                        if (n != null) list.add(n);
                    }
                }
            }
        }
        return list.toArray(new TCFNode[list.size()]);
    }

    protected abstract void selectionChanged();

    protected abstract void run();
}
