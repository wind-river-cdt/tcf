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

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchContextLaunchAttributes;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification;
import org.eclipse.tcf.te.launch.core.persistence.launchcontext.LaunchContextsPersistenceDelegate;
import org.eclipse.tcf.te.launch.core.selection.interfaces.IRemoteSelectionContext;
import org.eclipse.tcf.te.launch.core.selection.interfaces.ISelectionContext;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.runtime.services.ServiceManager;
import org.eclipse.tcf.te.runtime.services.interfaces.IPropertiesAccessService;
import org.eclipse.tcf.te.runtime.services.interfaces.constants.IPropertiesAccessServiceConstants;

/**
 * RemoteAppLaunchManagerDelegate
 */
public class AttachLaunchManagerDelegate extends DefaultLaunchManagerDelegate {

	private static final String[] MANDATORY_CONFIG_ATTRIBUTES = new String[] {
		ILaunchContextLaunchAttributes.ATTR_LAUNCH_CONTEXTS
	};

	/**
	 * Constructor.
	 */
	public AttachLaunchManagerDelegate() {
		super();
	}

	@Override
	public void updateLaunchConfigAttributes(ILaunchConfigurationWorkingCopy wc, ILaunchSpecification launchSpec) {
		super.updateLaunchConfigAttributes(wc, launchSpec);

		if (launchSpec.hasAttribute(ILaunchContextLaunchAttributes.ATTR_LAUNCH_CONTEXTS)) {
			wc.setAttribute(ILaunchContextLaunchAttributes.ATTR_LAUNCH_CONTEXTS, (String)launchSpec.getAttribute(ILaunchContextLaunchAttributes.ATTR_LAUNCH_CONTEXTS).getValue());
		}

		wc.rename(getDefaultLaunchName(wc));
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

		wc.rename(getDefaultLaunchName(wc));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate#updateLaunchConfig(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy, org.eclipse.tcf.te.launch.core.selection.interfaces.ISelectionContext, boolean)
	 */
	@Override
	public void updateLaunchConfig(ILaunchConfigurationWorkingCopy wc, ISelectionContext selContext, boolean replace) {
		super.updateLaunchConfig(wc, selContext, replace);

		if (selContext instanceof IRemoteSelectionContext) {
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

		return launchSpec;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate#getDefaultLaunchName(org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification)
	 */
	@Override
	public String getDefaultLaunchName(ILaunchSpecification launchSpec) {
		IModelNode[] contexts = LaunchContextsPersistenceDelegate.getLaunchContexts(launchSpec);
		String name = getDefaultLaunchName((contexts != null && contexts.length > 0 ? contexts[0] : null));
		return name != null && name.trim().length() > 0 ? name.trim() : super.getDefaultLaunchName(launchSpec);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate#getDefaultLaunchName(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public String getDefaultLaunchName(ILaunchConfiguration launchConfig) {
		IModelNode[] contexts = LaunchContextsPersistenceDelegate.getLaunchContexts(launchConfig);
		String name = getDefaultLaunchName((contexts != null && contexts.length > 0 ? contexts[0] : null));
		return name != null && name.trim().length() > 0 ? name.trim() : super.getDefaultLaunchName(launchConfig);
	}

	private String getDefaultLaunchName(IModelNode context) {
		String name = ""; //$NON-NLS-1$
		if (context != null) {
			IPropertiesAccessService service = ServiceManager.getInstance().getService(context, IPropertiesAccessService.class);
			Object dnsName = service != null ? service.getProperty(context, "dns.name.transient") : null; //$NON-NLS-1$
			String ctxName = service != null ? (String)service.getTargetAddress(context).get(IPropertiesAccessServiceConstants.PROP_ADDRESS) : null;
			ctxName = dnsName != null && dnsName.toString().trim().length() > 0 ? dnsName.toString().trim() : ctxName;

			name = ctxName;
		}
		return name.trim();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate#getMandatoryAttributes()
	 */
	@Override
	protected List<String> getMandatoryAttributes() {
		return Arrays.asList(MANDATORY_CONFIG_ATTRIBUTES);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate#getNumAttributes()
	 */
	@Override
	protected int getNumAttributes() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate#getAttributeRanking(java.lang.String)
	 */
	@Override
	protected int getAttributeRanking(String attributeKey) {
		if (ILaunchContextLaunchAttributes.ATTR_LAUNCH_CONTEXTS.equals(attributeKey)) {
			return getNumAttributes() * 2;
		}
		return 1;
	}
}
