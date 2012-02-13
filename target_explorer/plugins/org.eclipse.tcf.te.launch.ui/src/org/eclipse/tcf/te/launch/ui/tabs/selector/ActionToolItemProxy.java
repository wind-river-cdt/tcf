/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.tabs.selector;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.tcf.te.launch.ui.activator.UIPlugin;
import org.eclipse.ui.IActionDelegate;

/**
 * Standard implementation of a proxy between a toolbar item widget and a JFace action.
 */
public class ActionToolItemProxy implements ISelectionChangedListener {
	private final ToolItem item;
	private final IAction action;

	// Local selection listener invoking the associated action.
	private final SelectionListener listener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			doRun(e);
		}
	};

	/**
	 * Constructor.
	 *
	 * @param item The toolbar item widget. Must not be <code>null</code>.
	 * @param action The JFace action. Must not be <code>null</code>.
	 */
	public ActionToolItemProxy(ToolItem item, IAction action) {
		Assert.isNotNull(item);
		Assert.isNotNull(action);

		this.item = item;
		this.action = action;

		initialize();
	}

	/**
	 * Dispose this proxy instance.
	 */
	public void dispose() {
		item.removeSelectionListener(listener);
	}

	/**
	 * Initialize the connection between the toolbar item widget and the associated JFace action.
	 */
	public void initialize() {
		item.setData(this);
		if (getText(action) != null) item.setText(getText(action));
		if (getToolTipText(action) != null) item.setToolTipText(getToolTipText(action));
		if (getImage(action) != null) item.setImage(getImage(action));
		if (getDisabledImage(action) != null) item.setDisabledImage(getDisabledImage(action));
		item.addSelectionListener(listener);
	}

	/**
	 * Returns the associated action.
	 *
	 * @return The action.
	 */
	public final IAction getAction() {
		return action;
	}

	/**
	 * Returns the text to apply to the toolbar item widget.
	 *
	 * @param action The JFace action. Must not be <code>null</code>.
	 * @return The text to apply to the toolbar item widget or <code>null</code>.
	 */
	protected String getText(IAction action) {
		Assert.isNotNull(action);
		return action.getText();
	}

	/**
	 * Returns the tooltip text to apply to the toolbar item widget.
	 *
	 * @param action The JFace action. Must not be <code>null</code>.
	 * @return The tooltip text to apply to the toolbar item widget or <code>null</code>.
	 */
	protected String getToolTipText(IAction action) {
		Assert.isNotNull(action);
		return action.getToolTipText();
	}

	/**
	 * Lookup the corresponding image for the given image descriptor.
	 *
	 * @param descriptor The image descriptor.
	 * @return The corresponding image or <code>null</code>.
	 */
	protected final Image getImageFromDescriptor(ImageDescriptor descriptor) {
		if (descriptor == null) return null;

		String key = descriptor.toString();
		Image image = UIPlugin.getImage(key);
		if (image == null) {
			image = descriptor.createImage();
			UIPlugin.getDefault().getImageRegistry().put(key, image);
		}
		return image;
	}

	/**
	 * Returns the image to apply to the toolbar item widget.
	 *
	 * @param action The JFace action. Must not be <code>null</code>.
	 * @return The image to apply to the toolbar item widget or <code>null</code>.
	 */
	protected Image getImage(IAction action) {
		Assert.isNotNull(action);
		return getImageFromDescriptor(action.getImageDescriptor());
	}

	/**
	 * Returns the disabled image to apply to the toolbar item widget.
	 *
	 * @param action The JFace action. Must not be <code>null</code>.
	 * @return The disabled image to apply to the toolbar item widget or <code>null</code>.
	 */
	protected Image getDisabledImage(IAction action) {
		Assert.isNotNull(action);
		return getImageFromDescriptor(action.getDisabledImageDescriptor());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
    public final void selectionChanged(SelectionChangedEvent event) {
		Assert.isNotNull(action);
		if (action instanceof IActionDelegate) {
			((IActionDelegate) action).selectionChanged(action, event.getSelection());
		}
		item.setEnabled(action.isEnabled());
	}

	/**
	 * Executes the operation once the user clicked on the toolbar item.
	 *
	 * @param e The selection event that triggered the invocation.
	 */
	protected void doRun(SelectionEvent e) {
		Assert.isNotNull(action);

		// Use a SWT event to signal the source of the invocation
		Event event = new Event();
		// Fill in the event data based on the passed in selection event
		event.display = e.display;
		event.widget = e.widget;
		event.detail = e.detail;
		event.doit = e.doit;
		event.item = e.item;
		event.text = e.text;
		event.time = e.time;
		event.stateMask = e.stateMask;
		event.height = e.height;
		event.width = e.width;
		event.x = e.x;
		event.y = e.y;
		// The event data field is reserved for application use. Fill in
		// here our custom data to pass to the associated action.
		event.data = getCustomEventDataObject();

		// And execute the associated action with the created event
		action.runWithEvent(event);
	}

	/**
	 * Returns the custom data object to associated with the event passed on to the associated
	 * action to execute. The interpretation of the custom data object is up to the associated
	 * action. Check the action documentation!
	 * <p>
	 * The default implementation does return always <code>null</code>.
	 *
	 * @return The custom data object or <code>null</code>.
	 */
	protected Object getCustomEventDataObject() {
		return null;
	}
}
