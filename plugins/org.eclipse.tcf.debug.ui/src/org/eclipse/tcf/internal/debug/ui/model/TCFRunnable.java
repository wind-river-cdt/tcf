/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.debug.ui.model;

import org.eclipse.debug.core.IRequest;
import org.eclipse.tcf.protocol.Protocol;


public abstract class TCFRunnable implements Runnable {

    private final IRequest request;

    protected boolean done;

    public TCFRunnable(IRequest request) {
        this.request = request;
        Protocol.invokeLater(this);
    }

    public void done() {
        assert !done;
        done = true;
        // Don't call Display.asyncExec: display thread can be blocked waiting for the request.
        // For example, display thread is blocked for action state update requests.
        // Calling back into Eclipse on TCF thread is dangerous too - if Eclipse blocks TCF thread
        // we can get deadlocked. Might need a new thread (or Job) to make this call safe.
        request.done();
    }
}
