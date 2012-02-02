/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.selection;

import org.eclipse.tcf.te.launch.core.selection.interfaces.ISelectionContext;

/**
 * Abstract selection context implementation.
 */
public abstract class AbstractSelectionContext implements ISelectionContext {
	// The selection context type
	protected String type = null;
	// The selected objects
	private Object[] selections;
	// Preferred selection context marker
	private boolean isPreferred = false;

	/**
	 * Constructor.
	 *
	 * @param selections The selected objects or <code>null</code>.
	 * @param isPreferred <code>True</code> to mark the selection context the preferred context,
	 *            <code>false</code> otherwise.
	 */
	public AbstractSelectionContext(Object[] selections, boolean isPreferred) {
		this.selections = selections != null ? selections : new Object[0];
		this.isPreferred = isPreferred;
	}

	/**
	 * Constructor.
	 *
	 * @param type The selection context type or <code>null</code>.
	 * @param selections The selected objects or <code>null</code>.
	 * @param isPreferred <code>True</code> to mark the selection context the preferred context,
	 *            <code>false</code> otherwise.
	 */
	public AbstractSelectionContext(String type, Object[] selections, boolean isPreferred) {
		this.type = type;
		this.selections = selections != null ? selections : new Object[0];
		this.isPreferred = isPreferred;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.selection.interfaces.ISelectionContext#getType()
	 */
	@Override
    public String getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.selection.interfaces.ISelectionContext#getSelections()
	 */
	@Override
    public Object[] getSelections() {
		return selections;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.selection.interfaces.ISelectionContext#setIsPreferredContext(boolean)
	 */
	@Override
    public void setIsPreferredContext(boolean isPreferred) {
		this.isPreferred = isPreferred;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.selection.interfaces.ISelectionContext#isPreferredContext()
	 */
	@Override
    public boolean isPreferredContext() {
		return isPreferred;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean equals = obj instanceof ISelectionContext;

		if (equals) {
			ISelectionContext otherContext = (ISelectionContext)obj;

			equals &= type == null && otherContext.getType() == null || type != null && type.equals(otherContext.getType());
			equals &= isPreferred == otherContext.isPreferredContext();

			Object[] otherSelections = otherContext.getSelections();
			equals &= selections == null && otherSelections == null || selections != null && otherSelections != null;

			int i = 0;
			int length = selections != null ? selections.length : -1;
			int otherLength = otherSelections != null ? otherSelections.length : -1;
			equals &= (length == otherLength);

			while (equals && i < length) {
				equals &= selections[i] == null && otherSelections[i] == null || selections[i] != null && selections[i].equals(otherSelections[i]);
				i++;
			}
		}

		return equals;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hashCode = 0;

		if (type != null) {
			hashCode ^= type.hashCode() << 8;
		}
		hashCode ^= Boolean.valueOf(isPreferred).hashCode();

		if (selections != null) {
			hashCode ^= selections.hashCode();
		}

		return hashCode;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer toString = new StringBuffer();

		if (type != null){
			toString.append(type);
		}

		return toString.toString();
	}
}
