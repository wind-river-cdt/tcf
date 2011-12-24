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
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessModelManager;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;

public class ConfigIntervalDynamicContribution extends CompoundContributionItem {

	class MRUAction extends Action {
		private ProcessModel model;
		private int seconds;
		public MRUAction(ProcessModel aModel, int s) {
			super("" + s + " S", AS_RADIO_BUTTON);  //$NON-NLS-1$//$NON-NLS-2$
			this.seconds = s;
			this.model = aModel;
			if(aModel.getInterval() == seconds) {
				setChecked(true);
			}
		}
		@Override
        public void run() {
			if (isChecked()) {
				this.model.setInterval(seconds);
			}
        }
	}

	class GradeAction extends Action {
		private ProcessModel model;
		private int seconds;
		public GradeAction(ProcessModel aModel, String name, int s) {
			super(name, AS_RADIO_BUTTON);
			this.model = aModel;
			this.seconds = s;
			if(aModel.getInterval() == seconds) {
				setChecked(true);
			}
		}
		@Override
        public void run() {
			if (isChecked()) {
				this.model.setInterval(seconds);
				model.updateMRU(seconds);
			}
        }
	}

	@Override
	protected IContributionItem[] getContributionItems() {
		IEditorInput editorInput = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput();
		IPeerModel peer = (IPeerModel) editorInput.getAdapter(IPeerModel.class);
		List<IContributionItem> items = new ArrayList<IContributionItem>();
		if (peer != null) {
			ProcessModel pModel = ProcessModelManager.getInstance().getProcessModel(peer);
			IPreferenceStore prefStore = UIPlugin.getDefault().getPreferenceStore();
			String mruList = prefStore.getString(IPreferenceConsts.PREF_INTERVAL_MRU_LIST);
			if (mruList != null) {
				StringTokenizer st = new StringTokenizer(mruList, ":"); //$NON-NLS-1$
				int maxCount = prefStore.getInt(IPreferenceConsts.PREF_INTERVAL_MRU_COUNT);
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
						items.add(new ActionContributionItem(new MRUAction(pModel, seconds)));
					}
					items.add(new Separator("MRU")); //$NON-NLS-1$
				}
			}
			String grades = prefStore.getString(IPreferenceConsts.PREF_INTERVAL_GRADES);
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
						items.add(new ActionContributionItem(new GradeAction(pModel, name, seconds)));
					}
				}
				catch (NumberFormatException nfe) {
				}
			}
		}
		return items.toArray(new IContributionItem[items.size()]);
	}
}
