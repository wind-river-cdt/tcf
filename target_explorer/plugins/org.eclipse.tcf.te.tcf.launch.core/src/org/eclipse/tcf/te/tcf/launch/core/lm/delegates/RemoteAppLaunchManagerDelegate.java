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
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.tcf.te.launch.core.exceptions.LaunchServiceException;
import org.eclipse.tcf.te.launch.core.interfaces.IReferencedProjectItem;
import org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate;
import org.eclipse.tcf.te.launch.core.lm.interfaces.IFileTransferLaunchAttributes;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchContextLaunchAttributes;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification;
import org.eclipse.tcf.te.launch.core.lm.interfaces.IReferencedProjectLaunchAttributes;
import org.eclipse.tcf.te.launch.core.persistence.DefaultPersistenceDelegate;
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
public class RemoteAppLaunchManagerDelegate extends DefaultLaunchManagerDelegate {

	private static final String[] MANDATORY_CONFIG_ATTRIBUTES = new String[] {
		ILaunchContextLaunchAttributes.ATTR_LAUNCH_CONTEXTS,
		IRemoteAppLaunchAttributes.ATTR_PROCESS_IMAGE
	};

	/**
	 * Constructor.
	 */
	public RemoteAppLaunchManagerDelegate() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate#initLaunchConfigAttributes(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy, org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification)
	 */
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
	public void updateLaunchConfig(ILaunchConfigurationWorkingCopy wc, ISelectionContext selContext, boolean replace) {
		super.updateLaunchConfig(wc, selContext, replace);

		if (selContext instanceof IProjectSelectionContext) {
			List<IFileTransferItem> transfers;
			List<IReferencedProjectItem> projects;
			String processPath;
			if (replace) {
				transfers = new ArrayList<IFileTransferItem>();
				projects = new ArrayList<IReferencedProjectItem>();
				processPath = getProcessImageAndSetProjectAndTransfer((IProjectSelectionContext)selContext, transfers, projects);

				FileTransfersPersistenceDelegate.setFileTransfers(wc, transfers.toArray(new IFileTransferItem[transfers.size()]));
				ReferencedProjectsPersistenceDelegate.setReferencedProjects(wc, projects.toArray(new IReferencedProjectItem[projects.size()]));
				if (processPath != null && processPath.trim().length() > 0) {
					DefaultPersistenceDelegate.setAttribute(wc, IRemoteAppLaunchAttributes.ATTR_PROCESS_IMAGE, processPath);
				}
			}
			else {
				transfers = new ArrayList<IFileTransferItem>(Arrays.asList(FileTransfersPersistenceDelegate.getFileTransfers(wc)));
				projects = new ArrayList<IReferencedProjectItem>(Arrays.asList(ReferencedProjectsPersistenceDelegate.getReferencedProjects(wc)));
				processPath = getProcessImageAndSetProjectAndTransfer((IProjectSelectionContext)selContext, transfers, projects);

				FileTransfersPersistenceDelegate.setFileTransfers(wc, transfers.toArray(new IFileTransferItem[transfers.size()]));
				ReferencedProjectsPersistenceDelegate.setReferencedProjects(wc, projects.toArray(new IReferencedProjectItem[projects.size()]));
				if (processPath != null && processPath.trim().length() > 0 && !DefaultPersistenceDelegate.hasAttribute(wc, IRemoteAppLaunchAttributes.ATTR_PROCESS_IMAGE)) {
					DefaultPersistenceDelegate.setAttribute(wc, IRemoteAppLaunchAttributes.ATTR_PROCESS_IMAGE, processPath);
				}
			}
		}
		else if (selContext instanceof IRemoteSelectionContext) {
			IRemoteSelectionContext remoteCtx = (IRemoteSelectionContext)selContext;
			LaunchContextsPersistenceDelegate.setLaunchContexts(wc, new IModelNode[]{remoteCtx.getRemoteCtx()});
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate#addLaunchSpecAttributes(org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification, java.lang.String, org.eclipse.tcf.te.launch.core.selection.interfaces.ISelectionContext)
	 */
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

			launchSpec.setLaunchConfigName(getDefaultLaunchName(launchSpec));
		}
		else if (selectionContext instanceof IProjectSelectionContext) {
			List<IFileTransferItem> transfers = new ArrayList<IFileTransferItem>(Arrays.asList(FileTransfersPersistenceDelegate.getFileTransfers(launchSpec)));
			List<IReferencedProjectItem> projects = new ArrayList<IReferencedProjectItem>(Arrays.asList(ReferencedProjectsPersistenceDelegate.getReferencedProjects(launchSpec)));
			String processPath = getProcessImageAndSetProjectAndTransfer((IProjectSelectionContext)selectionContext, transfers, projects);

			FileTransfersPersistenceDelegate.setFileTransfers(launchSpec, transfers.toArray(new IFileTransferItem[transfers.size()]));
			ReferencedProjectsPersistenceDelegate.setReferencedProjects(launchSpec, projects.toArray(new IReferencedProjectItem[projects.size()]));
			launchSpec.addAttribute(IRemoteAppLaunchAttributes.ATTR_PROCESS_IMAGE, processPath);

			launchSpec.setLaunchConfigName(getDefaultLaunchName(launchSpec));
		}

		return launchSpec;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate#getDefaultLaunchName(org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification)
	 */
	@Override
	public String getDefaultLaunchName(ILaunchSpecification launchSpec) {
		IModelNode[] contexts = LaunchContextsPersistenceDelegate.getLaunchContexts(launchSpec);
		String processPath = (String)launchSpec.getAttribute(IRemoteAppLaunchAttributes.ATTR_PROCESS_IMAGE, null);
		String name = getDefaultLaunchName((contexts != null && contexts.length > 0 ? contexts[0] : null), processPath);
		return name != null && name.trim().length() > 0 ? name.trim() : super.getDefaultLaunchName(launchSpec);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate#getDefaultLaunchName(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public String getDefaultLaunchName(ILaunchConfiguration launchConfig) {
		IModelNode[] contexts = LaunchContextsPersistenceDelegate.getLaunchContexts(launchConfig);
		String processPath = DefaultPersistenceDelegate.getAttribute(launchConfig, IRemoteAppLaunchAttributes.ATTR_PROCESS_IMAGE, (String)null);
		String name = getDefaultLaunchName((contexts != null && contexts.length > 0 ? contexts[0] : null), processPath);
		return name != null && name.trim().length() > 0 ? name.trim() : super.getDefaultLaunchName(launchConfig);
	}

	private String getDefaultLaunchName(IModelNode context, String processPath) {
		String name = ""; //$NON-NLS-1$
		if (processPath != null) {
			name += new Path(processPath).lastSegment();
		}
		if (context != null) {
			IPropertiesAccessService service = ServiceManager.getInstance().getService(context, IPropertiesAccessService.class);
			Object dnsName = service != null ? service.getProperty(context, "dns.name.transient") : null; //$NON-NLS-1$
			String ctxName = service != null ? (String)service.getTargetAddress(context).get(IPropertiesAccessServiceConstants.PROP_ADDRESS) : null;
			ctxName = dnsName != null && dnsName.toString().trim().length() > 0 ? dnsName.toString().trim() : ctxName;

			name += " (" + ctxName + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return name.trim();
	}

	private String getProcessImageAndSetProjectAndTransfer(IProjectSelectionContext prjContext, List<IFileTransferItem> transfers, List<IReferencedProjectItem> projects) {
		String processName = null;
		String processPath = null;

		boolean added = false;
		for (Object selection : prjContext.getSelections()) {
			if (selection instanceof IPath) {
				IPath path = (IPath)selection;
				IFileTransferItem transfer = new FileTransferItem(path, new Path("/tmp/")); //$NON-NLS-1$
				if (!transfers.contains(transfer)) {
					transfers.add(transfer);
				}
				if (!added) {
					processName = path.lastSegment();
					processPath = "/tmp/" + processName; //$NON-NLS-1$
				}
				added = true;
			}
		}

		IReferencedProjectItem project = new ReferencedProjectItem(prjContext.getProjectCtx().getName());
		if (!projects.contains(project)) {
			projects.add(project);
		}

		return processPath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate#validate(java.lang.String, org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public void validate(String launchMode, ILaunchConfiguration launchConfig) throws LaunchServiceException {
		super.validate(launchMode, launchConfig);

		StringBuilder missingAttributes = new StringBuilder();
		for (String attribute : MANDATORY_CONFIG_ATTRIBUTES) {
			if (!isValidAttribute(attribute, launchConfig, launchMode)) {
				if (missingAttributes.length() == 0) {
					missingAttributes.append(attribute);
				} else {
					missingAttributes.append(", "); //$NON-NLS-1$
					missingAttributes.append(attribute);
				}
			}
		}
		if (missingAttributes.length() > 0) {
			throw new LaunchServiceException("Missing launch configuration attributes: " + '\n' + missingAttributes.toString(), LaunchServiceException.TYPE_MISSING_LAUNCH_CONFIG_ATTR); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate#equals(java.lang.String, java.lang.Object, java.lang.Object, org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification, org.eclipse.debug.core.ILaunchConfiguration, java.lang.String)
	 */
	@Override
	protected int equals(String attributeKey, Object specValue, Object confValue, ILaunchSpecification launchSpec, ILaunchConfiguration launchConfig, String launchMode) {

		if (IRemoteAppLaunchAttributes.ATTR_PROCESS_IMAGE.equals(attributeKey)) {
			// get match of object
			int match = specValue.equals(confValue) ? FULL_MATCH : NO_MATCH;
			// compare objects in the list when they are not already equal
			if (match != FULL_MATCH) {
				IPath confPath = new Path(confValue.toString());
				IPath specPath = new Path(specValue.toString());

				if (confPath.lastSegment().equals(specPath.lastSegment())) {
					match = PARTIAL_MATCH;
				}
				else {
					match = NO_MATCH;
				}
			}
			return match;
		}

		return super.equals(attributeKey, specValue, confValue, launchSpec, launchConfig, launchMode);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate#getNumAttributes()
	 */
	@Override
	protected int getNumAttributes() {
		return 5;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate#getAttributeRanking(java.lang.String)
	 */
	@Override
	protected int getAttributeRanking(String attributeKey) {
		if (ILaunchContextLaunchAttributes.ATTR_LAUNCH_CONTEXTS.equals(attributeKey)) {
			return getNumAttributes() * 32;
		}
		else if (IRemoteAppLaunchAttributes.ATTR_PROCESS_IMAGE.equals(attributeKey)) {
			return getNumAttributes() * 16;
		}
		else if (IRemoteAppLaunchAttributes.ATTR_PROCESS_ARGUMENTS.equals(attributeKey)) {
			return getNumAttributes() * 8;
		}
		else if (IFileTransferLaunchAttributes.ATTR_FILE_TRANSFERS.equals(attributeKey)) {
			return getNumAttributes() * 4;
		}
		else if (IReferencedProjectLaunchAttributes.ATTR_REFERENCED_PROJECTS.equals(attributeKey)) {
			return getNumAttributes() * 2;
		}
		else {
			return 1;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate#getDescription(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public String getDescription(ILaunchConfiguration config) {
		String image = DefaultPersistenceDelegate.getAttribute(config, IRemoteAppLaunchAttributes.ATTR_PROCESS_IMAGE, (String)null);
		String args = DefaultPersistenceDelegate.getAttribute(config, IRemoteAppLaunchAttributes.ATTR_PROCESS_ARGUMENTS, ""); //$NON-NLS-1$
		if (image != null) {
			return new Path(image).toPortableString() + " " + args; //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}
}
