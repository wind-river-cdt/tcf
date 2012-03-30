/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.tcf.te.tcf.launch.core.lm.delegates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.tcf.te.launch.core.interfaces.IFileTransferItem;
import org.eclipse.tcf.te.launch.core.interfaces.IReferencedProjectItem;
import org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate;
import org.eclipse.tcf.te.launch.core.lm.interfaces.IFileTransferLaunchAttributes;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification;
import org.eclipse.tcf.te.launch.core.lm.interfaces.IReferencedProjectLaunchAttributes;
import org.eclipse.tcf.te.launch.core.persistence.filetransfer.FileTransferItem;
import org.eclipse.tcf.te.launch.core.persistence.filetransfer.FileTransfersPersistenceDelegate;
import org.eclipse.tcf.te.launch.core.persistence.projects.ReferencedProjectItem;
import org.eclipse.tcf.te.launch.core.persistence.projects.ReferencedProjectsPersistenceDelegate;
import org.eclipse.tcf.te.launch.core.selection.interfaces.IProjectSelectionContext;
import org.eclipse.tcf.te.launch.core.selection.interfaces.ISelectionContext;

/**
 * RemoteAppLaunchManagerDelegate
 * @author tobias.schwarz@windriver.com
 */
public class RemoteAppLaunchManagerDelegate extends DefaultLaunchManagerDelegate {

	/**
	 * Constructor.
	 */
	public RemoteAppLaunchManagerDelegate() {
		super();
	}

	@Override
	public void initLaunchConfigAttributes(ILaunchConfigurationWorkingCopy wc, ILaunchSpecification launchSpec) {
		super.initLaunchConfigAttributes(wc, launchSpec);

		if (launchSpec.hasAttribute(IFileTransferLaunchAttributes.ATTR_FILE_TRANSFERS)) {
			wc.setAttribute(IFileTransferLaunchAttributes.ATTR_FILE_TRANSFERS, (String)launchSpec.getAttribute(IFileTransferLaunchAttributes.ATTR_FILE_TRANSFERS).getValue());
		}
		if (launchSpec.hasAttribute(IReferencedProjectLaunchAttributes.ATTR_REFERENCED_PROJECTS)) {
			wc.setAttribute(IReferencedProjectLaunchAttributes.ATTR_REFERENCED_PROJECTS, (String)launchSpec.getAttribute(IReferencedProjectLaunchAttributes.ATTR_REFERENCED_PROJECTS).getValue());
		}
	}

	@Override
	protected ILaunchSpecification addLaunchSpecAttributes(ILaunchSpecification launchSpec, String launchConfigTypeId, ISelectionContext selectionContext) {
		launchSpec = super.addLaunchSpecAttributes(launchSpec, launchConfigTypeId, selectionContext);

		if (selectionContext instanceof IProjectSelectionContext) {
			List<IFileTransferItem> transfers = new ArrayList<IFileTransferItem>(Arrays.asList(FileTransfersPersistenceDelegate.getFileTransfers(launchSpec)));
			List<IReferencedProjectItem> projects = new ArrayList<IReferencedProjectItem>(Arrays.asList(ReferencedProjectsPersistenceDelegate.getReferencedProjects(launchSpec)));

			boolean added = false;
			for (Object selection : selectionContext.getSelections()) {
				if (selection instanceof IPath) {
					IPath path = (IPath)selection;
					IFileTransferItem transfer = new FileTransferItem();
					transfer.setProperty(IFileTransferItem.PROPERTY_ENABLED, true);
					transfer.setProperty(IFileTransferItem.PROPERTY_HOST, path.toPortableString());
					transfer.setProperty(IFileTransferItem.PROPERTY_DIRECTION, IFileTransferItem.HOST_TO_TARGET);
					transfer.setProperty(IFileTransferItem.PROPERTY_HOST, new Path("/tmp/").toPortableString()); //$NON-NLS-1$
					transfers.add(transfer);
					added = true;
				}
			}

			if (added) {
				IReferencedProjectItem project = new ReferencedProjectItem();
				project.setProperty(IReferencedProjectItem.PROPERTY_ENABLED, true);
				project.setProperty(IReferencedProjectItem.PROPERTY_PROJECT_NAME, ((IProjectSelectionContext)selectionContext).getProjectCtx().getName());
				projects.add(project);
			}

			FileTransfersPersistenceDelegate.setFileTransfers(launchSpec, transfers.toArray(new IFileTransferItem[transfers.size()]));
			ReferencedProjectsPersistenceDelegate.setReferencedProjects(launchSpec, projects.toArray(new IReferencedProjectItem[projects.size()]));
		}

		return launchSpec;
	}
}
