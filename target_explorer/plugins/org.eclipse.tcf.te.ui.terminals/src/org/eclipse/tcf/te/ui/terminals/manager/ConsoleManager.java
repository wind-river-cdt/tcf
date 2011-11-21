/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.te.ui.terminals.activator.UIPlugin;
import org.eclipse.tcf.te.ui.terminals.interfaces.IPreferenceKeys;
import org.eclipse.tcf.te.ui.terminals.interfaces.ITerminalsView;
import org.eclipse.tcf.te.ui.terminals.interfaces.IUIConstants;
import org.eclipse.tcf.te.ui.terminals.tabs.TabFolderManager;
import org.eclipse.tcf.te.ui.terminals.view.TerminalsView;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;

/**
 * Terminals console manager.
 */
@SuppressWarnings("restriction")
public class ConsoleManager {
	private int secondaryIdCount;
	private boolean secondaryIdCountInit;

	// Reference to the perspective listener instance
	private final IPerspectiveListener perspectiveListener;

	// Internal perspective listener implementation
	static class ConsoleManagerPerspectiveListener extends PerspectiveAdapter {
		private final List<IViewReference> references = new ArrayList<IViewReference>();

		/* (non-Javadoc)
		 * @see org.eclipse.ui.PerspectiveAdapter#perspectiveActivated(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
		 */
		@Override
		public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
			// If the old references list is empty, just return
			if (references.isEmpty()) return;
			// Create a copy of the old view references list
			List<IViewReference> oldReferences = new ArrayList<IViewReference>(references);

			// Get the current list of view references
			List<IViewReference> references = new ArrayList<IViewReference>(Arrays.asList(page.getViewReferences()));
			for (IViewReference reference : oldReferences) {
				if (references.contains(reference)) continue;
				// Previous visible terminal console view reference, make visible again
				try {
					page.showView(reference.getId(), reference.getSecondaryId(), IWorkbenchPage.VIEW_VISIBLE);
				} catch (PartInitException e) { /* Failure on part instantiation is ignored */ }
			}

		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.PerspectiveAdapter#perspectivePreDeactivate(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
		 */
		@Override
		public void perspectivePreDeactivate(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
			references.clear();
			for (IViewReference reference : page.getViewReferences()) {
				IViewPart part = reference.getView(false);
				if (part instanceof TerminalsView && !references.contains(reference)) {
					references.add(reference);
				}
			}
		}
	}

	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstanceHolder {
		public static ConsoleManager fInstance = new ConsoleManager();
	}

	/**
	 * Returns the singleton instance for the console manager.
	 */
	public static ConsoleManager getInstance() {
		return LazyInstanceHolder.fInstance;
	}

	/**
	 * Constructor.
	 */
	ConsoleManager() {
		super();

		// Attach the perspective listener
		perspectiveListener = new ConsoleManagerPerspectiveListener();
		if (PlatformUI.isWorkbenchRunning() && PlatformUI.getWorkbench() != null && PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(perspectiveListener);
		}
	}

	/**
	 * Returns the active workbench window page if the workbench is still running.
	 *
	 * @return The active workbench window page or <code>null</code>
	 */
	private final IWorkbenchPage getActiveWorkbenchPage() {
		// To lookup the console view, the workbench must be still running
		if (PlatformUI.isWorkbenchRunning() && PlatformUI.getWorkbench() != null && PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
			return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		}
		return null;
	}

	/**
	 * Returns the console view if available within the active workbench window page.
	 * <p>
	 * <b>Note:</b> The method must be called within the UI thread.
	 *
	 * @param id The terminal console view id or <code>null</code> to show the default terminal console view.
	 * @return The console view instance if available or <code>null</code> otherwise.
	 */
	public ITerminalsView findConsoleView(String id, String secondaryId) {
		Assert.isNotNull(Display.findDisplay(Thread.currentThread()));

		ITerminalsView view = null;

		// Get the active workbench page
		IWorkbenchPage page = getActiveWorkbenchPage();
		if (page != null) {
			// Look for the view
			IViewPart part=getTerminalsViewWithSecondaryId(id != null ? id : IUIConstants.ID, secondaryId);
			// Check the interface
			if (part instanceof ITerminalsView) {
				view = (ITerminalsView)part;
			}
		}

		return view;
	}

	/**
	 * search and return a terminal view with a specific 
	 * secondary id
	 * @param id
	 * @param secondaryId
	 * @return
	 */
	private IViewPart getTerminalsViewWithSecondaryId(String id, String secondaryId){
		IWorkbenchPage page = getActiveWorkbenchPage();

		IViewReference[] refs=page.getViewReferences();
		for(int i=0; i<refs.length; i++){
			IViewReference ref=refs[i];
			if(ref.getId().equals(id)){
				IViewPart part=ref.getView(true);
				if(part instanceof ITerminalsView){
					String secId=((IViewSite)part.getSite()).getSecondaryId();					
					if(secId.equals(secondaryId)){
						return part;
					}
				}
			}
		}
		return null;
	}

