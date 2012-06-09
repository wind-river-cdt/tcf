/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.navigator;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.utils.net.IPAddressUtil;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProperties;
import org.eclipse.tcf.te.tcf.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.ui.internal.ImageConsts;
import org.eclipse.tcf.te.tcf.ui.navigator.images.PeerImageDescriptor;
import org.eclipse.tcf.te.tcf.ui.navigator.nodes.PeerRedirectorGroupNode;
import org.eclipse.tcf.te.tcf.ui.nls.Messages;
import org.eclipse.tcf.te.ui.jface.images.AbstractImageDescriptor;


/**
 * Label provider implementation.
 */
public class LabelProviderDelegate extends LabelProvider implements ILabelDecorator {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(final Object element) {
		if (element instanceof IPeerModel) {
			StringBuilder builder = new StringBuilder();

			// Copy the peer node and peer attributes
			final Map<String, Object> attrs = new HashMap<String, Object>();

			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					attrs.putAll(((IPeerModel)element).getProperties());
					attrs.putAll(((IPeerModel)element).getPeer().getAttributes());
				}
			};

			if (Protocol.isDispatchThread()) runnable.run();
			else Protocol.invokeAndWait(runnable);

			// Build up the base label from the peer name
			builder.append((String)attrs.get(IPeer.ATTR_NAME));

			// If the label is "TCF Agent" or "TCF Proxy", than append IP/dns.name
			// (if not localhost) and port to the label
			if (isAppendAddressText(builder.toString())) {
				String dnsName = (String)attrs.get("dns.name.transient"); //$NON-NLS-1$
				String ip = (String)attrs.get(IPeer.ATTR_IP_HOST);
				String port = (String)attrs.get(IPeer.ATTR_IP_PORT);

				if (ip != null && !"".equals(ip.trim())) { //$NON-NLS-1$
					builder.append(" "); //$NON-NLS-1$
					if (!IPAddressUtil.getInstance().isLocalHost(ip)) {
						builder.append(dnsName != null && !"".equals(dnsName.trim()) ? dnsName.trim() : ip.trim()); //$NON-NLS-1$
					}

					if (port != null && !"".equals(port.trim()) && !"1534".equals(port.trim())) { //$NON-NLS-1$ //$NON-NLS-2$
						builder.append(":"); //$NON-NLS-1$
						builder.append(port.trim());
					}
				}
			}

			String label = builder.toString();
			if (label != null && !"".equals(label.trim())) { //$NON-NLS-1$
				return label;
			}
		} else if (element instanceof PeerRedirectorGroupNode) {
			return Messages.RemotePeerDiscoveryRootNode_label;
		}

		return ""; //$NON-NLS-1$
	}

	/**
	 * Determines if the IP-address and port needs to be appended
	 * to the given label.
	 * <p>
	 * The default implementation returns <code>true</code> if the label is either
	 * &quot;TCF Agent&quot; or &quot;TCF Proxy&quot;.
	 *
	 * @param label The label. Must not be <code>null</code>.
	 * @return <code>True</code> if the address shall be appended, <code>false</code> otherwise.
	 */
	protected boolean isAppendAddressText(String label) {
		Assert.isNotNull(label);
		return "TCF Agent".equals(label) || "TCF Proxy".equals(label); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(final Object element) {
		if (element instanceof IPeerModel) {
			final AtomicBoolean isStatic = new AtomicBoolean();

			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					String value = ((IPeerModel)element).getPeer().getAttributes().get("static.transient"); //$NON-NLS-1$
					isStatic.set(value != null && Boolean.parseBoolean(value.trim()));
				}
			};

			if (Protocol.isDispatchThread()) runnable.run();
			else Protocol.invokeAndWait(runnable);

			return isStatic.get() ? UIPlugin.getImage(ImageConsts.PEER) : UIPlugin.getImage(ImageConsts.PEER_DISCOVERED);
		}
		if (element instanceof PeerRedirectorGroupNode) {
			return UIPlugin.getImage(ImageConsts.DISCOVERY_ROOT);
		}

		return super.getImage(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateImage(org.eclipse.swt.graphics.Image, java.lang.Object)
	 */
	@Override
	public Image decorateImage(Image image, Object element) {
		Image decoratedImage = null;

		if (image != null && element instanceof IPeerModel) {
			AbstractImageDescriptor descriptor = new PeerImageDescriptor(UIPlugin.getDefault().getImageRegistry(),
			                                                             image,
			                                                             (IPeerModel)element);
			decoratedImage = UIPlugin.getSharedImage(descriptor);
		}

		return decoratedImage;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(java.lang.String, java.lang.Object)
	 */
	@Override
	public String decorateText(final String text, final Object element) {
		if (element instanceof IPeerModel) {
			String label = text;

			final StringBuilder builder = new StringBuilder(label != null && !"".equals(label.trim()) ? label.trim() : "<noname>"); //$NON-NLS-1$ //$NON-NLS-2$

			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					doDecorateText(builder, (IPeerModel)element);
				}
			};

			if (Protocol.isDispatchThread()) runnable.run();
			else Protocol.invokeAndWait(runnable);

			label = builder.toString();

			if (label != null && !"".equals(label.trim()) && !"<noname>".equals(label.trim())) { //$NON-NLS-1$ //$NON-NLS-2$
				return label;
			}
		}
		return null;
	}

	/**
	 * Decorate the text with some peer attributes.
	 * <p>
	 * <b>Note:</b> Must be called with the TCF event dispatch thread.
	 *
	 * @param builder The string builder to decorate. Must not be <code>null</code>.
	 * @param peerModel The peer model node. Must not be <code>null</code>.
	 */
	/* default */ void doDecorateText(StringBuilder builder, IPeerModel peerModel) {
		Assert.isNotNull(builder);
		Assert.isNotNull(peerModel);
		Assert.isTrue(Protocol.isDispatchThread());

		int state = peerModel.getIntProperty(IPeerModelProperties.PROP_STATE);
		if (state > IPeerModelProperties.STATE_UNKNOWN) {
			builder.append(" ["); //$NON-NLS-1$
			builder.append(Messages.getString("LabelProviderDelegate_state_" + state)); //$NON-NLS-1$
			builder.append("]"); //$NON-NLS-1$
		}
	}
}
