/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tests.tcf.processes;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.te.tcf.processes.core.model.ProcessModel;
import org.eclipse.tcf.te.tcf.processes.core.model.ProcessTreeNode;
import org.eclipse.tcf.te.tests.tcf.TcfTestCase;

public class ProcessesTestCase extends TcfTestCase {
	protected ProcessModel processModel;
	protected ProcessTreeNode processRoot;

	@Override
    protected void setUp() throws Exception {
	    super.setUp();
	    Assert.isNotNull(peerModel);
	    processModel = ProcessModel.getProcessModel(peerModel);
	    Assert.isNotNull(processModel);
	    processRoot = processModel.getRoot();
	    Assert.isNotNull(processRoot);
    }
}