	/**
	 * search and return a terminal view that is NOT pinned
	 * @param id
	 * @return
	 */
	private IViewPart getFirstNotPinnedTerminalsView(String id){
		IWorkbenchPage page = getActiveWorkbenchPage();

		IViewReference[] refs=page.getViewReferences();
		for(int i=0; i<refs.length; i++){
			IViewReference ref=refs[i];
			if(ref.getId().equals(id)){
				IViewPart part=ref.getView(true);
				if(part instanceof ITerminalsView){
					if(!((ITerminalsView)part).isPinned()){
						return part;
					}
				}
			}
		}
		return null;
	}

	/**
	 * search and return a terminal view if available
	 * @param id
	 * @param useActive - return only an active terminal view
	 * @return
	 */
	private IViewPart getFirstTerminalsView(String id, boolean useActive){
		IWorkbenchPage page = getActiveWorkbenchPage();

		IViewReference[] refs=page.getViewReferences();
		for(int i=0; i<refs.length; i++){
			IViewReference ref=refs[i];
			if(ref.getId().equals(id)){
				IViewPart part=ref.getView(true);
				if(useActive){
					if(page.isPartVisible(part)){
						return part;
					}
				} else {
					return part;
				}
			}
		}
		return null;
	}

	/**
	 * cycle through all terminal views and find the highest
	 * secondary view id to know where to continue when
	 * a new terminal view is created
	 * @param id
	 */
	private void countTerminalViews(String id){
		if(!secondaryIdCountInit){
			secondaryIdCountInit=true;
			secondaryIdCount=0;
			IWorkbenchPage page = getActiveWorkbenchPage();

			IViewReference[] refs=page.getViewReferences();
			for(int i=0; i<refs.length; i++){
				IViewReference ref=refs[i];
				if(ref.getId().equals(id)){
					String secId=ref.getSecondaryId();
					try {
						int secIdInt=Integer.parseInt(secId);
						if(secIdInt>secondaryIdCount){
							secondaryIdCount=secIdInt;
						}
					} catch(NumberFormatException e){						
					}
				}
			}
		}
	}

	/**
	 * Show the terminal console view specified by the given id.
	 * <p>
	 * <b>Note:</b> The method must be called within the UI thread.
	 *
	 * @param id The terminal console view id or <code>null</code> to show the default terminal console view.
	 */
	public IViewPart showConsoleView(String id, String secondaryId) {
		Assert.isNotNull(Display.findDisplay(Thread.currentThread()));

		// Get the active workbench page
		IWorkbenchPage page = getActiveWorkbenchPage();
		if (page != null) {
			try {
				// show the view
				IViewPart part = page.showView(id != null ? id : IUIConstants.ID, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
				// and force the view to the foreground
				page.bringToTop(part);
				return part;
			} catch (PartInitException e) {
				IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
											e.getLocalizedMessage(), e);
				UIPlugin.getDefault().getLog().log(status);
			}
		}
		return null;
	}

	/**
	 * Bring the terminal console view, specified by the given id, to the top of the view stack.
	 *
	 * @param id The terminal console view id or <code>null</code> to show the default terminal console view.
	 * @param activate If <code>true</code> activate the console view.
	 */
	private IViewPart bringToTop(String id, boolean activate) {
		// Get the active workbench page
		IWorkbenchPage page = getActiveWorkbenchPage();
		if (page != null) {
			// Look for any terminal view
			IViewPart anyTerminal=getFirstTerminalsView(id != null ? id : IUIConstants.ID, false);
			// there is at least one terminal available
			if(anyTerminal!=null){
				// is there an active terminal view
				IViewPart activePart=getFirstTerminalsView(id != null ? id : IUIConstants.ID, true);
				// no terminal view active
				if(activePart==null){
					// use the first not pinned
					IViewPart notPinnedPart=getFirstNotPinnedTerminalsView(id != null ? id : IUIConstants.ID);
					if(notPinnedPart!=null){
						if (activate) {
							page.activate(notPinnedPart);
						}
						else {
							page.bringToTop(notPinnedPart);
						}
						return notPinnedPart;
					}
					// else we need to create a new one
					secondaryIdCount++;
					IViewPart newPart=showConsoleView(id != null ? id : IUIConstants.ID, Integer.toString(secondaryIdCount));
					return newPart;
				}
				// we found a active terminal page
				// if it is pinned search for a non pinned (not active)
				if(((ITerminalsView)activePart).isPinned()){
					// we found one so use it
					IViewPart notPinnedPart=getFirstNotPinnedTerminalsView(id != null ? id : IUIConstants.ID);
					if(notPinnedPart!=null){
						if (activate) {
							page.activate(notPinnedPart);
						}
						else {
							page.bringToTop(notPinnedPart);
						}
						return notPinnedPart;
					}
					// else we need to create a new one
					secondaryIdCount++;
					IViewPart newPart=showConsoleView(id != null ? id : IUIConstants.ID, Integer.toString(secondaryIdCount));
					return newPart;
				}
				// else return the active one
				return activePart;	
			}
			// create first new terminal
			if (activate) {
				IViewPart newPart=showConsoleView(id != null ? id : IUIConstants.ID, Integer.toString(secondaryIdCount));
				return newPart;
			}
		}
		return null;
	}

