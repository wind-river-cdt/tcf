/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.internal.editor;

import java.util.EventObject;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.te.runtime.events.ChangeEvent;
import org.eclipse.tcf.te.runtime.events.EventManager;
import org.eclipse.tcf.te.runtime.interfaces.IDisposable;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.ui.events.AbstractEventListener;
import org.eclipse.ui.forms.editor.IFormPage;

/**
 * Editor event listener implementation.
 * <p>
 * The event listener is registered by an editor instance for a given editor input
 * and is supposed to receive events for the editor input only.
 */
public class EditorEventListener extends AbstractEventListener implements IDisposable {
	// Reference to the parent editor
	private final Editor editor;

	/**
     * Constructor.
     *
     * @param editor The parent editor. Must not be <code>null</code>.
     */
    public EditorEventListener(Editor editor) {
    	super();

    	Assert.isNotNull(editor);
    	this.editor = editor;

    	// Register the event listener if the editor input is a properties container
		Object node = editor.getEditorInput() != null ? editor.getEditorInput().getAdapter(Object.class) : null;
		if (node instanceof IPropertiesContainer) EventManager.getInstance().addEventListener(this, ChangeEvent.class, node);
    }

    /* (non-Javadoc)
     * @see org.eclipse.tcf.te.runtime.interfaces.IDisposable#dispose()
     */
    @Override
    public void dispose() {
    	EventManager.getInstance().removeEventListener(this);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.events.IEventListener#eventFired(java.util.EventObject)
	 */
	@Override
	public void eventFired(EventObject event) {
		// Get the event source
		Object source = event.getSource();
		// Double check with the parent editors input object
		Object node = editor.getEditorInput() != null ? editor.getEditorInput().getAdapter(Object.class) : null;
		// If the editor input cannot be determined or it does not match the event source
		// --> return immediately
		if (node == null || !node.equals(source)) return;

		// Update the active page content by calling IFormPage#setActive(boolean)
		Object page = editor.getSelectedPage();
		if (page instanceof IFormPage) {
			((IFormPage)page).setActive(((IFormPage)page).isActive());
		}

		// Update the editor part name
		editor.updatePartName();
	}
}
