/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.persistence.launchcontext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.tcf.te.launch.core.lm.interfaces.IContextSelectorLaunchAttributes;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification;
import org.eclipse.tcf.te.launch.core.persistence.AbstractItemListPersistenceDelegate;
import org.eclipse.tcf.te.launch.core.persistence.AbstractItemListXMLParser;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.runtime.services.ServiceManager;
import org.eclipse.tcf.te.runtime.services.interfaces.IPropertiesAccessService;

/**
 * Context selector persistence delegate.
 */
public class LaunchContextsPersistenceDelegate {
	// The read cache for step contexts. Avoid running time consuming
	// re-parsing of an already parsed step context description again and again.
	private final static Map<String, List<IModelNode>> readCache = new LinkedHashMap<String, List<IModelNode>>();
	// The write cache for target contexts. Avoids re-generating the XML again and again.
	private final static Map<String, String> writeCache = new LinkedHashMap<String, String>();

	// Limit the read cache to the last 10 read step contexts
	private final static int READ_CACHE_MAX_CAPACITY = 25;
	// Limit the write cache to the last 10 written step contexts
	private final static int WRITE_CACHE_MAX_CAPACITY = 25;

	private static final String TAG_LAUNCH_CONTEXT = "context"; //$NON-NLS-1$

	private static final AbstractItemListPersistenceDelegate<IModelNode> delegate =
					new AbstractItemListPersistenceDelegate<IModelNode>(TAG_LAUNCH_CONTEXT, IContextSelectorLaunchAttributes.ATTR_LAUNCH_CONTEXTS) {
		@Override
		protected AbstractItemListXMLParser<IModelNode> getXMLParser() {
			return new AbstractItemListXMLParser<IModelNode>(TAG_LAUNCH_CONTEXT) {
				@Override
				protected Class<?> getReadClass() {
					return IModelNode.class;
				}
			};
		}
	};

	/**
	 * Saves the selected launch contexts to the specified launch configuration working copy. If the
	 * selected launch contexts are <code>null</code> or empty, the attribute will be removed from
	 * the specified launch configuration working copy.
	 *
	 * @param wc The launch configuration working copy. Must not be <code>null</code>.
	 * @param contexts The launch contexts to save or <code>null</code>.
	 */
	public final static void setLaunchContexts(ILaunchConfigurationWorkingCopy wc, IModelNode[] contexts) {
		delegate.setItems(wc, contexts);
	}

	/**
	 * Saves the selected launch contexts to the specified launch specification. If the selected
	 * launch contexts are <code>null</code> or empty, the attribute will be removed from the
	 * specified launch specification.
	 *
	 * @param launchSpec The launch specification. Must not be <code>null</code>.
	 * @param contexts The launch contexts to save or <code>null</code>.
	 */
	public final static void setLaunchContexts(ILaunchSpecification launchSpec, IModelNode[] contexts) {
		delegate.setItems(launchSpec, contexts);
	}

	/**
	 * Writes the given launch contexts into a string encoded in XML.
	 *
	 * @param contexts The launch contexts to encode. Must not be <code>null</code>.
	 * @return The full XML representation of the given contexts or <code>null</code>.
	 */
	public final static String encodeLaunchContexts(IModelNode[] contexts) {
		Assert.isNotNull(contexts);

		// The final result
		String result = null;

		// Generate the write cache key
		String writeCacheKey = makeWriteCacheKey(contexts);

		// Check if we have the contexts already generated before
		synchronized (writeCache) {
			if (writeCache.containsKey(writeCacheKey)) {
				result = writeCache.get(writeCacheKey);
			}
		}

		// If no cache hit, generate from scratch
		if (result == null) {
			result = delegate.encodeItems(contexts);

			synchronized (writeCache) {
				// Limit the write cache capacity
				checkCacheCapacity(writeCache, WRITE_CACHE_MAX_CAPACITY);
				// And put it into the write cache
				writeCache.put(writeCacheKey, result);
			}
		}

		return result;
	}

	/**
	 * Generates a write cache key from the given contexts.
	 *
	 * @param contexts The contexts
	 * @return The corresponding write key cache.
	 */
	private static String makeWriteCacheKey(IModelNode[] contexts) {
		Assert.isNotNull(contexts);

		StringBuffer key = new StringBuffer();
		for (IModelNode context : contexts) {
			key.append(Integer.toHexString(context.hashCode()));
			key.append(':');
		}
		if (key.charAt(key.length() - 1) == ':') {
			key.setCharAt(key.length() - 1, ' ');
		}
		return key.toString().trim();
	}

