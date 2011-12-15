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
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.tcf.te.tcf.ui.views.scriptpad.ScriptPad;

/**
 * Script Pad paste action handler implementation.
 */
public class PasteAction extends Action {
	// Reference to the parent view
	private final ScriptPad view;

	/**
     * Constructor.
     *
     * @param view The parent Script Pad view. Must not be <code>null</code>.
     */
    public PasteAction(ScriptPad view) {
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
    	view.getStyledText().paste();
        view.updateActionEnablements();
    }

    /**
     * Updates the actions enabled state.
     */
    public void updateEnabledState() {
		boolean canPaste = false;
    	if (view.getStyledText() != null && view.getStyledText().getEditable()) {
    		Clipboard clipboard = new Clipboard(view.getStyledText().getDisplay());
    		TransferData[] td = clipboard.getAvailableTypes();
    		for (int i = 0; i < td.length; ++i) {
    			if (TextTransfer.getInstance().isSupportedType(td[i])) {
    				canPaste = true;
    				break;
    			}
    		}

    		clipboard.dispose();
    	}

        setEnabled(canPaste);
    }
}
