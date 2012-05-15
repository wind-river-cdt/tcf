/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.test.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Exception that combines several exceptions for propagation to client. 
 */
public class AggregateError extends Exception {
    private static final long serialVersionUID = 1L;
    
    final private List<Throwable> fChildren = Collections.synchronizedList(new ArrayList<Throwable>(1));
    
    public AggregateError(String message) {
        super(message);
    }
    
    public void add(Throwable child) {
        boolean initCause = false;
        synchronized(fChildren) {
            if (fChildren.isEmpty()) {
                initCause = true;
            }
            fChildren.add(child);
        }
        if (initCause) {
            super.initCause(child);
        }
    }
    
    public List<Throwable> getChildren() {
        return fChildren;
    }
}