	/**
	 * Reads the selected launch contexts from the given XML encoded string.
	 *
	 * @param encodedContexts The selected launch contexts encoded as XML string. Must not be <code>null</code>.
	 * @return The selected launch contexts or an empty array.
	 */
	public final static IModelNode[] decodeLaunchContexts(String encodedContexts) {
		Assert.isNotNull(encodedContexts);

		List<IModelNode> contexts = null;

		if (!"".equals(encodedContexts.trim())) { //$NON-NLS-1$
			synchronized (readCache) {
				// Check if we have the contexts already parsed before
				if (readCache.containsKey(encodedContexts)) {
					// Take the result from the cache
					contexts = readCache.get(encodedContexts);
					// check sanity. If empty or we cannot find the step context,
					// drop the cache value and decode again.
					ListIterator<IModelNode> iterator = contexts.listIterator();
					while (iterator.hasNext()) {
						IModelNode node = iterator.next();
						IPropertiesAccessService service = ServiceManager.getInstance().getService(node, IPropertiesAccessService.class);
						boolean isGhost = false;
						if (service != null) {
							Object value = service.getProperty(node, IModelNode.PROPERTY_IS_GHOST);
							if (value instanceof Boolean) {
								isGhost = ((Boolean)value).booleanValue();
							}
						}
						if (isGhost) {
							contexts = null;
							readCache.remove(encodedContexts);
							break;
						}
					}

					if (contexts != null && contexts.isEmpty()) {
						readCache.remove(encodedContexts);
						contexts = null;
					}
				}
			}

			if (contexts == null || contexts.isEmpty()) {
				contexts = delegate.decodeItems(encodedContexts);
				if (!contexts.isEmpty()) {
					synchronized (readCache) {
						// Limit the read cache capacity
						checkCacheCapacity(readCache, READ_CACHE_MAX_CAPACITY);
						// Put the result into the read cache
						readCache.put(encodedContexts, contexts);
					}
				}
			}
		}

		return contexts != null ? contexts.toArray(new IModelNode[contexts.size()]) : new IModelNode[0];
	}

	/**
	 * Internal helper method to ensure a maximum capacity of the caches.
	 */
	private final static void checkCacheCapacity(Map<String, ?> cache, int maxCapacity) {
		if (cache.size() < maxCapacity) {
			return;
		}
		// Get all keys
		String[] keys = cache.keySet().toArray(new String[cache.keySet().size()]);
		// And remove all keys starting with the eldest till the
		// capacity is fine again.
		for (String key : keys) {
			cache.remove(key);
			if (cache.size() < maxCapacity / 2) {
				break;
			}
		}
	}

	/**
	 * Returns the list of configured launch contexts from the given launch configuration.
	 * <p>
	 * If the given launch configuration is <code>null</code> and the method will return an empty
	 * array.
	 *
	 * @param configuration The launch configuration or <code>null</code>.
	 * @return The list of configured launch contexts or an empty array.
	 */
	public static final IModelNode[] getLaunchContexts(ILaunchConfiguration configuration) {
		List<IModelNode> list = delegate.getItems(configuration);
		return list.toArray(new IModelNode[list.size()]);
	}

	/**
	 * Returns the list of configured launch contexts from the given launch specification.
	 * <p>
	 * If the given launch specification is <code>null</code> and the method will return an empty
	 * array.
	 *
	 * @param launchSpec The launch specification or <code>null</code>.
	 * @return The list of configured launch contexts or an empty array.
	 */
	public static final IModelNode[] getLaunchContexts(ILaunchSpecification launchSpec) {
		List<IModelNode> list = delegate.getItems(launchSpec);
		return list.toArray(new IModelNode[list.size()]);
	}

	/**
	 * Returns the first configured launch context from the given launch configuration.
	 *
	 * @param configuration The launch configuration or <code>null</code>.
	 * @return The first configured launch context or <code>null</code>.
	 */
	public static final IModelNode getFirstLaunchContext(ILaunchConfiguration configuration) {
		List<IModelNode> list = delegate.getItems(configuration);
		return list != null && !list.isEmpty() ? list.get(0) : null;
	}

}
