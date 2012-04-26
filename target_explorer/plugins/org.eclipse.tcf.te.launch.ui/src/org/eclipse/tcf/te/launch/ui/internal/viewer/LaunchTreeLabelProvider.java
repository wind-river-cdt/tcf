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

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tcf.te.launch.ui.activator.UIPlugin;
import org.eclipse.tcf.te.launch.ui.internal.ImageConsts;
import org.eclipse.tcf.te.launch.ui.model.LaunchNode;
import org.eclipse.tcf.te.ui.jface.images.AbstractImageDescriptor;

/**
 * The label provider for the tree column "launchConfigurations".
 */
public class LaunchTreeLabelProvider extends LabelProvider implements ILabelDecorator {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof LaunchNode) {
			return ((LaunchNode)element).getName();
		}
		else if (element instanceof ILaunchConfigurationType) {
			return ((ILaunchConfigurationType)element).getName();
		}
		else if (element instanceof ILaunchConfiguration) {
			return ((ILaunchConfiguration)element).getName();
		}
		return super.getText(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element) {
		if (element instanceof LaunchNode) {
			Image image = null;
			LaunchNode node = (LaunchNode)element;
			if (LaunchNode.TYPE_ROOT.equals(node.getType())) {
				image = UIPlugin.getImage(ImageConsts.OBJ_Launches_Root);
			}
			else if (LaunchNode.TYPE_LAUNCH_CONFIG_TYPE.equals(node.getType())) {
				image = DebugUITools.getImage(node.getLaunchConfigurationType().getIdentifier());
			}
			else if (LaunchNode.TYPE_LAUNCH_CONFIG.equals(node.getType())) {
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
		return super.getImage(element);
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
}
