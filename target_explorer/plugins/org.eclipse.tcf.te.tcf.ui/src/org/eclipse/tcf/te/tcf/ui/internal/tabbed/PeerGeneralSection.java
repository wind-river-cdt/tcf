/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.internal.tabbed;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.ui.forms.CustomFormToolkit;
import org.eclipse.tcf.te.ui.tables.TableViewerComparator;
import org.eclipse.tcf.te.ui.tables.properties.NodePropertiesTableControl;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
/**
 * The property section to display the general properties of a peer.
 */
public class PeerGeneralSection extends AbstractPropertySection {
	// The peer to be displayed.
	private IPeerModel peer;
	// The table control to display the properties.
	private NodePropertiesTableControl tableControl;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#createControls(org.eclipse.swt.widgets.Composite, org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	@Override
    public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
	    super.createControls(parent, aTabbedPropertySheetPage);
	    Composite composite = getWidgetFactory().createFlatFormComposite(parent);
	    composite.setLayout(new GridLayout());
	    tableControl = new NodePropertiesTableControl(this.getPart()) {
			/* (non-Javadoc)
			 * @see org.eclipse.tcf.te.tcf.vtl.ui.datasource.controls.tables.NodePropertiesTableControl#doCreateTableViewerContentProvider(org.eclipse.jface.viewers.TableViewer)
			 */
			@Override
			protected IStructuredContentProvider doCreateTableViewerContentProvider(TableViewer viewer) {
				return new PeerGeneralSectionContentProvider(true);
			}
			/* (non-Javadoc)
			 * @see org.eclipse.tcf.te.tcf.vtl.ui.datasource.controls.tables.properties.NodePropertiesTableControl#doCreateTableViewerLabelProvider(org.eclipse.jface.viewers.TableViewer)
			 */
			@Override
			protected ITableLabelProvider doCreateTableViewerLabelProvider(TableViewer viewer) {
				return new PeerGeneralSectionLabelProvider(viewer);
			}
			/* (non-Javadoc)
			 * @see org.eclipse.tcf.te.tcf.vtl.ui.datasource.controls.tables.NodePropertiesTableControl#doCreateTableViewerComparator(org.eclipse.jface.viewers.TableViewer)
			 */
			@Override
			protected ViewerComparator doCreateTableViewerComparator(TableViewer viewer) {
				return new TableViewerComparator(viewer, (ITableLabelProvider)viewer.getLabelProvider());
			}
		};
		CustomFormToolkit toolkit = new CustomFormToolkit(new FormToolkit(parent.getDisplay()));
		tableControl.setupFormPanel(composite, toolkit);
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#setInput(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
    public void setInput(IWorkbenchPart part, ISelection selection) {
        super.setInput(part, selection);
        Assert.isTrue(selection instanceof IStructuredSelection);
        Object input = ((IStructuredSelection) selection).getFirstElement();
        Assert.isTrue(input instanceof IPeerModel);
        this.peer = (IPeerModel) input;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#refresh()
	 */
	@Override
    public void refresh() {
		if (tableControl != null) {
			tableControl.getViewer().setInput(peer);
		}
    }

}
