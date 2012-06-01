/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems - Converted into a command
 *******************************************************************************/
package org.eclipse.tcf.internal.cdt.ui.breakpoints;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.ui.breakpoints.provisional.IBreakpointContainer;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Presents a custom properties dialog to configure the attibutes of a C/C++ breakpoint.
 */
public class BreakpointScopeCategoryPropertiesHandler extends AbstractHandler {

    /**
     * Constructor for CBreakpointPropertiesAction.
     */
    public BreakpointScopeCategoryPropertiesHandler() {
        super();
    }

    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
        final BreakpointScopeCategory category = getScopeCategory(event.getApplicationContext());

        if (part != null && category != null) {
            PreferenceDialog dlg = PreferencesUtil.createPropertyDialogOn(part.getSite().getShell(), category, null, null, null);
            if (dlg != null) {
                dlg.open();
            }
        }

        return null;
    }

    @Override
    public void setEnabled(Object evaluationContext) {
        setBaseEnabled( getScopeCategory(evaluationContext) != null );
    }

    private BreakpointScopeCategory getScopeCategory(Object evaluationContext) {
        if (evaluationContext instanceof IEvaluationContext) {
            Object s = ((IEvaluationContext) evaluationContext).getVariable(ISources.ACTIVE_MENU_SELECTION_NAME);
            if (s instanceof IStructuredSelection) {
                IStructuredSelection ss = (IStructuredSelection)s;
                if (ss.size() == 1 && ss.getFirstElement() instanceof IBreakpointContainer) {
                    IAdaptable category = ((IBreakpointContainer)ss.getFirstElement()).getCategory();
                    if (category instanceof BreakpointScopeCategory) {
                        return (BreakpointScopeCategory)category;
                    }
                }
            }
        }
        return null;
    }
}
