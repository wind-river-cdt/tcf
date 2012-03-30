/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.persistence.projects;

import java.util.List;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.tcf.te.launch.core.interfaces.IReferencedProjectItem;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification;
import org.eclipse.tcf.te.launch.core.lm.interfaces.IReferencedProjectLaunchAttributes;
import org.eclipse.tcf.te.launch.core.persistence.AbstractItemListPersistenceDelegate;
import org.eclipse.tcf.te.launch.core.persistence.AbstractItemListXMLParser;

/**
 * Referenced projects persistence delegate.
 */
public class ReferencedProjectsPersistenceDelegate {

	private static final String TAG_PROJECT = "referencedProject"; //$NON-NLS-1$

	private static final AbstractItemListPersistenceDelegate<IReferencedProjectItem> delegate =
					new AbstractItemListPersistenceDelegate<IReferencedProjectItem>(TAG_PROJECT, IReferencedProjectLaunchAttributes.ATTR_REFERENCED_PROJECTS) {
		@Override
		protected AbstractItemListXMLParser<IReferencedProjectItem> getXMLParser() {
			return new AbstractItemListXMLParser<IReferencedProjectItem>(TAG_PROJECT) {
				@Override
				protected Class<?> getReadClass() {
					return IReferencedProjectItem.class;
				}
			};
		}
	};

	/**
	 * Saves the selected referenced project items to the specified launch configuration working copy. If the
	 * selected referenced project items are <code>null</code> or empty, the attribute will be removed from
	 * the specified launch configuration working copy.
	 *
	 * @param wc The launch configuration working copy. Must not be <code>null</code>.
	 * @param items The referenced project items to save or <code>null</code>.
	 */
	public final static void setReferencedProjects(ILaunchConfigurationWorkingCopy wc, IReferencedProjectItem[] items) {
		delegate.setItems(wc, items);
	}

	/**
	 * Saves the selected referenced project items to the specified launch specification. If the selected
	 * referenced project items are <code>null</code> or empty, the attribute will be removed from the
	 * specified launch specification.
	 *
	 * @param launchSpec The launch specification. Must not be <code>null</code>.
	 * @param items The referenced project items to save or <code>null</code>.
	 */
	public final static void setReferencedProjects(ILaunchSpecification launchSpec, IReferencedProjectItem[] items) {
		delegate.setItems(launchSpec, items);
	}

	/**
	 * Writes the given referenced project items into a string encoded in XML.
	 *
	 * @param items The referenced project items to encode. Must not be <code>null</code>.
	 * @return The full XML representation of the given items or <code>null</code>.
	 */
	public final static String encodeReferencedProjectItems(IReferencedProjectItem[] items) {
		return delegate.encodeItems(items);
	}

	/**
	 * Reads the selected referenced project items from the given XML encoded string.
	 *
	 * @param encodedItems The selected referenced project items encoded as XML string. Must not be <code>null</code>.
	 * @return The selected referenced project items or an empty array.
	 */
	public final static IReferencedProjectItem[] decodeReferencedProjectItems(String encodedItems) {
		List<IReferencedProjectItem> list = delegate.decodeItems(encodedItems);
		return list.toArray(new IReferencedProjectItem[list.size()]);
	}

	/**
	 * Returns the list of configured referenced project items from the given launch configuration.
	 * <p>
	 * If the given launch configuration is <code>null</code> and the method will return an empty
	 * array.
	 *
	 * @param configuration The launch configuration or <code>null</code>.
	 * @return The list of configured referenced project items or an empty array.
	 */
	public static final IReferencedProjectItem[] getReferencedProjects(ILaunchConfiguration configuration) {
		List<IReferencedProjectItem> list = delegate.getItems(configuration);
		return list.toArray(new IReferencedProjectItem[list.size()]);
	}

	/**
	 * Returns the list of configured referenced project items from the given launch specification.
	 * <p>
	 * If the given launch specification is <code>null</code> and the method will return an empty
	 * array.
	 *
	 * @param launchSpec The launch specification or <code>null</code>.
	 * @return The list of configured referenced project items or an empty array.
	 */
	public static final IReferencedProjectItem[] getReferencedProjects(ILaunchSpecification launchSpec) {
		List<IReferencedProjectItem> list = delegate.getItems(launchSpec);
		return list.toArray(new IReferencedProjectItem[list.size()]);
	}
}
