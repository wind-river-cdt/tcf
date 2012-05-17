package org.eclipse.tcf.te.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.tcf.te.ui.dialogs.QuickFilter;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.handlers.HandlerUtil;

public class QuickFilterHanlder extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchSite site = HandlerUtil.getActiveSiteChecked(event);
		ISelectionProvider provider = site.getSelectionProvider();
		if (provider instanceof TreeViewer) {
			TreeViewer viewer = (TreeViewer) provider;
			Object root = getFilterRoot(event);
			if (root == null) root = viewer.getInput();
			QuickFilter.getQuickFilter(viewer).showFilter(root);
		}
		return null;
	}

	private TreePath getFilterRoot(ExecutionEvent event) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof TreeSelection) {
			TreeSelection treeSelection = (TreeSelection) selection;
			TreePath[] paths = treeSelection.getPaths();
			if (paths != null && paths.length > 0) {
				return paths[0];
			}
		}
		return null;
	}
}
