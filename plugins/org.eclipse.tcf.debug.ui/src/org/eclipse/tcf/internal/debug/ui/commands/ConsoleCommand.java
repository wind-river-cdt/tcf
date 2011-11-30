/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.debug.ui.commands;

import org.eclipse.tcf.internal.debug.ui.model.TCFNode;

public class ConsoleCommand extends AbstractActionDelegate {

    @Override
    protected void selectionChanged() {
        getAction().setEnabled(getSelectedNode() != null);
    }

    @Override
    protected void run() {
        TCFNode n = getSelectedNode();
        if (n != null) n.getModel().showDebugConsole();
    }
}
