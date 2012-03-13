/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.tcf.te.ui.views.activator.UIPlugin;
import org.eclipse.tcf.te.ui.views.internal.preferences.IPreferenceConsts;

/**
 * The list implementation for MRU items for a specified preference key which is used 
 * to retrieve the list from a preference store. It also provide a function to update the MRU
 * list from a delta set. 
 */
public class MRUList extends ArrayList<String> {
    private static final long serialVersionUID = 1L;
    // The preference key used to access the MRU list.
	private String prefKey;

	/**
	 * Construct the MRU list using a preference key.
	 * @param prefKey The preference key.
	 */
	public MRUList(String prefKey) {
		this.prefKey = prefKey;
		initData();
	}

	/**
	 * Initialize the list using its preference key.
	 */
	private void initData() {
		IPreferenceStore prefStore = UIPlugin.getDefault().getPreferenceStore();
		String mruList = prefStore.getString(prefKey);
		if (mruList != null) {
			StringTokenizer st = new StringTokenizer(mruList, "|"); //$NON-NLS-1$
			while (st.hasMoreTokens()) {
				add(st.nextToken());
			}
		}
	}

	/**
	 * Update the MRU list preference with the specified delta set.
	 * 
	 * @param deltaList The delta list to update the MRU preference with.
	 */
	public void updateMRUList(List<String> deltaList) {
		for (String id : deltaList) {
			if (!contains(id)) add(id);
		}
		IPreferenceStore prefStore = UIPlugin.getDefault().getPreferenceStore();
		int max = prefStore.getInt(prefKey+".max"); //$NON-NLS-1$
		if(max == 0) max = IPreferenceConsts.DEFAULT_MAX_MRU;
		int length = size();
		List<String> newList = this;
		if(length>max) {
			newList = subList(length-max, length);
		}
		StringBuilder builder = new StringBuilder();
		for(int i=0;i<newList.size();i++) {
			String id = newList.get(i);
			if(i>0) builder.append("|"); //$NON-NLS-1$
			builder.append(id);
		}
		prefStore.setValue(prefKey, builder.toString());
    }
}
