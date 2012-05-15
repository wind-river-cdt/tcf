/*******************************************************************************
 * Copyright (c) 2009, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.test.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.tcf.protocol.Protocol;

/**
 * Future implementation that executes a call on the TCF Protocol
 * thread.
 */
public abstract class Task<V> implements Future<V> , Callable<V> {

    private final AtomicBoolean fInvoked = new AtomicBoolean(false);
    private final FutureTask<V> fInternalFT = new FutureTask<V>(this);
    
    public boolean cancel(boolean mayInterruptIfRunning) {
        return fInternalFT.cancel(mayInterruptIfRunning);
    }

    public boolean isCancelled() {
        return fInternalFT.isCancelled();
    }

    public boolean isDone() {
        return fInternalFT.isDone();
    }

    public V get() throws InterruptedException, ExecutionException {
        invoke();
        return fInternalFT.get();
    }

    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        invoke();
        return fInternalFT.get(timeout, unit);
    }

    public void invoke() {
        if (!fInvoked.getAndSet(true)) {
            Protocol.invokeLater(fInternalFT);
        }
    }
    
    abstract public V call() throws Exception;
}
