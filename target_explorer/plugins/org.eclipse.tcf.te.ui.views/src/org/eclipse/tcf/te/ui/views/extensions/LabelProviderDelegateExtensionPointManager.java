/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.extensions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager;
import org.eclipse.tcf.te.runtime.extensions.ExecutableExtensionProxy;
import org.eclipse.tcf.te.ui.activator.UIPlugin;

/**
 * Label Provider Delegate extension point manager implementation.
 */
public class LabelProviderDelegateExtensionPointManager extends AbstractExtensionPointManager<ILabelProvider> {
	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstance {
		public static LabelProviderDelegateExtensionPointManager instance = new LabelProviderDelegateExtensionPointManager();
	}

	/**
	 * Constructor.
	 */
	LabelProviderDelegateExtensionPointManager() {
		super();
	}

	/**
	 * Returns the singleton instance of the extension point manager.
	 */
	public static LabelProviderDelegateExtensionPointManager getInstance() {
		return LazyInstance.instance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager#getExtensionPointId()
	 */
	@Override
	protected String getExtensionPointId() {
		return "org.eclipse.tcf.te.ui.views.labelProviderDelegates"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager#getConfigurationElementName()
	 */
	@Override
	protected String getConfigurationElementName() {
		return "labelProviderDelegate"; //$NON-NLS-1$
	}

	/**
	 * Returns the list of all contributed label provider delegates.
	 *
	 * @param unique If <code>true</code>, the method returns new instances for each
	 *               contributed label provider delegate.
	 *
	 * @return The list of contributed label provider delegates, or an empty array.
	 */
	public ILabelProvider[] getDelegates(Object context, boolean unique) {
		List<ILabelProvider> contributions = new ArrayList<ILabelProvider>();
		Collection<ExecutableExtensionProxy<ILabelProvider>> delegates = getExtensions().values();
		for (ExecutableExtensionProxy<ILabelProvider> delegate : delegates) {
			Expression enablement = null;
			// Read the sub elements of the extension
			IConfigurationElement[] children = delegate != null ? delegate.getConfigurationElement().getChildren() : null;
			// The "enablement" element is the only expected one
			if (children != null && children.length > 0) {
				try {
					enablement = ExpressionConverter.getDefault().perform(children[0]);
				}
				catch (CoreException e) {}
			}

			// The binding is applicable by default if no expression is specified.
			boolean isApplicable = enablement == null;

			if (enablement != null) {
				if (context != null) {
					// Set the default variable to the delegate context.
					EvaluationContext evalContext = new EvaluationContext(null, context);
					evalContext.addVariable("context", context); //$NON-NLS-1$
					// Allow plugin activation
					evalContext.setAllowPluginActivation(true);
					// Evaluate the expression
					try {
						isApplicable = enablement.evaluate(evalContext).equals(EvaluationResult.TRUE);
					} catch (CoreException e) {
						IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
										e.getLocalizedMessage(), e);
						Platform.getLog(UIPlugin.getDefault().getBundle()).log(status);
					}
				} else {
					// The enablement is false by definition if no delegate context is given.
					isApplicable = false;
				}
			}

			if (isApplicable) {
				ILabelProvider instance = unique ? delegate.newInstance() : delegate.getInstance();
				if (instance != null && !contributions.contains(instance)) {
					contributions.add(instance);
				}
			}
		}

		return contributions.toArray(new ILabelProvider[contributions.size()]);
	}
}
