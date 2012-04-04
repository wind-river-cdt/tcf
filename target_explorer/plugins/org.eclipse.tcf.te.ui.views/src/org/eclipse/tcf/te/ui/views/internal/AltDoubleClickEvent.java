/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.internal;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;

/**
 * Special double click event denoting a double click with pressed ALT key.
 */
public class AltDoubleClickEvent extends DoubleClickEvent {
    private static final long serialVersionUID = 4214922560003128633L;

	/**
	 * Constructor.
	 *
	 * @param source The viewer.
	 * @param selection The selection.
	 */
	public AltDoubleClickEvent(Viewer source, ISelection selection) {
		super(source, selection);
	}
}
