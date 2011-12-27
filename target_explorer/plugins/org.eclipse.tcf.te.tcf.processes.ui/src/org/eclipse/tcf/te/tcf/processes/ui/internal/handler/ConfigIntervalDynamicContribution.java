/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.internal.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.processes.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.processes.ui.internal.preferences.IPreferenceConsts;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessModel;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;

/**
 * The dynamic contribution class to create a drop down menu of interval configuration.
 */
public class ConfigIntervalDynamicContribution extends CompoundContributionItem implements IPreferenceConsts {

	/**
	 * The action to allow a most recently used interval to be selected.
	 */
	class MRUAction extends Action {
		// The process model.
		private ProcessModel model;
		// The interval of this most recently used item.
		private int seconds;
		/**
		 * Constructor
		 * 
		 * @param model The process model.
		 * @param seconds The interval time.
		 */
		public MRUAction(ProcessModel model, int seconds) {
			super("" + seconds + " S", AS_RADIO_BUTTON);  //$NON-NLS-1$//$NON-NLS-2$
			this.seconds = seconds;
			this.model = model;
			if(model.getInterval() == seconds) {
				setChecked(true);
			}
		}
		
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.action.Action#run()
		 */
		@Override
        public void run() {
			if (isChecked()) {
				this.model.setInterval(seconds);
			}
        }
	}

	/**
	 * The action to allow a speed grade to be selected.
	 */
	class GradeAction extends Action {
		// The process model.
		private ProcessModel model;
		// The interval time represented by this grade.
		private int seconds;

		/**
		 * Constructor
		 * 
		 * @param model The process model.
		 * @param name The grade name.
		 * @param seconds The interval time.
		 */
		public GradeAction(ProcessModel model, String name, int seconds) {
			super(name+" ("+seconds+" s)", AS_RADIO_BUTTON);  //$NON-NLS-1$//$NON-NLS-2$
			this.model = model;
			this.seconds = seconds;
			if(model.getInterval() == seconds) {
				setChecked(true);
			}
		}
		
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.action.Action#run()
		 */
		@Override
        public void run() {
			if (isChecked()) {
				this.model.setInterval(seconds);
				model.addMRUInterval(seconds);
			}
        }
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.actions.CompoundContributionItem#getContributionItems()
	 */
	@Override
	protected IContributionItem[] getContributionItems() {
		IEditorInput editorInput = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput();
		IPeerModel peerModel = (IPeerModel) editorInput.getAdapter(IPeerModel.class);
		List<IContributionItem> items = new ArrayList<IContributionItem>();
		if (peerModel != null) {
			ProcessModel model = ProcessModel.getProcessModel(peerModel);
			List<IContributionItem> groupItems = createGradeActions(model);
			if(!groupItems.isEmpty()) {
				items.addAll(groupItems);
			}
			groupItems = createMRUActions(model);
			if(!groupItems.isEmpty()) {
	    		items.add(new Separator("MRU")); //$NON-NLS-1$
				items.addAll(groupItems);
			}
		}
		return items.toArray(new IContributionItem[items.size()]);
	}
	
	/**
	 * Create and return the speed grade actions.
	 * 
	 * @param model The current process model.
	 * @return The grade action list.
	 */
	private List<IContributionItem> createGradeActions(ProcessModel model) {
		List<IContributionItem> items = new ArrayList<IContributionItem>();
		IPreferenceStore prefStore = UIPlugin.getDefault().getPreferenceStore();
	    String grades = prefStore.getString(PREF_INTERVAL_GRADES);
	    Assert.isNotNull(grades);
	    StringTokenizer st = new StringTokenizer(grades, "|"); //$NON-NLS-1$
	    while(st.hasMoreTokens()) {
	    	String token = st.nextToken();
	    	StringTokenizer st2 = new StringTokenizer(token, ":"); //$NON-NLS-1$
	    	String name = st2.nextToken();
	    	String value = st2.nextToken();
	    	try{
	    		int seconds = Integer.parseInt(value);
	    		if(seconds > 0) {
	    			items.add(new ActionContributionItem(new GradeAction(model, name, seconds)));
	    		}
	    	}
	    	catch (NumberFormatException nfe) {
	    	}
	    }
	    return items;
    }

	/**
	 * Create and return the most recently used actions.
	 * 
	 * @param model The current process model.
	 * @return The MRU action list.
	 */
	private List<IContributionItem> createMRUActions(ProcessModel model) {
		List<IContributionItem> items = new ArrayList<IContributionItem>();
		IPreferenceStore prefStore = UIPlugin.getDefault().getPreferenceStore();
	    String mruList = prefStore.getString(PREF_INTERVAL_MRU_LIST);
	    if (mruList != null) {
	    	StringTokenizer st = new StringTokenizer(mruList, ":"); //$NON-NLS-1$
	    	int maxCount = prefStore.getInt(PREF_INTERVAL_MRU_COUNT);
	    	int count = 0;
	    	List<Integer> mru = new ArrayList<Integer>();
	    	while (st.hasMoreTokens()) {
	    		String token = st.nextToken();
	    		try {
	    			int seconds = Integer.parseInt(token);
	    			if (seconds > 0) {
	    				mru.add(Integer.valueOf(seconds));
	    				count ++;
	    				if (count >= maxCount) break;
	    			}
	    		}
	    		catch (NumberFormatException nfe) {
	    		}
	    	}
	    	if(count > 0) {
	    		for(int seconds : mru) {
	    			items.add(new ActionContributionItem(new MRUAction(model, seconds)));
	    		}
	    	}
	    }
	    return items;
    }
}
