/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * Tobias Schwarz (Wind River) - [368243] [UI] Allow dynamic new wizard contributions
 *******************************************************************************/
package org.eclipse.tcf.te.ui.wizards.newWizard;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.tcf.te.ui.activator.UIPlugin;
import org.eclipse.tcf.te.ui.wizards.interfaces.INewWizardProvider;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.wizards.AbstractExtensionWizardRegistry;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * New wizard registry.
 *
 * @see org.eclipse.ui.internal.wizards.NewWizardRegistry
 */
@SuppressWarnings("restriction")
public final class NewWizardRegistry extends AbstractExtensionWizardRegistry {
	// The list of new wizard providers
	private List<INewWizardProvider> newWizardProvider = new ArrayList<INewWizardProvider>();
	// The reference to the root category
	private IWizardCategory root;

	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstance {
		public static NewWizardRegistry instance = new NewWizardRegistry();
	}

	/**
	 * Constructor.
	 */
	/* default */ NewWizardRegistry() {
		super();
	}

	/**
	 * Returns the singleton instance of the wizard registry.
	 */
	public static NewWizardRegistry getInstance() {
		return LazyInstance.instance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.wizards.AbstractExtensionWizardRegistry#doInitialize()
	 */
	@Override
	protected void doInitialize() {
		super.doInitialize();

		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(getPlugin(), "wizardProviders"); //$NON-NLS-1$
		for (IConfigurationElement element : point.getConfigurationElements()) {
			if (element.getName().equals("wizardProvider")) { //$NON-NLS-1$
				try {
					INewWizardProvider provider = (INewWizardProvider)element.createExecutableExtension("class"); //$NON-NLS-1$
					newWizardProvider.add(provider);
				}
				catch (CoreException e) {
				}
			}
		}

		// Determine the root category
		if (!newWizardProvider.isEmpty()) {
			NewWizardCategory newRoot = new NewWizardCategory(super.getRootCategory());

			for (INewWizardProvider provider : newWizardProvider) {
				for (IWizardCategory category : provider.getCategories()) {
					// Look for an existing category first
					IWizardCategory existing = newRoot.findCategory(category.getId());
					if (existing != null) {
						// The category exist already -> merge needed.
						if (existing instanceof NewWizardCategory) {
							mergeCategory(category, (NewWizardCategory)existing);
						} else {
							NewWizardCategory dst = new NewWizardCategory(existing);
							if (super.getRootCategory().equals(dst.getParent())) {
								dst.setParent(newRoot);
							}
							mergeCategory(category, dst);
							newRoot.removeCategory(existing);
							newRoot.addCategory(dst);
						}
					}
					else {
						newRoot.addCategory(category);
					}
				}
			}

			root = newRoot;
		} else {
			root = super.getRootCategory();
		}
	}

	/**
	 * Merge wizard categories.
	 *
	 * @param src The source wizard category. Must not be <code>null</code>.
	 * @param dst The destination wizard category. Must not be <code>null</code>.
	 */
	private void mergeCategory(IWizardCategory src, NewWizardCategory dst) {
		Assert.isNotNull(src);
		Assert.isNotNull(dst);

		IWizardCategory[] categories = src.getCategories();
		for (IWizardCategory category : categories) {
			IWizardCategory existing = dst.findCategory(category.getId());
			if (existing != null) {
				if (existing instanceof NewWizardCategory) {
					mergeCategory(category, (NewWizardCategory)existing);
				} else {
					NewWizardCategory merged = new NewWizardCategory(existing);
					mergeCategory(category, merged);
					dst.addCategory(merged);
				}
			}
			else {
				dst.addCategory(category);
			}
		}

		IWizardDescriptor[] wizards = src.getWizards();
		for (IWizardDescriptor wizard : wizards) {
			if (dst.findWizard(wizard.getId()) == null) {
				dst.addWizard(wizard);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.wizards.AbstractExtensionWizardRegistry#getExtensionPoint()
	 */
	@Override
	protected String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_NEW;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.wizards.AbstractExtensionWizardRegistry#getPlugin()
	 */
	@Override
	protected String getPlugin() {
		return UIPlugin.getUniqueIdentifier();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.wizards.AbstractWizardRegistry#findWizard(java.lang.String)
	 */
	@Override
	public IWizardDescriptor findWizard(String id) {
		return getRootCategory().findWizard(id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.wizards.AbstractWizardRegistry#getPrimaryWizards()
	 */
	@Override
	public IWizardDescriptor [] getPrimaryWizards() {
		return super.getPrimaryWizards();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.wizards.AbstractWizardRegistry#findCategory(java.lang.String)
	 */
	@Override
	public IWizardCategory findCategory(String id) {
		IWizardCategory root = getRootCategory();
		if (root instanceof NewWizardCategory) {
			return ((NewWizardCategory)root).findCategory(id);
		}
		return super.findCategory(id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.wizards.AbstractWizardRegistry#getRootCategory()
	 */
	@Override
	public IWizardCategory getRootCategory() {
		initialize();
		return root;
	}

	/**
	 * Get the list of common wizards for the given selection.
	 *
	 * @param selection The current selection.
	 * @return The list of common wizards.
	 */
	public IWizardDescriptor[] getCommonWizards(ISelection selection) {
		initialize();
		List<IWizardDescriptor> allWizards = new ArrayList<IWizardDescriptor>();

		for (INewWizardProvider provider : newWizardProvider) {
			IWizardDescriptor[] wizards = provider.getCommonWizards(selection);
			if (wizards != null && wizards.length > 0) {
				allWizards.addAll(Arrays.asList(wizards));
			}
		}

		return allWizards.toArray(new IWizardDescriptor[allWizards.size()]);
	}
}
