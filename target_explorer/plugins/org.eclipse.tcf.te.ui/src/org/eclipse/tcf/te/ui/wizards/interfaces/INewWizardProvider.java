/*
 * INewWizardProvider.java
 * Created on 14.12.2011
 *
 * Copyright 2011 Wind River Systems Inc. All rights reserved.
 */
package org.eclipse.tcf.te.ui.wizards.interfaces;

import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * INewWizardProvider
 * @author tobias.schwarz@windriver.com
 */
public interface INewWizardProvider extends IExecutableExtension {

	/**
	 * Get the dynamic list of new wizard categories including the wizards.
	 * @return The list of new wizard categories.
	 */
	public IWizardCategory[] getCategories();

	/**
	 * Get the list if common wizards for the given selection.
	 * @param selection The current selection.
	 * @return A list of wizards or <code>null</code>.
	 */
	public IWizardDescriptor[] getCommonWizards(ISelection selection);
}
