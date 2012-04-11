/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.tcf.te.tcf.launch.core.steps.iterators;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tcf.te.launch.core.lm.interfaces.IFileTransferLaunchAttributes;
import org.eclipse.tcf.te.launch.core.persistence.filetransfer.FileTransfersPersistenceDelegate;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.services.interfaces.filetransfer.IFileTransferItem;
import org.eclipse.tcf.te.runtime.stepper.StepperAttributeUtil;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext;
import org.eclipse.tcf.te.tcf.launch.core.activator.CoreBundleActivator;

/**
 * Step group iterator for file transfer.
 */
public class FileTransferIterator extends AbstractTcfLaunchStepGroupIterator {

	private IFileTransferItem[] items = null;
	private int iteration = -1;

	/**
	 * Constructor.
	 */
	public FileTransferIterator() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepGroupIterator#initialize(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void initialize(IStepContext context, IPropertiesContainer data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor) {
		items = FileTransfersPersistenceDelegate.getFileTransfers(getLaunchConfiguration(context));
		iteration = 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepGroupIterator#getNumIterations()
	 */
	@Override
	public int getNumIterations() {
		return items != null ? items.length : 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepGroupIterator#hasNext(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public boolean hasNext(IStepContext context, IPropertiesContainer data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor) {
		return iteration < getNumIterations();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepGroupIterator#next(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void next(IStepContext context, IPropertiesContainer data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor) throws CoreException {
		if (iteration < 0) {
			throw new CoreException(new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), "iterator not initialized")); //$NON-NLS-1$
		}
		if (iteration >= getNumIterations()) {
			throw new CoreException(new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), "no more iterations")); //$NON-NLS-1$
		}
		StepperAttributeUtil.setProperty(IFileTransferLaunchAttributes.ATTR_ACTIVE_FILE_TRANSFER, fullQualifiedId, data, items[iteration++]);
	}
}
