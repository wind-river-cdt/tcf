/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.osgi.services;

import org.eclipse.tcf.protocol.IPeer;

/**
 * A service whose purpose is to provide value-add related information for a
 * given peer.
 */
public interface IValueAddService {

    /**
     * Returns the redirection path to use for the given peer. The redirection
     * path is encoded as string where the id's to redirect the communication
     * through, are separated by '/'.
     * <p>
     * If there are no value-adds to redirect through, the passed in peer id is
     * returned as is.
     * <p>
     * If there are value-add's to redirect through, the passed in peer id will
     * be prefixed with the value-add id's to redirect through.
     *
     * @param peerId The peer id. Must not be <code>null</code>.
     * @param done The client callback. Must not be <code>null</code>.
     */
    public void getRedirectionPath(IPeer peer, DoneGetRedirectionPath done);

    /**
     * Client call back interface for getRedirectionPath(...).
     */
    interface DoneGetRedirectionPath {
        /**
         * Called when the redirection path has been fully determined.
         *
         * @param error The error description if operation failed, <code>null</code> if succeeded.
         * @param redirectionPath The redirection path or <code>null</code>.
         */
        void doneGetRedirectionPath(Throwable error, String redirectionPath);
}
}
