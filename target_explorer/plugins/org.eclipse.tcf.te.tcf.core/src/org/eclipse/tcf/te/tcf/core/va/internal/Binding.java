/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.va.internal;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.tcf.te.runtime.extensions.ExecutableExtension;

/**
 * Value-add binding implementation.
 */
public class Binding extends ExecutableExtension {
	// The mandatory value-add identifier
	private String valueAddId;
	// The converted expression
	private Expression expression;

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.ExecutableExtension#doSetInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
	public void doSetInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		super.doSetInitializationData(config, propertyName, data);

		// Initialize the value-add id field by reading the <valueadd> extension attribute.
		// Throws an exception if the id is empty or null.
		valueAddId = config != null ? config.getAttribute("valueAddId") : null; //$NON-NLS-1$
		if (valueAddId == null || (valueAddId != null && "".equals(valueAddId.trim()))) { //$NON-NLS-1$
			throw createMissingMandatoryAttributeException("valueAddId", config.getContributor().getName()); //$NON-NLS-1$
		}

		// Read the sub elements of the extension
		IConfigurationElement[] children = config != null ? config.getChildren() : null;
		// The "enablement" element is the only expected one
		if (children != null && children.length > 0) {
			expression = ExpressionConverter.getDefault().perform(children[0]);
		}
	}

	/**
	 * Returns the value-add id which is associated with this binding.
	 *
	 * @return The value-add id.
	 */
	public String getValueAddId() {
		return valueAddId;
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
