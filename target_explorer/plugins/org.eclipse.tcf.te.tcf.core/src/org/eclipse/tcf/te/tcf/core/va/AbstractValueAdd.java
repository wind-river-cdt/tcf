/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.va;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.tcf.te.runtime.extensions.ExecutableExtension;
import org.eclipse.tcf.te.tcf.core.va.interfaces.IValueAdd;

/**
 * Abstract value-add implementation.
 */
public abstract class AbstractValueAdd extends ExecutableExtension implements IValueAdd {
	// Flag marking the value-add as optional
	private boolean optional = false;
	// For internal debugging purpose, a value-add can be redirected to an
	// manual launched value-add via "-DVA_<id>.peerId=<peerId>"
	private String debugPeerId = null;

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.ExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
	    super.setInitializationData(config, propertyName, data);

	    String value = config.getAttribute("optional"); //$NON-NLS-1$
	    if (value != null && !"".equals(value.trim())) { //$NON-NLS-1$
	    	optional = Boolean.valueOf(value.trim()).booleanValue();
	    }

	    value = System.getProperty("VA_" + getId() + ".peerId"); //$NON-NLS-1$1 //$NON-NLS-2$2
	    if (value != null && !"".equals(value.trim())) { //$NON-NLS-1$
	    	debugPeerId = value.trim();
	    }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.core.va.interfaces.IValueAdd#isOptional()
	 */
	@Override
    public final boolean isOptional() {
		return optional;
	}

	/**
	 * Returns the peer id to use if set manually for debugging purpose.
	 *
	 * @return The peer id or <code>null</code>.
	 */
	protected final String getDebugPeerId() {
		return debugPeerId;
	}
}
