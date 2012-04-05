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
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
