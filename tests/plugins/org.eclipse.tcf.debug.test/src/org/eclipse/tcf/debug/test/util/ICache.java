/*******************************************************************************
 * Copyright (c) 2010, 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.test.util;


/** 
 * The interface for a general purpose cache that caches the result of a single 
 * request.  Implementations need to provide the logic to fetch data from an
 * asynchronous data source.
 * <p>
 * This cache requires an executor to use.  The executor is used to synchronize 
 * access to the cache state and data.   
 * </p>
 * @since 2.2
 */ 
public interface ICache<V> {
    
	/**
	 * Returns the current data value held by this cache. Clients should first
	 * call isValid() to determine if the data is up to date. Calling this when
	 * in the invalid state will throw an IllegalStateException
	 */
    public V getData();

	/**
	 * Returns the status of the source request held by this cache. Clients
	 * should first call isValid() to determine if the data is up to date.
	 * Calling this when in the invalid state will throw an
	 * IllegalStateException
	 */    
    public Throwable getError();

	/**
	 * Asks the cache to update its value from the source. Typically, this 
	 * method is called by a client after it discovers the cache is invalid 
	 * via {@link #isValid()}.
	 * 
	 * <p>If the cache is already valid, the cache is not updated again from 
	 * source.  Instead the callback is completed next time the cache state is 
	 * changed.  Clients can use this feature to be notified when the cache is 
	 * being reset.
	 * </p>
	 * @param cb
	 *            Callback that is called when cache becomes valid.
	 */
    public void wait(Callback cb);

    /**
     * Returns <code>true</code> if the cache is currently valid.  I.e. 
     * whether the cache can return a value immediately without first 
     * retrieving it from the data source. 
     */
    public boolean isValid();
}
