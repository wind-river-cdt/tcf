/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.interfaces;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.te.ui.utils.AbstractSearchable;

/**
 * This interface should be implemented or adapted by the tree nodes which
 * should provide find function. Subclass is encouraged to inherit AbstractSearchable
 * which provides basic implementation methods, or CompositeSearchable which combines
 * several simple searchable implementations into a complexed implementation.
 * 
 * @see AbstractSearchable
 * @see CompositeSearchable
 */
public interface ISearchable {
	/**
	 * Get the title text of the searching dialog for
	 * this element.
	 * 
	 * @return A title of the search dialog
	 */
	public String getSearchTitle();
	
	/**
	 * Get the description message that will be displayed 
	 * in the title area of the searching dialog with
	 * the given root element.
	 * 
	 * @param rootElement The root element where the search is started.
	 * @return The message to describe the searching.
	 */
	public String getSearchMessage(Object rootElement);
	
	/**
	 * Get a text to be used during searching process.
	 * 
	 * @param element The element to searched.
	 * @return A description text used in searching.
	 */
	public String getElementText(Object element);
	
	/**
	 * Create the part in the searching dialog where the user 
	 * enters the common matching rule used in searching.
	 * 
	 * @param parent The parent composite of this option part.
	 */
	public void createCommonPart(Composite parent);
	
	/**
	 * Create the part in the searching dialog where the user 
	 * enters the advanced matching rule used in searching.
	 * 
	 * @param parent The parent composite of this option part.
	 */
	public void createAdvancedPart(Composite parent);
	
	/**
	 * Get a searching matcher object to test if a tree node matches
	 * the current conditions entered by the user.
	 * 
	 * @return The matcher object which implements ISearchMatcher for matching.
	 */
	public ISearchMatcher getMatcher();
	
	/**
	 * If the current input from the user is valid for a searching process.
	 * 
	 * @return true if the input is valid or else false.
	 */
	public boolean isInputValid();
	
	/**
	 * Add an option listener that handles the option changed event.
	 * 
	 * @param listener The listener to be added.
	 */
	public void addOptionListener(IOptionListener listener);
	
	/**
	 * Remove an option listener
	 * 
	 * @param listener The listener to be removed.
	 */
	public void removeOptionListener(IOptionListener listener);
	
	/**
	 * Restore the part's values from its parent settings.
	 * 
	 * @param settings The dialog settings.
	 */
	public void restoreValues(IDialogSettings settings);
	
	/**
	 * Persist the part's values to its parent settings.
	 * Called when the searching dialog is closed.
	 * 
	 * @param settings The dialog settings.
	 */
	public void persistValues(IDialogSettings settings);
}
