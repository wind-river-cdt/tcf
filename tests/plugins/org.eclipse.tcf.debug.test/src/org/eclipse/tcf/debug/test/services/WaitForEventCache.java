/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.test.services;

import org.eclipse.tcf.debug.test.util.AbstractCache;

class WaitForEventCache<V> extends AbstractCache<V> implements IWaitForEventCache<V> {
    @Override
    protected void retrieve() { } // no-op - called by listener
    @Override
    protected void canceled() { } // no-op - no command sent

    public void eventReceived(V data) {
        if (!isValid()) {
            set(data, null, true); // notify listeners
        }
    }
}