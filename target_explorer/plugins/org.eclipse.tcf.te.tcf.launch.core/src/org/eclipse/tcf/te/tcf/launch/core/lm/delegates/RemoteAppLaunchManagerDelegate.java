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
import org.eclipse.tcf.te.launch.core.interfaces.IReferencedProjectItem;
import org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate;
import org.eclipse.tcf.te.launch.core.lm.interfaces.IFileTransferLaunchAttributes;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchContextLaunchAttributes;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification;
import org.eclipse.tcf.te.launch.core.lm.interfaces.IReferencedProjectLaunchAttributes;
import org.eclipse.tcf.te.launch.core.nls.Messages;
import org.eclipse.tcf.te.launch.core.persistence.filetransfer.FileTransfersPersistenceDelegate;
import org.eclipse.tcf.te.launch.core.persistence.launchcontext.LaunchContextsPersistenceDelegate;
import org.eclipse.tcf.te.launch.core.persistence.projects.ReferencedProjectItem;
import org.eclipse.tcf.te.launch.core.persistence.projects.ReferencedProjectsPersistenceDelegate;
import org.eclipse.tcf.te.launch.core.selection.interfaces.IProjectSelectionContext;
import org.eclipse.tcf.te.launch.core.selection.interfaces.IRemoteSelectionContext;
import org.eclipse.tcf.te.launch.core.selection.interfaces.ISelectionContext;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.runtime.services.ServiceManager;
import org.eclipse.tcf.te.runtime.services.filetransfer.FileTransferItem;
import org.eclipse.tcf.te.runtime.services.interfaces.IPropertiesAccessService;
import org.eclipse.tcf.te.runtime.services.interfaces.constants.IPropertiesAccessServiceConstants;
import org.eclipse.tcf.te.runtime.services.interfaces.filetransfer.IFileTransferItem;
import org.eclipse.tcf.te.tcf.launch.core.interfaces.IRemoteAppLaunchAttributes;

/**
 * RemoteAppLaunchManagerDelegate
 */
@SuppressWarnings("restriction")
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

		if (launchSpec.hasAttribute(ILaunchContextLaunchAttributes.ATTR_LAUNCH_CONTEXTS)) {
			wc.setAttribute(ILaunchContextLaunchAttributes.ATTR_LAUNCH_CONTEXTS, (String)launchSpec.getAttribute(ILaunchContextLaunchAttributes.ATTR_LAUNCH_CONTEXTS).getValue());
		}
		if (launchSpec.hasAttribute(IRemoteAppLaunchAttributes.ATTR_PROCESS_IMAGE)) {
			wc.setAttribute(IRemoteAppLaunchAttributes.ATTR_PROCESS_IMAGE, (String)launchSpec.getAttribute(IRemoteAppLaunchAttributes.ATTR_PROCESS_IMAGE).getValue());
		}
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

		if (selectionContext instanceof IRemoteSelectionContext) {
			List<IModelNode> launchContexts = new ArrayList<IModelNode>(Arrays.asList(LaunchContextsPersistenceDelegate.getLaunchContexts(launchSpec)));
			IModelNode remoteCtx = ((IRemoteSelectionContext)selectionContext).getRemoteCtx();
			if (!launchContexts.contains(remoteCtx)) {
				launchContexts.add(remoteCtx);
				LaunchContextsPersistenceDelegate.setLaunchContexts(launchSpec, launchContexts.toArray(new IModelNode[launchContexts.size()]));
			}

			IPropertiesAccessService service = ServiceManager.getInstance().getService(remoteCtx, IPropertiesAccessService.class);
			String ctxName = service != null ? (String)service.getTargetAddress(remoteCtx).get(IPropertiesAccessServiceConstants.PROP_ADDRESS) : null;

			if (ctxName != null) {
				if (launchSpec.getLaunchConfigName() == null ||
								Messages.DefaultLaunchManagerDelegate_defaultLaunchName.equals(launchSpec.getLaunchConfigName())) {
					launchSpec.setLaunchConfigName(ctxName);
				}
				else {
					launchSpec.setLaunchConfigName(launchSpec.getLaunchConfigName() + " (" + ctxName + ")"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		else if (selectionContext instanceof IProjectSelectionContext) {
			List<IFileTransferItem> transfers = new ArrayList<IFileTransferItem>(Arrays.asList(FileTransfersPersistenceDelegate.getFileTransfers(launchSpec)));
			List<IReferencedProjectItem> projects = new ArrayList<IReferencedProjectItem>(Arrays.asList(ReferencedProjectsPersistenceDelegate.getReferencedProjects(launchSpec)));
			String processName = null;
			String processPath = null;

			boolean added = false;
			for (Object selection : selectionContext.getSelections()) {
				if (selection instanceof IPath) {
					IPath path = (IPath)selection;
					IFileTransferItem transfer = new FileTransferItem();
					transfer.setProperty(IFileTransferItem.PROPERTY_ENABLED, true);
					transfer.setProperty(IFileTransferItem.PROPERTY_HOST, path.toPortableString());
					transfer.setProperty(IFileTransferItem.PROPERTY_DIRECTION, IFileTransferItem.HOST_TO_TARGET);
					transfer.setProperty(IFileTransferItem.PROPERTY_TARGET, new Path("/tmp/").toPortableString()); //$NON-NLS-1$
					transfers.add(transfer);
					if (!added) {
						processName = path.lastSegment();
						processPath = "/tmp/" + processName; //$NON-NLS-1$
					}
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
			launchSpec.addAttribute(IRemoteAppLaunchAttributes.ATTR_PROCESS_IMAGE, processPath);

			if (launchSpec.getLaunchConfigName() == null ||
							Messages.DefaultLaunchManagerDelegate_defaultLaunchName.equals(launchSpec.getLaunchConfigName())) {
				launchSpec.setLaunchConfigName(processName);
			}
			else {
				launchSpec.setLaunchConfigName(processName + " (" + launchSpec.getLaunchConfigName() + ")");  //$NON-NLS-1$//$NON-NLS-2$
			}
		}

		return launchSpec;
	}
}
