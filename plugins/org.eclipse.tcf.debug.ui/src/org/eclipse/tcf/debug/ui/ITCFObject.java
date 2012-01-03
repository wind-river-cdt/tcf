/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.debug.ui;

import org.eclipse.tcf.protocol.IChannel;

/**
 * ITCFObject is an interface that is implemented by all TCF debug model elements.
 * A visual element in a debugger view can be adapted to this interface -
 * if the element represents a remote TCF object.
 * Clients can get communication channel and ID of the object,
 * and use them to access the object through TCF service interfaces.
 */
public interface ITCFObject {

    /**
     * Get TCF ID of the object.
     * @return TCF ID
     */
    public String getID();

    /**
     * Get IChannel of the debug model that owns this object.
     * @return IChannel object
     */
    public IChannel getChannel();
}
