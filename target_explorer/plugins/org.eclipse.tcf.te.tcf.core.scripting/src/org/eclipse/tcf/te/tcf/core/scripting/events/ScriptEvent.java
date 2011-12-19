/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.scripting.events;

import java.util.EventObject;

/**
 * Script event implementation.
 */
public class ScriptEvent extends EventObject {
    private static final long serialVersionUID = -5350037587555199985L;

	/**
	 * Script event types.
	 */
	public static enum Type { START, OUTPUT, STOP }

	/**
	 * Immutable script event message.
	 */
	public static final class Message {
		/** The message type */
		public final char type;
		/** The message text */
		public final String text;

		/**
         * Constructor.
         */
        public Message(char type, String text) {
        	this.type = type;
        	this.text = text;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
        	StringBuilder buffer = new StringBuilder(getClass().getName());
        	buffer.append(": type = '"); //$NON-NLS-1$
        	buffer.append(type);
        	buffer.append("', text = '"); //$NON-NLS-1$
        	buffer.append(text);
        	buffer.append("'"); //$NON-NLS-1$

            return buffer.toString();
        }
	}


	private Type type;
	private Message message;


	/**
	 * Constructor.
	 *
	 * @param source The source object. Must not be <code>null</code>.
	 * @param type The script event type. Must not be <code>null</code>.
	 * @param message The script event message or <code>null</code>.
	 *
	 * @exception IllegalArgumentException if type == null.
	 */
	public ScriptEvent(Object source, Type type, Message message) {
		super(source);

		if (type == null) throw new IllegalArgumentException("null type"); //$NON-NLS-1$
		this.type = type;

		this.message = message;
	}

	/**
	 * Returns the script event type.
	 *
	 * @return The script event type.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Returns the script event message.
	 *
	 * @return The script event message or <code>null</code>.
	 */
	public Message getMessage() {
		return message;
	}
}
