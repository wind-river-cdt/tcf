/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.internal.viewer;

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tcf.te.launch.ui.activator.UIPlugin;
import org.eclipse.tcf.te.launch.ui.internal.ImageConsts;
import org.eclipse.tcf.te.launch.ui.model.LaunchNode;
import org.eclipse.tcf.te.ui.jface.images.AbstractImageDescriptor;
import org.eclipse.ui.navigator.IDescriptionProvider;

/**
 * The label provider for the tree column "launchConfigurations".
 */
public class LaunchTreeLabelProvider extends LabelProvider implements ILabelDecorator, IDescriptionProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof LaunchNode) {
			return ((LaunchNode)element).getName();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element) {
		if (element instanceof LaunchNode) {
			Image image = null;
			LaunchNode node = (LaunchNode)element;
			if (node.isType(LaunchNode.TYPE_ROOT)) {
				image = UIPlugin.getImage(ImageConsts.OBJ_Launches_Root);
			}
			else if (node.isType(LaunchNode.TYPE_LAUNCH_CONFIG_TYPE)) {
				image = DebugUITools.getImage(node.getLaunchConfigurationType().getIdentifier());
			}
			else if (node.isType(LaunchNode.TYPE_LAUNCH_CONFIG)) {
				try {
					image = DebugUITools.getImage(node.getLaunchConfiguration().getType().getIdentifier());
				}
				catch (Exception e) {
				}
			}
			if (image != null) {
				return decorateImage(image, element);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateImage(org.eclipse.swt.graphics.Image, java.lang.Object)
	 */
	@Override
	public Image decorateImage(final Image image, final Object element) {
		Image decoratedImage = null;

		if (image != null && element instanceof LaunchNode) {
			AbstractImageDescriptor descriptor = new LaunchNodeImageDescriptor(UIPlugin.getDefault().getImageRegistry(),
							image,
							(LaunchNode)element);
			decoratedImage = UIPlugin.getSharedImage(descriptor);
		}

		return decoratedImage;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(java.lang.String, java.lang.Object)
	 */
	@Override
	public String decorateText(final String text, final Object element) {
		return text;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IDescriptionProvider#getDescription(java.lang.Object)
	 */
	@Override
	public String getDescription(Object element) {
		if (element instanceof LaunchNode) {
			return getText(element);
		}
		return null;
	}
}
