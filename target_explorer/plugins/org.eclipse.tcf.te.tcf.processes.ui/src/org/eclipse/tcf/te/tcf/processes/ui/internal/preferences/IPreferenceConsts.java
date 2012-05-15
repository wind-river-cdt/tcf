/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.internal.preferences;

/**
 * The constants for the preferences used in the process monitor.
 */
public interface IPreferenceConsts {
	// The key to access the interval grades for refreshing.
	String PREF_INTERVAL_GRADES = "PrefIntervalGrades"; //$NON-NLS-1$
	// The default value of the interval grades.
	String DEFAULT_INTERVAL_GRADES = "Off:0|Default:15"; //$NON-NLS-1$
	// The key to access the max count of the most recently used intervals.
	String PREF_INTERVAL_MRU_COUNT = "PrefIntervalMRUCount"; //$NON-NLS-1$
	// The key to access the list of the most recently used intervals.
	String PREF_INTERVAL_MRU_LIST = "PrefIntervalMRUList"; //$NON-NLS-1$
	// The default count of the most recently used intervals.
	int DEFAULT_INTERVAL_MRU_COUNT = 5;
}
