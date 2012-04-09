/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.core.internal.operations;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;

public class OpOutStreamOp extends OpStreamOp {
	private OutputStream output;
	private FSTreeNode node;
	public OpOutStreamOp(FSTreeNode node, OutputStream output) {
		this.node = node;
		this.output = output;
	}
	@Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		try {
	        download2OutputStream(node, output, monitor);
        }
        catch (IOException e) {
        	throw new InvocationTargetException(e);
        }
    }
}
