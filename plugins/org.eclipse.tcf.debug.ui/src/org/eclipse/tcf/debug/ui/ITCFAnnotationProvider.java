/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.ui;

import org.eclipse.ui.IWorkbenchWindow;

/**
 * TCF clients can implement ITCFAnnotationProvider to manage debugger annotations
 * in the Eclipse workspace.
 *
 * Debugger annotations include editor markers for current instruction pointer,
 * stack frame addresses, and breakpoint planting status.
 *
 * TCF will use internal annotation provider if no suitable provider is found
 * through "annotation_provider" extension point for current selection in the Debug view.
 */
public interface ITCFAnnotationProvider {

    /**
     * Check if this provider recognizes type of a selection.
     * @param selection
     * @return true if the selection is supported by this provider.
     */
    boolean isSupportedSelection(Object selection);


    /**
     * Update all annotations in a workbench window for given selection in the Debug view.
     * @param window
     * @param selection
     */
    void updateAnnotations(IWorkbenchWindow window, final Object selection);
}
