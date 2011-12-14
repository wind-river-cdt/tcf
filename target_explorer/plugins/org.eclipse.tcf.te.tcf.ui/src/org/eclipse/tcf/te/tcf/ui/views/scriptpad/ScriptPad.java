/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.views.scriptpad;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.part.ViewPart;

/**
 * Script Pad view implementation.
 */
public class ScriptPad extends ViewPart implements ISelectionProvider, SelectionListener {
	// Reference to the Text widget used by the script pad
	private StyledText text;

	// The list of registered selection changed listeners
	private final List<ISelectionChangedListener> listeners = new ArrayList<ISelectionChangedListener>();

	// Reference to the selected peer model
	private IPeerModel peerModel;

	/**
     * Constructor.
     */
    public ScriptPad() {
    	super();
    	listeners.clear();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
    	if (text != null && !text.isDisposed()) {
    		text.removeSelectionListener(this);
    	}
        listeners.clear();
        super.dispose();
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		// Create the StyledText widget
		text = new StyledText(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		text.setFont(JFaceResources.getTextFont());
		text.addSelectionListener(this);

		// Register ourselves as selection provider
		getViewSite().setSelectionProvider(this);

		// Create the context menu
		createContextMenu();
		// Create the toolbar
		createToolbar();

		// Update the action bars
		getViewSite().getActionBars().updateActionBars();
	}

	/**
	 * Creates the views context menu and register it to enable contributions.
	 */
	private void createContextMenu() {
		// Create menu manager.
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
            public void menuAboutToShow(IMenuManager mgr) {
				fillContextMenu(mgr);
			}
		});

		// Create menu.
		Menu menu = menuMgr.createContextMenu(text);
		text.setMenu(menu);

		// Register menu for extension.
		getSite().registerContextMenu(menuMgr, this);
	}

	/**
	 * Fill the context menu.
	 *
	 * @param manager The menu manager. Must not be <code>null</code>.
	 */
	protected void fillContextMenu(IMenuManager manager) {
		Assert.isNotNull(manager);
	}

	/**
	 * Creates the views toolbar.
	 */
	private void createToolbar() {
		IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();
		manager.add(new Separator("peers")); //$NON-NLS-1$
		manager.add(new Separator("play")); //$NON-NLS-1$
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		if (text != null) text.setFocus();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	@Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
		Assert.isNotNull(listener);
		if (!listeners.contains(listener)) listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	@Override
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		Assert.isNotNull(listener);
		listeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	@Override
	public ISelection getSelection() {
		String selected = text.getSelectionText();
	    return selected != null ? new StructuredSelection(selected) : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void setSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			Object element = ((IStructuredSelection)selection).getFirstElement();
			if (element instanceof String) {
				int start = text.getText().indexOf((String)element);
				if (start != -1) {
					text.setSelection(start, start + ((String)element).length());
				}
			}
		} else {
			text.setSelection(text.getCaretOffset(), text.getCaretOffset());
		}

		// Fire the selection changed event
		fireSelectionChanged();
	}

	/**
	 * Notify the registered selection changed listener about a changed selection.
	 */
	private void fireSelectionChanged() {
		if (getSelection() == null) return;

		SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());
		Iterator<ISelectionChangedListener> iterator = listeners.iterator();
		while (iterator.hasNext()) {
			ISelectionChangedListener listener = iterator.next();
			listener.selectionChanged(event);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
		fireSelectionChanged();
	}

	/**
	 * Sets the peer model.
	 *
	 * @param peerModel The peer model or <code>null</code>.
	 */
	public void setPeerModel(IPeerModel peerModel) {
		this.peerModel = peerModel;
		// Update the action bars
		getViewSite().getActionBars().updateActionBars();
		// Fire a property change
		firePropertyChange(IWorkbenchPartConstants.PROP_INPUT);
	}

	/**
	 * Returns the associated peer model.
	 *
	 * @return The associated peer model.
	 */
	public IPeerModel getPeerModel() {
		return peerModel;
	}
}
