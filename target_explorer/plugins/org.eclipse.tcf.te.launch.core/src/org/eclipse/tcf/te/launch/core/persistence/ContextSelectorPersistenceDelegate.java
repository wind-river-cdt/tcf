/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.persistence;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.tcf.te.launch.core.activator.CoreBundleActivator;
import org.eclipse.tcf.te.launch.core.lm.interfaces.IContextSelectorLaunchAttributes;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext;
import org.osgi.framework.Bundle;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Context selector persistence delegate.
 */
public class ContextSelectorPersistenceDelegate {
	// The read cache for step contexts. Avoid running time consuming
	// re-parsing of an already parsed step context description again and again.
	private final static Map<String, List<IStepContext>> readCache = new LinkedHashMap<String, List<IStepContext>>();
	// The write cache for target contexts. Avoids re-generating the XML again and again.
	private final static Map<String, String> writeCache = new LinkedHashMap<String, String>();

	// Limit the read cache to the last 10 read step contexts
	private final static int READ_CACHE_MAX_CAPACITY = 25;
	// Limit the write cache to the last 10 written step contexts
	private final static int WRITE_CACHE_MAX_CAPACITY = 25;

	/**
	 * Saves the selected launch contexts to the specified launch configuration working copy. If the
	 * selected launch contexts are <code>null</code> or empty, the attribute will be removed from
	 * the specified launch configuration working copy.
	 *
	 * @param wc The launch configuration working copy. Must not be <code>null</code>.
	 * @param contexts The launch contexts to save or <code>null</code>.
	 */
	public final static void setLaunchContexts(ILaunchConfigurationWorkingCopy wc, IStepContext[] contexts) {
		Assert.isNotNull(wc);

		if (contexts == null || contexts.length == 0) {
			DefaultPersistenceDelegate.setAttribute(wc, IContextSelectorLaunchAttributes.ATTR_LAUNCH_CONTEXTS, (String) null);
			return;
		}

		// Get the encoded XML representation
		String xml = encodeLaunchContexts(contexts);
		// And save them to the launch configuration. If XML == null, the
		// launch contexts will be removed from the launch configuration
		DefaultPersistenceDelegate.setAttribute(wc, IContextSelectorLaunchAttributes.ATTR_LAUNCH_CONTEXTS, xml);
	}

	/**
	 * Saves the selected launch contexts to the specified launch specification. If the selected
	 * launch contexts are <code>null</code> or empty, the attribute will be removed from the
	 * specified launch specification.
	 *
	 * @param launchSpec The launch specification. Must not be <code>null</code>.
	 * @param contexts The launch contexts to save or <code>null</code>.
	 */
	public final static void setLaunchContexts(ILaunchSpecification launchSpec, IStepContext[] contexts) {
		Assert.isNotNull(launchSpec);

		if (contexts == null || contexts.length == 0) {
			launchSpec.removeAttribute(IContextSelectorLaunchAttributes.ATTR_LAUNCH_CONTEXTS);
			return;
		}

		// Get the encoded XML representation
		String xml = encodeLaunchContexts(contexts);
		// And save them to the launch specification. If XML == null, the
		// launch contexts will be removed from the launch specification
		launchSpec.addAttribute(IContextSelectorLaunchAttributes.ATTR_LAUNCH_CONTEXTS, xml);
	}

