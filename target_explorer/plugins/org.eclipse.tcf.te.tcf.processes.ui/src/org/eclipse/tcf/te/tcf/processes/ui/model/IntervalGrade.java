/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.model;

/**
 * The data model to contain the information of a refreshing grade including
 * its name and its value.
 */
public class IntervalGrade {
	// The grade's name.
	private String name;
	// The grade's value.
	private int value;
	
	/**
	 * Constructor.
	 */
	public IntervalGrade(String name, int value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Get the name of the grade.
	 * 
	 * @return The grade's name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the name of the grade.
	 * 
	 * @param name The grade's name.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Get the grade's value.
	 * 
	 * @return The grade's value.
	 */
	public int getValue() {
		return value;
	}
	
	/**
	 * Set the grade's value.
	 * 
	 * @param value The grade's value.
	 */
	public void setValue(int value) {
		this.value = value;
	}
}