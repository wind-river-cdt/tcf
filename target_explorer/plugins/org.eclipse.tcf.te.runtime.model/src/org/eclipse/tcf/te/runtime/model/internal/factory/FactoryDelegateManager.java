/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.model.internal.factory;

import java.util.Collection;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager;
import org.eclipse.tcf.te.runtime.extensions.ExecutableExtensionProxy;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.runtime.model.interfaces.factory.IFactoryDelegate;


/**
 * Model node factory delegate extension point manager implementation.
 */
public class FactoryDelegateManager extends AbstractExtensionPointManager<IFactoryDelegate> {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager#getExtensionPointId()
	 */
	@Override
	protected String getExtensionPointId() {
		return "org.eclipse.tcf.te.runtime.model.factoryDelegates"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager#getConfigurationElementName()
	 */
	@Override
	protected String getConfigurationElementName() {
		return "delegate"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager#doCreateExtensionProxy(org.eclipse.core.runtime.IConfigurationElement)
	 */
	@Override
	protected ExecutableExtensionProxy<IFactoryDelegate> doCreateExtensionProxy(IConfigurationElement element) throws CoreException {
	    return new FactoryDelegateProxy(element);
	}

	/**
	 * Returns the model node factory delegate for the given node type.
	 * <p>
	 * <b>Note:</b> The first factory delegate declaring the given node type
	 *              as supported will be returned.
	 *
	 * @param nodeType The node type. Must not be <code>null</code>.
	 * @return The model node factory delegate or <code>null</code>.
	 */
	public IFactoryDelegate getFactoryDelegate(Class<? extends IModelNode> nodeType) {
		Assert.isNotNull(nodeType);

		IFactoryDelegate delegate = null;

		Collection<ExecutableExtensionProxy<IFactoryDelegate>> delegates = getExtensions().values();
		for (ExecutableExtensionProxy<IFactoryDelegate> candidate : delegates) {
			if (!(candidate instanceof FactoryDelegateProxy)) continue;
			if (((FactoryDelegateProxy)candidate).getNodeTypes().contains(nodeType)) {
				delegate = candidate.getInstance();
				break;
			}
		}

		return delegate;
	}

}