	/**
	 * Writes the given launch contexts into a string encoded in XML.
	 *
	 * @param contexts The launch contexts to encode. Must not be <code>null</code>.
	 * @return The full XML representation of the given contexts or <code>null</code>.
	 */
	public final static String encodeLaunchContexts(IStepContext[] contexts) {
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
			// First, we write the selected contexts as XML representation into a string
			StringWriter writer = new StringWriter();

			try {
				// Write the header and get the initial indentation
				String indentation = writeHeader(writer);
				// Iterate over the given selected step contexts and write them out.
				for (IStepContext node : contexts) {
					writeStepContext(writer, indentation, node);
				}
				// Write the footer
				writeFooter(writer);

				// Convert into a string
				result = writer.toString();

				synchronized (writeCache) {
					// Limit the write cache capacity
					checkCacheCapacity(writeCache, WRITE_CACHE_MAX_CAPACITY);
					// And put it into the write cache
					writeCache.put(writeCacheKey, result);
				}
			}
			catch (IOException e) {
				// Export to the string writer failed --> remove attribute from launch configuration
				if (CoreBundleActivator.getTraceHandler().getDebugMode() > 0) {
					IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
												"Launch framework internal error: " + e.getLocalizedMessage(), e); //$NON-NLS-1$
					Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
				}
				result = null;
			}
			finally {
				try {
					writer.close();
				}
				catch (IOException e) { /* ignored on purpose */
				}
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
	private static String makeWriteCacheKey(IStepContext[] contexts) {
		Assert.isNotNull(contexts);

		StringBuffer key = new StringBuffer();
		for (IStepContext context : contexts) {
			key.append(Integer.toHexString(context.hashCode()));
			key.append(':');
		}
		if (key.charAt(key.length() - 1) == ':') {
			key.setCharAt(key.length() - 1, ' ');
		}
		return key.toString().trim();
	}

	/**
	 * Writes the header to the given writer and returns the indentation to be used for following
	 * elements.
	 *
	 * @param writer The writer instance. Must not be <code>null</code>.
	 * @throws IOException in case the write failed.
	 */
	private static String writeHeader(Writer writer) throws IOException {
		Assert.isNotNull(writer);
		writer.write("<contexts>\n"); //$NON-NLS-1$
		return "\t"; //$NON-NLS-1$
	}

	/**
	 * Writes the footer to the given writer and returns the indentation to be used for following
	 * elements.
	 *
	 * @param writer The writer instance. Must not be <code>null</code>.
	 * @throws IOException in case the write failed.
	 */
	private static String writeFooter(Writer writer) throws IOException {
		Assert.isNotNull(writer);
		writer.write("</contexts>\n"); //$NON-NLS-1$
		return ""; //$NON-NLS-1$
	}

	/**
	 * Writes the step context element to the given writer.
	 *
	 * @param writer The writer instance. Must not be <code>null</code>.
	 * @param indentation The indentation to prefix each exported line with. Must not be <code>null</code>.
	 * @param context The step context instance. Must not be <code>null</code>.
	 *
	 * @throws IOException in case the write failed.
	 */
	private static void writeStepContext(Writer writer, String indentation, IStepContext context) throws IOException {
		Assert.isNotNull(writer);
		Assert.isNotNull(indentation);
		Assert.isNotNull(context);

		writer.write(indentation + "<context type=\"" + context.getModelNode().getClass().getName() + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.write(indentation + "\t" + context.encode() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		writer.write(indentation + "</context>\n"); //$NON-NLS-1$
	}

	/**
	 * Reads the selected launch contexts from the given XML encoded string.
	 *
	 * @param encodedContexts The selected launch contexts encoded as XML string. Must not be <code>null</code>.
	 * @return The selected launch contexts or an empty array.
	 */
	public final static IStepContext[] decodeLaunchContexts(String encodedContexts) {
		Assert.isNotNull(encodedContexts);

		List<IStepContext> contexts = null;

		if (!"".equals(encodedContexts.trim())) { //$NON-NLS-1$
			synchronized (readCache) {
				// Check if we have the contexts already parsed before
				if (readCache.containsKey(encodedContexts)) {
					// Take the result from the cache
					contexts = readCache.get(encodedContexts);
					// check sanity. If empty or we cannot find the step context,
					// drop the cache value and decode again.
					ListIterator<IStepContext> iterator = contexts.listIterator();
					while (iterator.hasNext()) {
						IStepContext node = iterator.next();
						if (!node.exists()) {
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
				contexts = new ArrayList<IStepContext>();
				// We have to parse the contexts from the string
				InputStream input = new ByteArrayInputStream(encodedContexts.getBytes());
				// Instantiate the XML parser
				LaunchContextXMLParser xmlParser = new LaunchContextXMLParser();
				xmlParser.initXMLParser();
				xmlParser.setContexts(contexts);
				try {
					xmlParser.getXMLReader().parse(input, xmlParser);
					if (!contexts.isEmpty()) {
						synchronized (readCache) {
							// Limit the read cache capacity
							checkCacheCapacity(readCache, READ_CACHE_MAX_CAPACITY);
							// Put the result into the read cache
							readCache.put(encodedContexts, contexts);
						}
					}
				}
				catch (Exception e) {
					// Import failed --> remove attribute from launch configuration
					if (CoreBundleActivator.getTraceHandler().getDebugMode() > 0) {
						IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
													"Launch framework internal error: " + e.getLocalizedMessage(), e); //$NON-NLS-1$
						Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
					}
					contexts = null;
				}
			}
		}

		return contexts != null ? contexts.toArray(new IStepContext[contexts.size()]) : new IStepContext[0];
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

	private final static class LaunchContextXMLParser extends DefaultHandler {
		private final int IN_CONTEXTS_DEFINITION = 1;
		private final int IN_CONTEXT_DEFINITION = 2;

		private SAXParser parser;

		private int parseState;
		private String lastData;
		private String lastType;
		private List<IStepContext> contexts;

		/**
		 * Constructor
		 */
		public LaunchContextXMLParser() {
			super();

			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(false);
			factory.setValidating(false);
			try {
				parser = factory.newSAXParser();
			}
			catch (ParserConfigurationException e) {
				IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), e.getClass().getName(), e);
				Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
			}
			catch (SAXException e) {
				IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), e.getClass().getName(), e);
				Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
			}
		}

		/**
		 * Returns the associated XML parser instance.
		 */
		protected SAXParser getXMLReader() {
			return parser;
		}

		/**
		 * Reset the XML parser to a defined start point.
		 */
		protected void initXMLParser() {
			parseState = 0;
			lastData = null;
			lastType = null;
			contexts = null;
		}

		/**
		 * Associate the list instance to store the identified contexts.
		 */
		protected void setContexts(List<IStepContext> contexts) {
			this.contexts = contexts;
		}

		/*
		 * (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String,
		 * java.lang.String)
		 */
		@Override
		public void endElement(String uri, String localName, String name) throws SAXException {
			if ("contexts".equalsIgnoreCase(name) && (parseState & IN_CONTEXTS_DEFINITION) == IN_CONTEXTS_DEFINITION) { //$NON-NLS-1$
				parseState ^= IN_CONTEXTS_DEFINITION;
			}
			if ("context".equalsIgnoreCase(name) && (parseState & IN_CONTEXT_DEFINITION) == IN_CONTEXT_DEFINITION) { //$NON-NLS-1$
				parseState ^= IN_CONTEXT_DEFINITION;

				// The context encoded string is in last data
				if (lastType != null && lastData != null) {
					Class<IModelNode> clazz = null;
					try {
						clazz = (Class<IModelNode>)CoreBundleActivator.getContext().getBundle().loadClass(lastType);
					} catch (ClassNotFoundException e) {
						if (CoreBundleActivator.getTraceHandler().getDebugMode() > 0) {
							IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
														"Launch framework internal error: " + e.getLocalizedMessage(), e); //$NON-NLS-1$
							Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
						}
					}

					// If the class could not be loaded by our own bundle class loader, try to find
					// the bundle from the class name and try to load the class through the bundle.
					if (clazz == null) {
						String bundleId = lastType;
						Bundle bundle = null;
						while (bundleId != null && bundle == null) {
							bundle = Platform.getBundle(bundleId);
							if (bundle == null) {
								int i = bundleId.lastIndexOf('.');
								if (i != -1) {
									bundleId = bundleId.substring(0, i);
								} else {
									bundleId = null;
								}
							}
						}

						if (bundle != null) {
							try {
								clazz = (Class<IModelNode>)bundle.loadClass(lastType);
							} catch (ClassNotFoundException e) {
								if (CoreBundleActivator.getTraceHandler().getDebugMode() > 0) {
									IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
																"Launch framework internal error: " + e.getLocalizedMessage(), e); //$NON-NLS-1$
									Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
								}
							}
						}
					}

					if (clazz != null) {
						// Try to load the step context
						IStepContext context = (IStepContext)Platform.getAdapterManager().loadAdapter(clazz, IStepContext.class.getName());
						if (context != null && !contexts.contains(context)) {
							contexts.add(context);
						}
					}
				}
			}
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
			// Each time we start a new element, throw away the lastData content
			lastData = null;

			if ("contexts".equalsIgnoreCase(name)) { //$NON-NLS-1$
				parseState |= IN_CONTEXTS_DEFINITION;
			}
			if ("context".equalsIgnoreCase(name) && (parseState & IN_CONTEXTS_DEFINITION) == IN_CONTEXTS_DEFINITION) { //$NON-NLS-1$
				parseState |= IN_CONTEXT_DEFINITION;
				lastType = attributes.getValue("type"); //$NON-NLS-1$
			}
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
		 */
		@Override
		public void characters(char ch[], int start, int length) {
			if (lastData == null) {
				lastData = new String(ch, start, length).trim();
			}
			else {
				lastData += new String(ch, start, length).trim();
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
	public static final IStepContext[] getLaunchContexts(ILaunchConfiguration configuration) {
		IStepContext[] contexts = new IStepContext[0];

		// First read the contexts written by the launch context selector control.
		if (configuration != null) {
			// Read the context attribute from the launch configuration
			String encodedContexts = DefaultPersistenceDelegate.getAttribute(configuration, IContextSelectorLaunchAttributes.ATTR_LAUNCH_CONTEXTS, (String) null);
			if (encodedContexts != null) {
				contexts = ContextSelectorPersistenceDelegate.decodeLaunchContexts(encodedContexts);
			}
		}

		return contexts;
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
	public static final IStepContext[] getLaunchContexts(ILaunchSpecification launchSpec) {
		IStepContext[] contexts = new IStepContext[0];

		// First read the contexts written by the launch context selector control.
		if (launchSpec != null) {
			// Read the context attribute from the launch specification
			String encodedContexts = (String) launchSpec.getAttribute(IContextSelectorLaunchAttributes.ATTR_LAUNCH_CONTEXTS, null);
			if (encodedContexts != null) {
				contexts = ContextSelectorPersistenceDelegate.decodeLaunchContexts(encodedContexts);
			}
		}

		return contexts;
	}
}
