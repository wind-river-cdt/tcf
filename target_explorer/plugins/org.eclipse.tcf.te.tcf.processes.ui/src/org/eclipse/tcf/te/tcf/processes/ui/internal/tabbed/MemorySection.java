/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.internal.tabbed;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tcf.services.ISysMonitor;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProvider;
import org.eclipse.tcf.te.tcf.processes.core.model.ProcessTreeNode;
import org.eclipse.tcf.te.tcf.processes.ui.nls.Messages;
import org.eclipse.tcf.te.tcf.ui.tabbed.BaseTitledSection;
import org.eclipse.tcf.te.ui.swt.SWTControlUtil;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * The property section to display the memory usage of a process.
 */
public class MemorySection extends BaseTitledSection {
	// The context of the process selected.
	protected ISysMonitor.SysMonitorContext context;
	// The text field for the virtual memory size in bytes.
	protected Text vsizeText;
	// The text field for the virtual memory pages.
	protected Text psizeText;
	// The number of memory pages in process resident set.
	protected Text rssText;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection#createControls(org.eclipse.swt.widgets.Composite, org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	@Override
	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		vsizeText = createTextField(null, Messages.MemorySection_VSize);
		psizeText = createTextField(vsizeText, Messages.MemorySection_PSize);
		rssText = createTextField(psizeText, Messages.MemorySection_RSS);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection#getText()
	 */
	@Override
	protected String getText() {
		return Messages.MemorySection_Title;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.tabbed.BaseTitledSection#updateData(org.eclipse.tcf.te.ui.interfaces.IPropertyChangeProvider)
	 */
	@Override
    protected void updateInput(IPeerModelProvider input) {
		Assert.isTrue(input instanceof ProcessTreeNode);
		ProcessTreeNode node = (ProcessTreeNode) input;
		context = node.context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#refresh()
	 */
	@Override
	public void refresh() {
		SWTControlUtil.setText(vsizeText, context == null ? "" : "" + context.getVSize()); //$NON-NLS-1$ //$NON-NLS-2$
		SWTControlUtil.setText(psizeText, context == null ? "" : "" + context.getPSize()); //$NON-NLS-1$ //$NON-NLS-2$
		SWTControlUtil.setText(rssText, context == null ? "" : "" + context.getRSS()); //$NON-NLS-1$ //$NON-NLS-2$
		super.refresh();
	}
}
