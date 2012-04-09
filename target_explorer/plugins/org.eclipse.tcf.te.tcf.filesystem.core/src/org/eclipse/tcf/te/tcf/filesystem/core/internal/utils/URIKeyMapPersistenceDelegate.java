/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.core.internal.utils;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.tcf.te.runtime.persistence.AbstractGsonMapPersistenceDelegate;

/**
 * The persistence delegate to persist or restore a map whose keys are URIs.
 */
public class URIKeyMapPersistenceDelegate extends AbstractGsonMapPersistenceDelegate {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate#getPersistedClass(java.lang.Object)
	 */
	@Override
	public Class<?> getPersistedClass(Object context) {
		return Map.class;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.AbstractGsonMapPersistenceDelegate#toMap(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, Object> toMap(Object context) throws IOException {
		Map<URI, ?> attrs = (Map<URI, ?>) context;
		Map<String, Object> result = new HashMap<String, Object>();
		if (attrs != null) {
			for (URI key : attrs.keySet()) {
				Object object = attrs.get(key);
				if (object instanceof URI) {
					String value = "uri:" + object.toString();
					result.put(key.toString(), value);
				}
				else if (object instanceof IContentType) {
					String value = "contenttype:" + ((IContentType) object).getId();
					result.put(key.toString(), value);
				}
				else if (object instanceof Map) {
					Map<QualifiedName, String> map = (Map<QualifiedName, String>) object;
					Map<String, Object> valueMap = qNames2Map(map);
					result.put(key.toString(), valueMap);
				}
				else {
					result.put(key.toString(), object);
				}
			}
		}
		return result;
	}

	/**
	 * Translate the specified map whose keys are QualifiedNames to a
	 * map whose keys are strings.
	 * 
	 * @param map The map to be translated.
	 * @return a map with string keys.
	 */
	private Map<String, Object> qNames2Map(Map<QualifiedName, String> map) {
		Map<String, Object> result = new HashMap<String, Object>();
		for (QualifiedName name : map.keySet()) {
			result.put(name.toString(), map.get(name));
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.persistence.AbstractGsonMapPersistenceDelegate#fromMap(java.util.Map, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Object fromMap(Map<String, Object> map, Object context) throws IOException {
		if (context == null) {
			context = new HashMap<URI, Object>();
		}
		Map<URI, Object> result = (Map<URI, Object>) context;
		for (String key : map.keySet()) {
			Object v = map.get(key);
			URI uri = toURI(key);
			Assert.isNotNull(uri);
			if (v instanceof String) {
				String value = (String) map.get(key);
				Object object = null;
				if (value.startsWith("uri:")) {
					value = value.substring("uri:".length());
					object = toURI(value);
					Assert.isNotNull(object);
					result.put(uri, object);
				}
				else if (value.startsWith("contenttype:")) {
					value = value.substring("contenttype:".length());
					object = Platform.getContentTypeManager().getContentType(value);
					result.put(uri, object);
				}
			}
			else if (v instanceof Number) {
				result.put(uri, Long.valueOf(((Number) v).longValue()));
			}
			else if (v instanceof Map) {
				Map<QualifiedName, String> valueMap = toQNameMap((Map<String, String>) v);
				result.put(uri, valueMap);
			}
		}
		return result;
	}

	/**
	 * Translate the specified map with string keys to a map whose keys are
	 * qualified names.
	 * 
	 * @param strMap The map with string keys.
	 * @return A map with qualified names as keys.
	 */
	private Map<QualifiedName, String> toQNameMap(Map<String, String> strMap) {
		Map<QualifiedName, String> result = new HashMap<QualifiedName, String>();
		for (String key : strMap.keySet()) {
			int dot = key.lastIndexOf(":");
			String qualifier = null;
			String local = key;
			if(dot != -1) {
				qualifier = key.substring(0, dot);
				local = key.substring(dot + 1);
			}
			QualifiedName name = new QualifiedName(qualifier, local);
			result.put(name,  strMap.get(key));
		}
		return result;
	}

	/**
	 * Convert the string to a URI.
	 * 
	 * @param string The string to be converted.
	 * @return the URI or null if there're issues when parsing.
	 */
	private URI toURI(final String string) {
		final AtomicReference<URI> ref = new AtomicReference<URI>();
		SafeRunner.run(new ISafeRunnable(){
			@Override
            public void handleException(Throwable exception) {
				// Ignore on purpose.
            }
			@Override
            public void run() throws Exception {
				ref.set(new URI(string));
            }});
		return ref.get();
	}
}
