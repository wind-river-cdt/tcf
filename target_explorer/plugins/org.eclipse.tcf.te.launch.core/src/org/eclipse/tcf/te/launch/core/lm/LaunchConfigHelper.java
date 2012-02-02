/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.lm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;

/**
 * Static launch configuration utility implementations.
 */
public class LaunchConfigHelper {

	/**
	 * Get a sorted list of all launch modes for the given launch configuration type.
	 *
	 * @param launchConfigType The launch configuration type.
	 * @param reverse If <code>true</code> the sorted list order is reversed.
	 *
	 * @return Sorted list of supported launch modes.
	 */
	public static String[] getLaunchConfigTypeModes(ILaunchConfigurationType launchConfigType, boolean reverse) {
		return getLaunchConfigTypeModes(new ILaunchConfigurationType[] { launchConfigType }, reverse);
	}

	/**
	 * Get a sorted list of all launch modes for the given launch configuration types.
	 *
	 * @param launchConfigTypes The launch configuration types.
	 * @param reverse If <code>true</code> the sorted list order is reversed.
	 *
	 * @return Sorted list of supported launch modes.
	 */
	public static String[] getLaunchConfigTypeModes(ILaunchConfigurationType[] launchConfigTypes, boolean reverse) {
		List<String> modes = new ArrayList<String>();
		for (ILaunchConfigurationType launchConfigType : launchConfigTypes) {
			for (Object modeCombination : launchConfigType.getSupportedModeCombinations()) {
				if (((Set<?>) modeCombination).size() == 1) {
					String mode = (String) ((Set<?>) modeCombination).toArray()[0];
					if (!modes.contains(mode)) {
						modes.add(mode);
					}
				}
			}
		}
		return getLaunchModesSorted(modes.toArray(new String[modes.size()]), reverse);
	}

	/**
	 * Gets a sorted list of launch mode identifiers.
	 *
	 * @param launchModes The launch modes. Must not be <code>null</code>.
	 * @param reverse If <code>true</code> the sorted list order is reversed.
	 *
	 * @return Sorted list of launch mode identifiers.
	 */
	public static String[] getLaunchModesSorted(ILaunchMode[] launchModes, boolean reverse) {
		Assert.isNotNull(launchModes);

		String[] modes = new String[launchModes.length];
		for (int i = 0; i < launchModes.length; i++) {
			modes[i] = launchModes[i].getIdentifier();
		}
		return getLaunchModesSorted(modes, reverse);
	}

	/**
	 * Gets a sorted list of launch mode identifiers.
	 *
	 * @param launchModes The unsorted list of launch modes identifiers. Must not be <code>null</code>.
	 * @param reverse If <code>true</code> the sorted list order is reversed.
	 *
	 * @return Sorted list of launch mode identifiers.
	 */
	public static String[] getLaunchModesSorted(String[] launchModes, final boolean reverse) {
		Assert.isNotNull(launchModes);

		// sort the list of launch modes
		// Run is always the first, followed by Debug.
		// All other modes are sorted alphabetically at the end of the list.
		Arrays.sort(launchModes, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				if (o1.equals(ILaunchManager.RUN_MODE) && !o2.equals(ILaunchManager.RUN_MODE)) return reverse ? 1 : -1;
				if (o2.equals(ILaunchManager.RUN_MODE) && !o1.equals(ILaunchManager.RUN_MODE)) return reverse ? -1 : 1;
				if (o1.equals(ILaunchManager.DEBUG_MODE) && !o2.equals(ILaunchManager.DEBUG_MODE)) return reverse ? 1 : -1;
				if (o2.equals(ILaunchManager.DEBUG_MODE) && !o1.equals(ILaunchManager.DEBUG_MODE)) return reverse ? -1 : 1;
				return reverse ? o2.compareTo(o1) : o1.compareTo(o2);
			}
		});

		return launchModes;
	}
}
