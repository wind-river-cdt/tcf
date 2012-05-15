/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.wizards.interfaces;

import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * INewWizardProvider
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
