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
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.processes.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.processes.ui.internal.preferences.IPreferenceConsts;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessModel;
import org.eclipse.tcf.te.tcf.processes.ui.nls.Messages;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.commands.ICommandService;

/**
 * The dynamic contribution class to create a drop down menu of interval configuration.
 */
public class ConfigIntervalDynamicContribution extends CompoundContributionItem implements IPreferenceConsts {
	private static final String CUSTOM_COMMAND_ID = "org.eclipse.tcf.te.tcf.processes.ui.command.refreshInterval"; //$NON-NLS-1$
	/**
	 * The action to allow a most recently used interval to be selected.
	 */
	static class MRUAction extends Action {
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
			super("" + seconds + " s", AS_RADIO_BUTTON);  //$NON-NLS-1$//$NON-NLS-2$
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
	static class GradeAction extends Action {
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
			super(name + (seconds > 0 ? " (" + seconds + " s)" : ""), AS_RADIO_BUTTON); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			this.model = model;
			this.seconds = seconds;
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


	/**
	 * The action to allow defining a custom interval.
	 */
	static class CustomAction extends Action {
		/**
		 * Constructor
		 *
		 * @param model The process model.
		 * @param name The grade name.
		 * @param seconds The interval time.
		 */
		public CustomAction() {
			super(Messages.ConfigIntervalDynamicContribution_Custom, AS_RADIO_BUTTON);
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.action.Action#run()
		 */
		@Override
		public void run() {
			if (isChecked()) {
				final IWorkbench workbench = PlatformUI.getWorkbench();
				ICommandService service = (ICommandService) workbench.getService(ICommandService.class);
				Command cmd = service != null ? service.getCommand(CUSTOM_COMMAND_ID) : null;
				if (cmd != null && cmd.isDefined() && cmd.isEnabled()) {
					final Command command = cmd;
					SafeRunner.run(new SafeRunnable() {
						@Override
	                    public void handleException(Throwable e) {
							// Ignore exception
	                    }
						@Override
						public void run() throws Exception {
							EvaluationContext ctx = new EvaluationContext(null, StructuredSelection.EMPTY);
							IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
							Shell shell = window.getShell();
							ctx.addVariable(ISources.ACTIVE_SHELL_NAME, shell);
							IEditorInput editorInput = window.getActivePage().getActiveEditor().getEditorInput();
							ctx.addVariable(ISources.ACTIVE_EDITOR_INPUT_NAME, editorInput);
							ctx.setAllowPluginActivation(true);
							ExecutionEvent executionEvent = new ExecutionEvent(command, Collections.EMPTY_MAP, CustomAction.this, ctx);
							command.executeWithChecks(executionEvent);
						}
					});
				}
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
	    int current = model.getInterval();
	    boolean custom = true;
	    while(st.hasMoreTokens()) {
	    	String token = st.nextToken();
	    	StringTokenizer st2 = new StringTokenizer(token, ":"); //$NON-NLS-1$
	    	String name = st2.nextToken();
	    	String value = st2.nextToken();
	    	try{
	    		int seconds = Integer.parseInt(value);
	    		GradeAction action = new GradeAction(model, name, seconds);
	    		boolean checked = current == seconds;
				action.setChecked(checked);
				custom &= !checked;
    			items.add(new ActionContributionItem(action));
	    	}
	    	catch (NumberFormatException nfe) {
	    	}
	    }
	    CustomAction action = new CustomAction();
	    action.setChecked(custom);
	    items.add(new ActionContributionItem(action));
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
					mru.add(Integer.valueOf(seconds));
					count++;
					if (count >= maxCount) break;
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
