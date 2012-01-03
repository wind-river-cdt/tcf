/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The handler to show the selected object in Properties view.
 */
public class ShowInPropertiesHandler extends AbstractHandler {

	// The id of Properties view.
	private static final String PROP_VIEW_ID = "org.eclipse.ui.views.PropertySheet"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow workbench = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		final IWorkbenchPage activePage = workbench.getActivePage();
		SafeRunner.run(new SafeRunnable(){
			@Override
            public void run() throws Exception {
		        activePage.showView(PROP_VIEW_ID);
            }});
		return null;
	}
}