	/**
	 * Opens the console with the given title and connector.
	 * <p>
	 * <b>Note:</b> The method must be called within the UI thread.
	 *
	 * @param id The terminal console view id or <code>null</code> to show the default terminal console view.
	 * @param title The console title. Must not be <code>null</code>.
	 * @param connector The terminal connector. Must not be <code>null</code>.
	 * @param data The custom terminal data node or <code>null</code>.
	 * @param activate If <code>true</code> activate the console view.
	 */
	public void openConsole(String id, String title, ITerminalConnector connector, Object data, boolean activate) {
		Assert.isNotNull(title);
		Assert.isNotNull(connector);
		Assert.isNotNull(Display.findDisplay(Thread.currentThread()));

		countTerminalViews(id != null ? id : IUIConstants.ID);

		// make the consoles view visible
		IViewPart part=bringToTop(id, activate);
		if(part==null){
			return;
		}
		ITerminalsView view = (ITerminalsView)part;

		// Get the tab folder manager associated with the view
		TabFolderManager manager = (TabFolderManager)view.getAdapter(TabFolderManager.class);
		if (manager == null) return;

		// Lookup an existing console first
		String secId=((IViewSite)part.getSite()).getSecondaryId();

		CTabItem item = findConsole(id, secId, title, connector, data);

		// If no existing console exist -> Create the tab item
		if (item == null) {
			// If configured, check all existing tab items if they are associated
			// with terminated consoles
			if (UIPlugin.getScopedPreferences().getBoolean(IPreferenceKeys.PREF_REMOVE_TERMINATED_TERMINALS)) {
				manager.removeTerminatedItems();
			}

			// Create a new tab item
			item = manager.createTabItem(title, connector, data);
		}
		// If still null, something went wrong
		if (item == null) {
			return;
		}

		// Make the item the active console
		manager.bringToTop(item);

		// Show the tab folder page
		view.switchToTabFolderControl();
	}

	/**
	 * Lookup a console with the given title and the given terminal connector.
	 * <p>
	 * <b>Note:</b> The method must be called within the UI thread.
	 * <b>Note:</b> The method will handle unified console titles itself.
	 *
	 * @param id The terminal console view id or <code>null</code> to show the default terminal console view.
	 * @param title The console title. Must not be <code>null</code>.
	 * @param connector The terminal connector. Must not be <code>null</code>.
	 * @param data The custom terminal data node or <code>null</code>.
	 *
	 * @return The corresponding console tab item or <code>null</code>.
	 */
	public CTabItem findConsole(String id, String secondaryId, String title, ITerminalConnector connector, Object data) {
		Assert.isNotNull(title);
		Assert.isNotNull(connector);
		Assert.isNotNull(Display.findDisplay(Thread.currentThread()));

		// Get the console view
		ITerminalsView view = findConsoleView(id, secondaryId);
		if (view == null) return null;

		// Get the tab folder manager associated with the view
		TabFolderManager manager = (TabFolderManager)view.getAdapter(TabFolderManager.class);
		if (manager == null) return null;

		return manager.findTabItem(title, connector, data);
	}

	/**
	 * Close the console with the given title and the given terminal connector.
	 * <p>
	 * <b>Note:</b> The method must be called within the UI thread.
	 * <b>Note:</b> The method will handle unified console titles itself.
	 *
	 * @param title The console title. Must not be <code>null</code>.
	 * @param connector The terminal connector. Must not be <code>null</code>.
	 * @param data The custom terminal data node or <code>null</code>.
	 */
	public void closeConsole(String id, String title, ITerminalConnector connector, Object data) {
		Assert.isNotNull(title);
		Assert.isNotNull(connector);
		Assert.isNotNull(Display.findDisplay(Thread.currentThread()));

		String secondaryId=Integer.toString(secondaryIdCount);
		// Lookup the console
		CTabItem console = findConsole(id, secondaryId, title, connector, data);
		// If found, dispose the console
		if (console != null) {
			console.dispose();
		}
	}
}
