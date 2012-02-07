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
package org.eclipse.tcf.internal.debug.model;

import java.util.Map;

import org.eclipse.tcf.protocol.IErrorReport;
import org.eclipse.tcf.services.IRunControl;

public class TCFContextState {
    public boolean is_suspended;

    public String suspend_pc;
    public String suspend_reason;
    public Map<String,Object> suspend_params;

    /**
     * Return true if the context is currently running in reverse.
     */
    public boolean isReversing() {
        if (is_suspended) return false;
        if (suspend_params == null) return false;
        Object reversing = suspend_params.get(IRunControl.STATE_REVERSING);
        if (reversing instanceof Boolean) return ((Boolean)reversing).booleanValue();
        if (reversing instanceof String) return ((String)reversing).equals("true");
        return false;
    }

    /**
     * Return true if this context cannot be accessed because it is not active.
     * Not active means the target is suspended, but this context is not one that is
     * currently scheduled to run on a target CPU, and the debuggers don't support
     * access to register values and other properties of such contexts.
     */
    public boolean isNotActive() {
        if (!is_suspended) return false;
        if (suspend_params == null) return false;
        @SuppressWarnings("unchecked")
        Map<String,Object> attrs = (Map<String,Object>)suspend_params.get(IRunControl.STATE_PC_ERROR);
        if (attrs != null) {
            Number n = (Number)attrs.get(IErrorReport.ERROR_CODE);
            if (n != null) return n.intValue() == IErrorReport.TCF_ERROR_NOT_ACTIVE;
        }
        return false;
    }
}
