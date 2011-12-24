package org.eclipse.tcf.te.tcf.processes.ui.internal.dialogs;

import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class IntervalConfigDialog extends StatusDialog {

	public IntervalConfigDialog(Shell parent) {
	    super(parent);
    }

	@Override
    protected Control createDialogArea(Composite parent) {
	    Composite composite = (Composite) super.createDialogArea(parent);
	    
	    return composite;
    }

}
