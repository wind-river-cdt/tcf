/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.va;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager;
import org.eclipse.tcf.te.runtime.extensions.ExecutableExtensionProxy;
import org.eclipse.tcf.te.tcf.core.va.interfaces.IValueAdd;
import org.eclipse.tcf.te.tcf.core.va.internal.Binding;
import org.eclipse.tcf.te.tcf.core.va.internal.BindingExtensionPointManager;

/**
 * Value add manager implementation.
 */
public class ValueAddManager extends AbstractExtensionPointManager<IValueAdd> {
	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstance {
		public static ValueAddManager instance = new ValueAddManager();
	}

	/**
	 * Constructor.
	 */
	ValueAddManager() {
		super();
	}

	/**
	 * Returns the singleton instance of the extension point manager.
	 */
	public static ValueAddManager getInstance() {
		return LazyInstance.instance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager#getExtensionPointId()
	 */
	@Override
	protected String getExtensionPointId() {
		return "org.eclipse.tcf.te.tcf.core.valueadds"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager#getConfigurationElementName()
	 */
	@Override
	protected String getConfigurationElementName() {
		return "valueadd"; //$NON-NLS-1$
	}

	/**
	 * Returns the list of all contributed value adds.
	 *
	 * @param unique If <code>true</code>, the method returns new instances for each
	 *               contributed value add.
	 *
	 * @return The list of contributed value adds, or an empty array.
	 */
	public IValueAdd[] getValueAdds(boolean unique) {
		List<IValueAdd> contributions = new ArrayList<IValueAdd>();
		Collection<ExecutableExtensionProxy<IValueAdd>> valueAdds = getExtensions().values();
		for (ExecutableExtensionProxy<IValueAdd> valueAdd : valueAdds) {
			IValueAdd instance = unique ? valueAdd.newInstance() : valueAdd.getInstance();
			if (instance != null && !contributions.contains(instance)) {
				contributions.add(instance);
			}
		}

		return contributions.toArray(new IValueAdd[contributions.size()]);
	}

	/**
	 * Returns the value add identified by its unique id. If no value
	 * add with the specified id is registered, <code>null</code> is returned.
	 *
	 * @param id The unique id of the value add or <code>null</code>
	 * @param unique If <code>true</code>, the method returns new instances of the value add contribution.
	 *
	 * @return The value add instance or <code>null</code>.
	 */
	public IValueAdd getValueAdd(String id, boolean unique) {
		IValueAdd contribution = null;
		if (getExtensions().containsKey(id)) {
			ExecutableExtensionProxy<IValueAdd> proxy = getExtensions().get(id);
			// Get the extension instance
			contribution = unique ? proxy.newInstance() : proxy.getInstance();
		}

		return contribution;
	}

	/**
	 * Returns the value adds which are enabled for the given peer context.
	 *
	 * @param peer The peer context. Must not be <code>null</code>.
	 * @return The list of value adds which are enabled or an empty array.
	 */
	public IValueAdd[] getValueAdd(IPeer peer) {
		Assert.isNotNull(peer);

		List<IValueAdd> valueAdds = new ArrayList<IValueAdd>();

		// Get the list of applicable bindings
		Binding[] bindings = BindingExtensionPointManager.getInstance().getApplicableBindings(peer);
		for (Binding binding : bindings) {
			IValueAdd valueAdd = getValueAdd(binding.getValueAddId(), false);
			if (valueAdd != null && !valueAdds.contains(valueAdd)) valueAdds.add(valueAdd);
		}

		return valueAdds.toArray(new IValueAdd[valueAdds.size()]);
	}

}
