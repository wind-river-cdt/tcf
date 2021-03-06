/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.views.scriptpad.actions;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.tcf.te.tcf.ui.views.scriptpad.ScriptPad;

/**
 * Script Pad copy action handler implementation.
 */
public class CopyAction extends Action {
	// Reference to the parent view
	private final ScriptPad view;

	/**
     * Constructor.
     *
     * @param view The parent Script Pad view. Must not be <code>null</code>.
     */
    public CopyAction(ScriptPad view) {
    	super();

    	Assert.isNotNull(view);
    	this.view = view;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
    	if (view.getStyledText() == null || view.getStyledText().isDisposed()) return;
        view.getStyledText().copy();
        view.updateActionEnablements();
    }

    /**
     * Updates the actions enabled state.
     */
    public void updateEnabledState() {
        setEnabled(view.getStyledText() != null && view.getStyledText().getSelectionCount() > 0);
    }
}
