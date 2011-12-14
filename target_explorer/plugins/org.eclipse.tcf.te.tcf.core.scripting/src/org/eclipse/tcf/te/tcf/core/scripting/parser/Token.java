/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.scripting.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;

/**
 * Script token. Created by the script parser on parsing the script.
 */
public final class Token {
	// The service name
	private String serviceName;
	// The command name
	private String commandName;
	// The command arguments
	private List<Object> arguments;

	/**
     * Constructor.
     */
    public Token() {
    }

    /**
     * Sets the service name.
     *
     * @param serviceName The service name. Must not be <code>null</code>.
     */
    public void setServiceName(String serviceName) {
    	Assert.isNotNull(serviceName);
    	this.serviceName = serviceName;
    }

    /**
     * Gets the service name.
     *
     * @return The service name or <code>null</code>.
     */
    public String getServiceName() {
    	return serviceName;
    }

    /**
     * Sets the command name.
     *
     * @param commandName The command name. Must not be <code>null</code>.
     */
    public void setCommandName(String commandName) {
    	Assert.isNotNull(commandName);
    	this.commandName = commandName;
    }

    /**
     * Returns the command name.
     *
     * @return The command name or <code>null</code>.
     */
    public String getCommandName() {
    	return commandName;
    }

    /**
     * Adds an argument to the command arguments list.
     *
     * @param arg The argument. Must not be <code>null</code>.
     */
    public void addArgument(Object arg) {
    	Assert.isNotNull(arg);
    	if (arguments == null) arguments = new ArrayList<Object>();
    	arguments.add(arg);
    }

    /**
     * Returns the command arguments.
     *
     * @return The command arguments or an empty array.
     */
    public Object[] getArguments() {
    	return arguments != null ? arguments.toArray() : new Object[0];
    }
}
