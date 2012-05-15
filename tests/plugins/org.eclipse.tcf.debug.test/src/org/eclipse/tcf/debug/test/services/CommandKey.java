/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.test.services;


/**
 * 
 */
public abstract class CommandKey<V> extends Key<V> {
    Object fClientKey;
    
    CommandKey(Class<V> cacheClass, Object clientKey) {
        super(cacheClass);
        fClientKey = clientKey;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj) && obj instanceof CommandKey<?>) {
            return ((CommandKey<?>)obj).fClientKey.equals(fClientKey);
        } 
        return false;        
    }
    
    @Override
    public int hashCode() {
        return super.hashCode() + fClientKey.hashCode();
    }
}
