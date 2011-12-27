/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
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
	String DEFAULT_INTERVAL_GRADES = "Slow:10|Normal:5|Fast:1"; //$NON-NLS-1$
	// The key to access the max count of the most recently used intervals.
	String PREF_INTERVAL_MRU_COUNT = "PrefIntervalMRUCount"; //$NON-NLS-1$
	// The key to access the list of the most recently used intervals.
	String PREF_INTERVAL_MRU_LIST = "PrefIntervalMRUList"; //$NON-NLS-1$
	// The default count of the most recently used intervals.
	int DEFAULT_INTERVAL_MRU_COUNT = 3;
	// The key to access the last selected interval.
	String PREF_LAST_INTERVAL = "PrefLastInterval"; //$NON-NLS-1$
	// The default last selected interval value.
	int DEFAULT_LAST_INTERVAL = 5;
}
