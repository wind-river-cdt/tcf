package org.eclipse.tcf.te.tcf.processes.ui.controls;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.navigator.CommonViewer;

public class CommonViewerListener implements IPropertyChangeListener {
	private CommonViewer viewer;
	public CommonViewerListener(CommonViewer viewer) {
		this.viewer = viewer;
	}

	@Override
    public void propertyChange(final PropertyChangeEvent event) {
		Tree tree = viewer.getTree();
		Display display = tree.getDisplay();
		if (display.getThread() == Thread.currentThread()) {
			viewer.refresh();
		}
		else {
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					propertyChange(event);
				}
			});
		}
    }
}
