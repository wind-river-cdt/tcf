/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.services.contexts.interfaces;

import org.eclipse.tcf.protocol.IService;

/**
 * TCF contexts service.
 */
public interface IContexts extends IService {

	/**
	 * The service name.
	 */
	public static final String NAME = "Contexts"; //$NON-NLS-1$

	/**
	 * Context handler delegate.
	 */
	public interface IDelegate {

		/**
		 * Checks if the delegate can handle the context identified by the
		 * given id.
		 *
		 * @param contextID The context id. Must not be <code>null</code>.
		 * @return <code>True</code> if the delegate can handle the context id, <code>false</code> otherwise.
		 */
		public boolean canHandle(String contextID);

		/**
		 * Returns the name of the context identified by the given id.
		 *
		 * @param contextID The context id. Must not be <code>null</code>.
		 * @return The name of the context, or <code>null</code> if the context does not exist
		 *         or is not handled by this delegate.
		 */
		public String getName(String contextID);

		/**
		 * Checks if the context identified by the given id is available. Available in this
		 * context means other clients can access data for this context.
		 *
		 * @param contextID The context id. Must not be <code>null</code>.
		 * @return <code>True</code> if the context is available, <code>false</code> otherwise.
		 */
		public boolean isAvailable(String contextID);

	    /**
	     * Client call back interface for makeAvailable().
	     */
	    public interface DoneMakeAvailable {
	        /**
	         * Called when context has been made available.
	         *
	         * @param error The error description if the operation failed, <code>null</code> if succeeded.
	         */
	        void doneMakeAvailable(Exception error);
	    }

		/**
		 * Attempt to make the context identified by the given id available to other
		 * clients.
		 *
		 * @param contextID The context id. Must not be <code>null</code>.
		 * @param done The call back interface called when the operation is completed. Must not be <code>null</code>.
		 */
		public void makeAvailable(String contextID, DoneMakeAvailable done);
	}

	/**
	 * Returns the first delegate that can handle the given context ID.
	 *
	 * @param contextID The context id. Must not be <code>null</code>.
	 * @return The delegate or <code>null</code>.
	 */
	public IDelegate getDelegate(String contextID);

	/**
	 * Adds the given context handler delegate to the service.
	 *
	 * @param delegate The context handler delegate. Must not be <code>null</code>.
	 */
	public void addDelegate(IDelegate delegate);

	/**
	 * Removes the given context handler delegate from the service.
	 *
	 * @param delegate The context handler delegate. Must not be <code>null</code>.
	 */
	public void removeDelegate(IDelegate delegate);
}
