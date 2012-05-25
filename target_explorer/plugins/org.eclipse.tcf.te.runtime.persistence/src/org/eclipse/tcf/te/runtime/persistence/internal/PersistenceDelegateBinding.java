/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.persistence.internal;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.tcf.te.runtime.extensions.ExecutableExtension;

/**
 * Persistence delegate binding implementation.
 */
public class PersistenceDelegateBinding extends ExecutableExtension {
	// The mandatory delegate identifier
	private String delegateId;
	// The converted expression
	private Expression expression;

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.ExecutableExtension#doSetInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
	public void doSetInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		super.doSetInitializationData(config, propertyName, data);

		// Initialize the delegate id field by reading the <delegate> extension attribute.
		// Throws an exception if the id is empty or null.
		delegateId = config != null ? config.getAttribute("delegateId") : null; //$NON-NLS-1$
		if (delegateId == null || "".equals(delegateId.trim())) { //$NON-NLS-1$
			throw createMissingMandatoryAttributeException("delegateId", config.getContributor().getName()); //$NON-NLS-1$
		}

		// Read the sub elements of the extension
		IConfigurationElement[] children = config != null ? config.getChildren() : null;
		// The "enablement" element is the only expected one
		if (children != null && children.length > 0) {
			expression = ExpressionConverter.getDefault().perform(children[0]);
		}
	}

	/**
	 * Returns the delegate id which is associated with this binding.
	 *
	 * @return The delegate id.
	 */
	public String getDelegateId() {
		return delegateId;
	}

	/**
	 * Returns the enablement expression which is associated with this binding.
	 *
	 * @return The enablement expression or <code>null</code>.
	 */
	public Expression getEnablement() {
		return expression;
	}
}
