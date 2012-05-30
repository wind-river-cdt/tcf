/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.cdt.ui.breakpoints;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.tcf.internal.debug.model.TCFLaunch;

public class ToggleTCFBreakpointTester extends PropertyTester {
    /*
     * (non-Javadoc)
     * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
     */
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if ( "isTCFBreakpointSupported".equals(property) ) { //$NON-NLS-1$
            ILaunch launch = getAttributeLaunch();
            if (launch != null && launch instanceof TCFLaunch) {
                return true;
            }
        }
        return false;
    }

    protected ILaunch getAttributeLaunch() {
        ILaunch launch = null;
        IAdaptable dbgContext = DebugUITools.getDebugContext();
        if (dbgContext != null) {
            launch = (ILaunch)dbgContext.getAdapter(ILaunch.class);
        }
        return launch;
    }
}
