/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.controls;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.te.tcf.filesystem.core.model.ITreeNodeModel;
import org.eclipse.tcf.te.tcf.filesystem.ui.controls.NavigatorContentProvider;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.processes.core.model.ProcessModel;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.navigator.NavigatorFilterService;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonContentProvider;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.INavigatorFilterService;


/**
 * Processes content provider for the common navigator of Target Explorer.
 */
@SuppressWarnings("restriction")
public class ProcessNavigatorContentProvider  extends NavigatorContentProvider implements ICommonContentProvider {
	// The "Single Thread" filter id
	private final static String SINGLE_THREAD_FILTER_ID = "org.eclipse.tcf.te.tcf.processes.ui.navigator.filter.singleThread"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.ui.controls.NavigatorContentProvider#doGetModel(org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel)
	 */
	@Override
    protected ITreeNodeModel doGetModel(IPeerModel peerNode) {
		return ProcessModel.getProcessModel(peerNode);
	}

	/* (non-Javadoc)
     * @see org.eclipse.ui.navigator.ICommonContentProvider#init(org.eclipse.ui.navigator.ICommonContentExtensionSite)
     */
    @Override
    public void init(ICommonContentExtensionSite config) {
    	Assert.isNotNull(config);

    	// Make sure that the hidden "Single Thread" filter is active
    	INavigatorContentService cs = config.getService();
    	INavigatorFilterService fs = cs != null ? cs.getFilterService() : null;
		if (fs != null && !fs.isActive(SINGLE_THREAD_FILTER_ID)) {
			if (fs instanceof NavigatorFilterService) {
				final NavigatorFilterService navFilterService = (NavigatorFilterService)fs;
				navFilterService.addActiveFilterIds(new String[] { SINGLE_THREAD_FILTER_ID });
				Display display = PlatformUI.getWorkbench().getDisplay();
				display.asyncExec(new Runnable(){
					@Override
                    public void run() {
						navFilterService.updateViewer();
                    }});
			}
		}
    }

	/* (non-Javadoc)
     * @see org.eclipse.ui.navigator.IMementoAware#restoreState(org.eclipse.ui.IMemento)
     */
    @Override
    public void restoreState(IMemento aMemento) {
    }

	/* (non-Javadoc)
     * @see org.eclipse.ui.navigator.IMementoAware#saveState(org.eclipse.ui.IMemento)
     */
    @Override
    public void saveState(IMemento aMemento) {
    }
}
