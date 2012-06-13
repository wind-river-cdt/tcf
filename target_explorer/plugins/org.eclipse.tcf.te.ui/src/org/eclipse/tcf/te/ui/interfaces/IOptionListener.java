/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.interfaces;

import java.util.EventListener;
import java.util.EventObject;

/**
 * The interface that listens and processes the event that
 * the searching options have been changed. 
 */
public interface IOptionListener extends EventListener {
	/**
	 * Invoked when one of the searching options has changed.
	 * 
	 * @param event An option changed event.
	 */
	public void optionChanged(EventObject event);
}
