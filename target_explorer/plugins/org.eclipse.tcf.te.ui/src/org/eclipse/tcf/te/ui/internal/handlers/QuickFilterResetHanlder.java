package org.eclipse.tcf.te.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.tcf.te.ui.dialogs.QuickFilter;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.handlers.HandlerUtil;

public class QuickFilterResetHanlder extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchSite site = HandlerUtil.getActiveSiteChecked(event);
		ISelectionProvider provider = site.getSelectionProvider();
		if (provider instanceof TreeViewer) {
			TreeViewer viewer = (TreeViewer) provider;
			QuickFilter.getQuickFilter(viewer).resetViewer();
		}
		return null;
	}

}
