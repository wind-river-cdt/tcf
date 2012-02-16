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
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.tcf.te.launch.ui.nls.Messages;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.ui.jface.interfaces.IValidatingContainer;

/**
 * Remote context selector control implementation.
 * <p>
 * Default properties:
 * <ul>
 * 	<li>PROPERTY_SHOW_GHOST_MODEL_NODES = false</li>
 * 	<li>PROPERTY_MULTI_CONTEXT_SELECTOR = true</li>
 * </ul>
 */
public class RemoteContextSelectorControl extends ContextSelectorControl {
	/**
	 * Property: If set to <code>true</code>, the control will be created as multi
	 *           context control. That means that more than one tree item will be
	 *           checkmarkable. In single context selector mode, only one tree item
	 *           can be checkmarked at the same time.
	 */
	public static final String PROPERTY_MULTI_CONTEXT_SELECTOR = "multiContextSelector"; //$NON-NLS-1$

	// The last failure cause
	private Throwable lastFailureCause;
	// Flag for controlling if at least one element has to be selected
	private boolean requireSelection = true;

	/**
     * Constructor.
	 *
	 * @param parentPage The parent target connection page this control is embedded in. Might be
	 *            		 <code>null</code> if the control is not associated with a page.
     */
    public RemoteContextSelectorControl(IDialogPage parentPage) {
	    super(parentPage);
    }

    /* (non-Javadoc)
     * @see org.eclipse.tcf.te.launch.ui.tabs.selector.ContextSelectorControl#initializeProperties(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer)
     */
	@Override
	protected void initializeProperties(IPropertiesContainer properties) {
		super.initializeProperties(properties);
		properties.setProperty(PROPERTY_MULTI_CONTEXT_SELECTOR, true);
	}

	/**
	 * Set the last failure cause to display.
	 *
	 * @param cause The last failure case or <code>null</code>.
	 */
	public final void setLastFailureCause(Throwable cause) {
		lastFailureCause = cause;
		if (getParentPage() instanceof IValidatingContainer) {
			((IValidatingContainer)getParentPage()).validate();
		}
	}

	/**
	 * Returns the last failure cause to display.
	 *
	 * @return The last failure cause or <code>null</code>.
	 */
	public final Throwable getLastFailureCause() {
		return lastFailureCause;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.tabs.selector.ContextSelectorControl#createTreeViewerControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected TreeViewer createTreeViewerControl(Composite parent) {
		TreeViewer viewer = super.createTreeViewerControl(parent);
		if (viewer != null) {
			viewer.expandAll();
		}
		return viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.tabs.selector.ContextSelectorControl#getTreeViewerStyle()
	 */
	@Override
	protected int getTreeViewerStyle() {
		// For the remote context selector we do want to have checkboxes in front
		// of the tree items and allow for multi-selection.
		return  super.getTreeViewerStyle() & ~SWT.SINGLE | SWT.CHECK | SWT.MULTI;
	}

	/* (non-Javadoc)
	 * @see com.windriver.ide.target.ui.wizard.controls.elementselector.WRModelContextSelectorControl#onModelNodeCheckStateChanged(com.windriver.ide.target.api.model.IModelNode, boolean)
	 */
	@Override
	protected void onModelNodeCheckStateChanged(IModelNode node, boolean checked) {
		// In case the control is operating in single context selector mode,
		// we have to uncheck any other element than the given checked one.
		if (checked && getPropertiesContainer().isProperty(PROPERTY_MULTI_CONTEXT_SELECTOR, false)) {
			if (getViewer() instanceof ContextSelectorTreeViewer) {
				// Node: Within here, only methods which do not fire the check state listeners
				//       again must be used!
				ContextSelectorTreeViewer viewer = (ContextSelectorTreeViewer)getViewer();

				// If the checked node is a container node and has children, select
				// the first children of the container.
				Item[] childItems = viewer.getChildren(node);
				if (childItems != null && childItems.length > 1) {
					// Take the first item as element to be checked
					viewer.setCheckedElements(new Object[] { childItems[0].getData() });
				} else {
					// Set the passed in element node checked
					viewer.setCheckedElements(new Object[] { node });
				}
			}
		}

		// Trigger page validation after adjusting the checked state.
		super.onModelNodeCheckStateChanged(node, checked);
	}

	/* (non-Javadoc)
	 * @see com.windriver.ide.target.ui.wizard.controls.WRBaseTargetConnectionPageControl#isValid()
	 */
	@Override
	public boolean isValid() {
		boolean valid = super.isValid();

		// If there is a last failure cause set, show that failure cause
		valid = getLastFailureCause() == null;
		if (!valid) {
			setMessage(getLastFailureCause().getLocalizedMessage(), IMessageProvider.ERROR);
		}

		// The remote context selector control is only valid, if at least one
		// element has been checked (if operating with CHECK style set)
		if (valid && (getTreeViewerStyle() & SWT.CHECK) != 0 && requireSelection) {
			valid = getCheckedModelContexts().length > 0;

			// if we are not valid here, it can only mean, that there is
			// no connectable checked.
			if (!valid) {
				String messageId = "RemoteContextSelectorControl_error_noContextSelected"; //$NON-NLS-1$
				if (getPropertiesContainer().isProperty(PROPERTY_MULTI_CONTEXT_SELECTOR, true)) {
					messageId += "_multi"; //$NON-NLS-1$
				}
				else {
					messageId += "_single"; //$NON-NLS-1$
				}

				setMessage(getMessageForId(messageId), getMessageTypeForId(messageId, IMessageProvider.ERROR));
			}
		}

		return valid;
	}

	/**
	 * Returns the message text for the given message id. Subclass in case different
	 * message text should be used for standard messages.
	 *
	 * @param messageId The message id. Must be not <code>null</code>.
	 * @return The message text.
	 */
	protected String getMessageForId(String messageId) {
		Assert.isNotNull(messageId);
		return Messages.getString(messageId);
	}

	/**
	 * Returns the message type for the given message id. Subclass in case different
	 * message types should by used for standard messages. The default implementation
	 * returns the proposed message type unchanged.
	 *
	 * @param messageId The message id. Must be not <code>null</code>.
	 * @param proposed The proposed message type.
	 * @return The message type for the given message id.
	 */
	protected int getMessageTypeForId(String messageId, int proposed) {
		Assert.isNotNull(messageId);
		return proposed;
	}

	/**
	 * Configures whether a selection is required or not.
	 */
	public void setRequireSelection(boolean value) {
		requireSelection = value;
	}
}
