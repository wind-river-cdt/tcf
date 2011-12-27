package org.eclipse.tcf.te.tcf.filesystem.internal.tabbed;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

public class BasicFolderSection extends AbstractPropertySection {
	private FSTreeNode folder;
	private Label labelText;

	public BasicFolderSection() {
		System.out.println();
	}

	@Override
	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		Composite composite = getWidgetFactory().createFlatFormComposite(parent);
		FormData data;

		labelText = getWidgetFactory().createLabel(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		labelText.setLayoutData(data);
	}
	
	 @Override
    public void setInput(IWorkbenchPart part, ISelection selection) {
         super.setInput(part, selection);
         Assert.isTrue(selection instanceof IStructuredSelection);
         Object input = ((IStructuredSelection) selection).getFirstElement();
         Assert.isTrue(input instanceof FSTreeNode);
         this.folder = (FSTreeNode) input;
     }
	 
	 @Override
    public void refresh() {
         labelText.setText(folder.name);
     }	 
}
