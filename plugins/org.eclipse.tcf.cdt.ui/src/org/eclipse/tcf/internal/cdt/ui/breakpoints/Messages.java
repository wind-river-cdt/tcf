/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.cdt.ui.breakpoints;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tcf.internal.cdt.ui.breakpoints.messages"; //$NON-NLS-1$

    public static String TCFThreadFilterQueryExpressionStore;
    public static String TCFThreadFilterQueryModeButtonState;
    public static String TCFThreadFilterQueryButtonBasic;
    public static String TCFThreadFilterQueryButtonAdvanced;
    public static String TCFThreadFilterQueryButtonEdit;
    public static String TCFThreadFilterQueryAdvancedLabel;
    public static String TCFThreadFilterQueryTreeViewLabel;
    public static String TCFThreadFilterEditorFormatError;

    public static String BreakpointScopeCategory_filter_label;
    public static String BreakpointScopeCategory_contexts_label;
    public static String BreakpointScopeCategory_global_label;
    

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
