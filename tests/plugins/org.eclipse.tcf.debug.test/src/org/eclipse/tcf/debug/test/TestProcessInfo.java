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

import org.eclipse.tcf.services.IRunControl.RunControlContext;

/**
 * 
 */
public class TestProcessInfo {
    public TestProcessInfo(String testId,
        RunControlContext testCtx,
        String processId,
        String threadId,
        RunControlContext threadCtx) 
    {
        fTestId = testId;
        fTestCtx = testCtx;
        fProcessId = processId;
        fThreadId = threadId;
        fThreadCtx = threadCtx;
    }
    
    public String fTestId;
    public RunControlContext fTestCtx;
    public String fProcessId = "";
    public String fThreadId = "";
    public RunControlContext fThreadCtx;
}
